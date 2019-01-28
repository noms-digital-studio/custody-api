package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.OffenderImprisonmentStatus;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderImprisonStatus;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderImprisonStatusesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.ImprisonStatusTransformer;
import uk.gov.justice.digital.nomis.service.transformer.TypesTransformer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class ImprisonStatusService {

    private final ImprisonStatusTransformer imprisonStatusTransformer;
    private final OffenderImprisonStatusesRepository offenderImprisonStatusesRepository;
    private final OffenderRepository offenderRepository;

    private static final Comparator<OffenderImprisonStatus> BY_EFFECTIVE_STATUS = Comparator
            .comparing(OffenderImprisonStatus::getLatestStatus, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(OffenderImprisonStatus::getEffectiveDate)
            .thenComparing(OffenderImprisonStatus::getEffectiveTime)
            .thenComparing(OffenderImprisonStatus::getImprisonStatusSeq)
            .reversed();

    private final TypesTransformer typesTransformer;

    @Autowired
    public ImprisonStatusService(ImprisonStatusTransformer imprisonStatusTransformer, OffenderImprisonStatusesRepository offenderImprisonStatusesRepository, OffenderRepository offenderRepository, TypesTransformer typesTransformer) {
        this.imprisonStatusTransformer = imprisonStatusTransformer;
        this.offenderImprisonStatusesRepository = offenderImprisonStatusesRepository;
        this.offenderRepository = offenderRepository;
        this.typesTransformer = typesTransformer;
    }


    @Transactional
    public Page<OffenderImprisonmentStatus> getOffenderImprisonStatuses(Pageable pageable) {
        Page<uk.gov.justice.digital.nomis.jpa.entity.OffenderImprisonStatus> rawImprisonStatusesPage = offenderImprisonStatusesRepository.findAll(pageable);

        List<OffenderImprisonmentStatus> offenderImprisonmentStatuses = rawImprisonStatusesPage.getContent()
                .stream()
                .sorted(BY_EFFECTIVE_STATUS)
                .map(imprisonStatusTransformer::offenderImprisonStatusOf)
                .collect(Collectors.toList());

        return new PageImpl<>(offenderImprisonmentStatuses, pageable, rawImprisonStatusesPage.getTotalElements());
    }

    @Transactional
    public Optional<List<OffenderImprisonmentStatus>> offenderImprisonStatusForOffenderId(Long offenderId) {

        Optional<Stream<OffenderImprisonStatus>> maybeImprisonStatuses = offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings()
                        .stream()
                        .map(OffenderBooking::getOffenderImprisonStatuses)
                        .flatMap(Collection::stream));

        return maybeImprisonStatuses.map(imprisonStatuses -> imprisonStatuses
                .sorted(BY_EFFECTIVE_STATUS)
                .map(imprisonStatusTransformer::offenderImprisonStatusOf)
                .collect(Collectors.toList()));
    }

    public Optional<List<OffenderImprisonmentStatus>> offenderImprisonStatusForOffenderIdAndBookingId(Long offenderId, Long bookingId) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderImprisonStatuses()
                        .stream()
                        .sorted(BY_EFFECTIVE_STATUS)
                        .map(imprisonStatusTransformer::offenderImprisonStatusOf)
                        .collect(Collectors.toList()));
    }

}
