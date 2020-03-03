package uk.gov.justice.digital.nomis.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;

import java.util.Optional;

@Repository
public interface OffenderExternalMovementsRepository extends JpaRepository<OffenderExternalMovement, Long> {

    Optional<OffenderExternalMovement> findByOffenderBookIdAndMovementSeq(Long movementSeq, Long bookingId);

}
