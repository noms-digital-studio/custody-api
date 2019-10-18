package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Charge;
import uk.gov.justice.digital.nomis.jpa.entity.Offence;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCharge;
import uk.gov.justice.digital.nomis.jpa.filters.ChargesFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderChargesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.ChargesTransformer;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChargesService {

    private static final Comparator<OffenderCharge> BY_OFFENCE_RANK = Comparator
            .comparing((OffenderCharge oc) -> Optional.ofNullable(oc.getOffence())
                    .map(Offence::getSeverityRanking)
                    .orElse(null), Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(OffenderCharge::getOffenderChargeId);

    private final OffenderChargesRepository offenderChargesRepository;
    private final OffenderRepository offenderRepository;
    private final ChargesTransformer chargesTransformer;

    @Autowired
    public ChargesService(final OffenderChargesRepository offenderChargesRepository,
                          final OffenderRepository offenderRepository, final ChargesTransformer chargesTransformer) {
        this.offenderChargesRepository = offenderChargesRepository;
        this.offenderRepository = offenderRepository;
        this.chargesTransformer = chargesTransformer;
    }

    public Page<Charge> getCharges(final Pageable pageable, final Set<Long> bookingIds) {
        final var offenderCharges = offenderChargesRepository.findAll(
                ChargesFilter.builder()
                        .bookingIds(bookingIds)
                        .build(),
                pageable);

        final var chargesList = offenderCharges.getContent()
                .stream()
                .map(chargesTransformer::chargeOf)
                .collect(Collectors.toList());

        return new PageImpl<>(chargesList, pageable, offenderCharges.getTotalElements());
    }

    public Optional<List<Charge>> chargesForOffenderId(final Long offenderId) {

        final var maybeOffenderCharges = offenderRepository.findById(offenderId)
                .map(offender ->
                        offender.getOffenderBookings()
                                .stream()
                                .map(OffenderBooking::getOffenderCharges)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));

        return maybeOffenderCharges.map(offenderCharges ->
                offenderCharges.stream()
                        .sorted(BY_OFFENCE_RANK)
                        .map(chargesTransformer::chargeOf)
                        .collect(Collectors.toList()));
    }

    public Optional<List<Charge>> chargesForOffenderIdAndBookingId(final Long offenderId, final Long bookingId) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderCharges()
                        .stream()
                        .sorted(BY_OFFENCE_RANK)
                        .map(chargesTransformer::chargeOf)
                        .collect(Collectors.toList()));
    }

}
