package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.MilitaryRecord;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderMilitaryRecord;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderMilitaryRecordFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderMilitaryRecordsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.MilitaryRecordsTransformer;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MilitaryRecordsService {
    private static final Comparator<OffenderMilitaryRecord> BY_SEQUENCE = Comparator
            .comparingLong((OffenderMilitaryRecord oem) -> oem.getId().getMilitarySeq())
            .reversed();
    private final OffenderMilitaryRecordsRepository offenderMilitaryRecordsRepository;
    private final OffenderRepository offenderRepository;
    private final MilitaryRecordsTransformer militaryRecordsTransformer;

    @Autowired
    public MilitaryRecordsService(OffenderMilitaryRecordsRepository offenderMilitaryRecordsRepository,
                                  OffenderRepository offenderRepository,
                                  MilitaryRecordsTransformer militaryRecordsTransformer) {
        this.offenderMilitaryRecordsRepository = offenderMilitaryRecordsRepository;
        this.offenderRepository = offenderRepository;
        this.militaryRecordsTransformer = militaryRecordsTransformer;
    }

    @Transactional
    public Page<MilitaryRecord> getMilitaryRecords(Pageable pageable,
                                                   Optional<LocalDateTime> maybeFrom,
                                                   Optional<LocalDateTime> maybeTo) {
        Page<OffenderMilitaryRecord> offenderMilitaryRecords = offenderMilitaryRecordsRepository.findAll(OffenderMilitaryRecordFilter.builder()
                .from(maybeFrom)
                .to(maybeTo)
                .build(), pageable);

        List<MilitaryRecord> offenderMilitaryRecordList = offenderMilitaryRecords.getContent()
                .stream()
                .sorted(BY_SEQUENCE)
                .map(militaryRecordsTransformer::militaryRecordOf)
                .collect(Collectors.toList());

        return new PageImpl<>(offenderMilitaryRecordList, pageable, offenderMilitaryRecords.getTotalElements());
    }

    @Transactional
    public Optional<List<MilitaryRecord>> getMilitaryRecordsForOffenderId(Long offenderId) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        Optional<List<OffenderBooking>> maybeBookings = maybeOffender.map(o -> o.getOffenderBookings());

        return maybeBookings.map(bookings -> bookings.stream()
                .flatMap(ob -> ob.getOffenderMilitaryRecords().stream())
                .sorted(BY_SEQUENCE)
                .map(militaryRecordsTransformer::militaryRecordOf)
                .collect(Collectors.toList()));
    }

    public Optional<List<MilitaryRecord>> getMilitaryRecordsForOffenderIdAndBookingId(Long offenderId, Long bookingId) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        Optional<OffenderBooking> maybeBooking = maybeOffender
                .flatMap(offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst());

        return maybeBooking.map(ob -> ob.getOffenderMilitaryRecords()
                .stream()
                .sorted(BY_SEQUENCE)
                .map(militaryRecordsTransformer::militaryRecordOf)
                .collect(Collectors.toList()));
    }
}
