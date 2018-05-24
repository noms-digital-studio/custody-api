package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Charge;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCharge;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderChargesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.ChargesTransformer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChargesService {

    private final OffenderChargesRepository offenderChargesRepository;
    private final OffenderRepository offenderRepository;
    private final ChargesTransformer chargesTransformer;

    @Autowired
    public ChargesService(final OffenderChargesRepository offenderChargesRepository,
                          final OffenderRepository offenderRepository, ChargesTransformer chargesTransformer) {
        this.offenderChargesRepository = offenderChargesRepository;
        this.offenderRepository = offenderRepository;
        this.chargesTransformer = chargesTransformer;
    }

    @Transactional
    public Page<Charge> getCharges(Pageable pageable) {

        Page<OffenderCharge> offenderCharges = offenderChargesRepository.findAll(pageable);

        List<Charge> chargesList = offenderCharges.getContent()
                .stream()
                .map(chargesTransformer::chargeOf)
                .collect(Collectors.toList());

        return new PageImpl<>(chargesList, pageable, offenderCharges.getTotalElements());
    }

    public Optional<List<Charge>> chargesForOffenderId(Long offenderId) {

        Optional<List<OffenderCharge>> maybeOffenderCharges = Optional.ofNullable(offenderRepository.findOne(offenderId))
                .map(offender ->
                        offender.getOffenderBookings()
                                .stream()
                                .map(OffenderBooking::getOffenderCharges)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));

        return maybeOffenderCharges.map(offenderCharges ->
                offenderCharges.stream()
                        .map(chargesTransformer::chargeOf)
                        .sorted(byOffenceRank())
                        .collect(Collectors.toList()));
    }

    public Optional<List<Charge>> chargesForOffenderIdAndBookingId(Long offenderId, Long bookingId) {
        Optional<Offender> maybeOffender = Optional.ofNullable(offenderRepository.findOne(offenderId));

        if (!maybeOffender.isPresent()) {
            return Optional.empty();
        }

        Optional<OffenderBooking> maybeOffenderBooking = maybeOffender.get().getOffenderBookings()
                .stream()
                .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                .findFirst();

        return maybeOffenderBooking.map(ob -> ob.getOffenderCharges()
                .stream()
                .map(chargesTransformer::chargeOf)
                .sorted(byOffenceRank())
                .collect(Collectors.toList()));
    }

    private Comparator<Charge> byOffenceRank() {
        return Comparator
                .comparing(Charge::getMostSeriousCharge)
                .thenComparingLong(Charge::getOffenceSeverityRanking).reversed()
                .thenComparingLong(Charge::getChargeId).reversed();
    }
}