package uk.gov.justice.digital.nomis.service;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.ProgrammeProfile;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderProgramProfilesFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderProgramProfilesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderProgrammeProfileTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OffenderProgrammeProfilesService {

    private final OffenderRepository offenderRepository;
    private final OffenderProgrammeProfileTransformer offenderProgrammeProfileTransformer;
    private final OffenderProgramProfilesRepository offenderProgramProfilesRepository;

    @Autowired
    public OffenderProgrammeProfilesService(OffenderRepository offenderRepository, OffenderProgrammeProfileTransformer offenderProgrammeProfileTransformer, OffenderProgramProfilesRepository offenderProgramProfilesRepository) {
        this.offenderRepository = offenderRepository;
        this.offenderProgramProfilesRepository = offenderProgramProfilesRepository;
        this.offenderProgrammeProfileTransformer = offenderProgrammeProfileTransformer;
    }

    public Optional<List<ProgrammeProfile>> offenderProgrammeProfilesForOffenderId(Long offenderId, Optional<LocalDateTime> maybeFrom, Optional<LocalDateTime> maybeTo) {
        final Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        final Optional<List<OffenderBooking>> maybeOffenderBookings = maybeOffender.map(Offender::getOffenderBookings);

        LocalDateTime from = maybeFrom.orElse(maybeTo.orElse(LocalDate.now().atStartOfDay()));
        LocalDateTime to = maybeTo.orElse(from.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1));

        Optional<List<ProgrammeProfile>> out = maybeOffenderBookings.map(bookings -> bookings
                .stream()
                .flatMap(ob -> offenderProgramProfilesRepository
                        .findAll(OffenderProgramProfilesFilter.builder()
                                .bookingId(ob.getOffenderBookId())
                                .from(from)
                                .to(to)
                                .build())
                        .stream()
                        .map(opp -> offenderProgrammeProfileTransformer.programmeProfileOf(opp, from, to)))
                .collect(Collectors.toSet()))
                .map(ImmutableList::copyOf);
        return out;
    }

    public Optional<List<ProgrammeProfile>> offenderProgrammeProfilesForOffenderIdAndBookingId(Long offenderId, Long bookingId, Optional<LocalDateTime> maybeFrom, Optional<LocalDateTime> maybeTo) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        LocalDateTime from = maybeFrom.orElse(maybeTo.orElse(LocalDate.now().atStartOfDay()));
        LocalDateTime to = maybeTo.orElse(from.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1));

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> offenderProgramProfilesRepository
                        .findAll(OffenderProgramProfilesFilter.builder()
                                .bookingId(ob.getOffenderBookId())
                                .from(from)
                                .to(to)
                                .build())
                        .stream()
                        .map(opp -> offenderProgrammeProfileTransformer.programmeProfileOf(opp, from, to))
                        .collect(Collectors.toSet()))
                .map(ImmutableList::copyOf);
    }
}