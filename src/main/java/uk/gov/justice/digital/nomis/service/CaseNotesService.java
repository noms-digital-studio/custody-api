package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.CaseNote;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCaseNote;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderCaseNotesFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderCaseNotesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderCaseNotesTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CaseNotesService {

    private static final Comparator<OffenderCaseNote> BY_CONTACT_TIMESTAMP =
            Comparator.comparing(OffenderCaseNote::getContactDate)
                    .thenComparing(OffenderCaseNote::getContactTime)
                    .reversed();

    private final OffenderCaseNotesTransformer offenderCaseNotesTransformer;
    private final OffenderCaseNotesRepository offenderCaseNotesRepository;
    private final OffenderRepository offenderRepository;

    @Autowired
    public CaseNotesService(OffenderCaseNotesTransformer offenderCaseNotesTransformer,
                            OffenderCaseNotesRepository offenderCaseNotesRepository,
                            OffenderRepository offenderRepository) {
        this.offenderCaseNotesTransformer = offenderCaseNotesTransformer;
        this.offenderCaseNotesRepository = offenderCaseNotesRepository;
        this.offenderRepository = offenderRepository;
    }

    public Optional<List<CaseNote>> getCaseNotesForOffenderId(Long offenderId,
                                                              Optional<LocalDateTime> maybeFrom,
                                                              Optional<LocalDateTime> maybeTo,
                                                              Optional<Set<String>> maybeTypeFilter,
                                                              Optional<Set<String>> maybeSubTypeFilter) {
        LocalDateTime from = maybeFrom.orElse(maybeTo.map(to -> to.minusDays(1)).orElse(LocalDate.now().atStartOfDay()));
        LocalDateTime to = maybeTo.orElse(from.plusDays(1));

        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings().stream()
                .flatMap(ob -> offenderCaseNotesRepository.findAll(OffenderCaseNotesFilter.builder()
                        .bookingId(ob.getOffenderBookId())
                        .from(from)
                        .to(to)
                        .types(maybeTypeFilter)
                        .subTypes(maybeSubTypeFilter)
                        .build()).stream())
                .sorted(BY_CONTACT_TIMESTAMP)
                .map(offenderCaseNotesTransformer::caseNoteOf)
                .collect(Collectors.toList()));
    }

    public Optional<List<CaseNote>> getCaseNotesForOffenderIdAndBookingId(Long offenderId,
                                                              Long bookingId,
                                                              Optional<LocalDateTime> maybeFrom,
                                                              Optional<LocalDateTime> maybeTo,
                                                              Optional<Set<String>> maybeTypeFilter,
                                                              Optional<Set<String>> maybeSubTypeFilter) {
        LocalDateTime from = maybeFrom.orElse(maybeTo.map(to -> to.minusDays(1)).orElse(LocalDate.now().atStartOfDay()));
        LocalDateTime to = maybeTo.orElse(from.plusDays(1));

        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings().stream()
                .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                .flatMap(ob -> offenderCaseNotesRepository.findAll(OffenderCaseNotesFilter.builder()
                        .from(from)
                        .to(to)
                        .bookingId(ob.getOffenderBookId())
                        .types(maybeTypeFilter)
                        .subTypes(maybeSubTypeFilter)
                        .build()).stream())
                .sorted(BY_CONTACT_TIMESTAMP)
                .map(offenderCaseNotesTransformer::caseNoteOf)
                .collect(Collectors.toList()));
    }
}
