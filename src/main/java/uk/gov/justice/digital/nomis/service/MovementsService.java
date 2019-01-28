package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.ExternalMovement;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;
import uk.gov.justice.digital.nomis.jpa.filters.MovementsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.ExternalMovementsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.MovementsTransformer;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MovementsService {



    private static final Comparator<OffenderExternalMovement> BY_MOVEMENT_DATE = Comparator
            .comparing(OffenderExternalMovement::getMovementDate)
            .thenComparing((OffenderExternalMovement::getMovementTime))
            .thenComparingLong((OffenderExternalMovement oem) -> oem.getId().getMovementSeq())
            .reversed();
    private final ExternalMovementsRepository externalMovementsRepository;
    private final OffenderRepository offenderRepository;
    private final MovementsTransformer movementsTransformer;

    @Autowired
    public MovementsService(ExternalMovementsRepository externalMovementsRepository, OffenderRepository offenderRepository, MovementsTransformer movementsTransformer) {
        this.externalMovementsRepository = externalMovementsRepository;
        this.offenderRepository = offenderRepository;
        this.movementsTransformer = movementsTransformer;
    }

    @Transactional
    public Page<ExternalMovement> getMovements(Pageable pageable,
                                               Optional<LocalDateTime> maybeFrom,
                                               Optional<LocalDateTime> maybeTo,
                                               Optional<Long> maybeBookingId) {
        MovementsFilter movementsFilter = MovementsFilter.builder()
                .from(maybeFrom)
                .to(maybeTo)
                .bookingId(maybeBookingId)
                .build();

        Page<OffenderExternalMovement> externalMovements = externalMovementsRepository.findAll(movementsFilter, pageable);

        List<ExternalMovement> movementList = externalMovements.getContent()
                .stream()
                .sorted(BY_MOVEMENT_DATE)
                .map(movementsTransformer::movementOf)
                .collect(Collectors.toList());

        return new PageImpl<>(movementList, pageable, externalMovements.getTotalElements());
    }

    @Transactional
    public Optional<List<ExternalMovement>> getOffenderMovements(Long offenderId) {
        Optional<List<uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement>> maybeOffenderMovements =
                offenderRepository.findById(offenderId)
                        .map(offender -> offender.getOffenderBookings().stream()
                                .map(OffenderBooking::getOffenderExternalMovements).
                                        flatMap(Collection::stream).
                                        collect(Collectors.toList()));

        return maybeOffenderMovements.map(externalMovements -> externalMovements
                .stream()
                .sorted(BY_MOVEMENT_DATE)
                .map(movementsTransformer::movementOf)
                .collect(Collectors.toList()));
    }

    public Optional<List<ExternalMovement>> movementsForOffenderIdAndBookingId(Long offenderId, Long bookingId) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderExternalMovements()
                        .stream()
                        .sorted(BY_MOVEMENT_DATE)
                        .map(movementsTransformer::movementOf)
                        .collect(Collectors.toList()));

    }
}
