package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.ReleaseDetails;
import uk.gov.justice.digital.nomis.jpa.entity.MovementReason;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderReleaseDetails;

import java.util.Optional;

@Component
public class ReleaseDetailsTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public ReleaseDetailsTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public ReleaseDetails releaseDetailsOf(final OffenderReleaseDetails ord) {
        final var maybeMovementReason = Optional.ofNullable(ord.getMovementReason());

        return ReleaseDetails.builder()
                .approvedReleaseDate(typesTransformer.localDateOf(ord.getApprovedReleaseDate()))
                .autoReleaseDate(typesTransformer.localDateOf(ord.getAutoReleaseDate()))
                .bookingId(ord.getOffenderBookId())
                .comments(ord.getCommentText())
                .dtoApprovedDate(typesTransformer.localDateOf(ord.getDtoApprovedDate()))
                .dtoMidTermDate(typesTransformer.localDateOf(ord.getDtoMidTermDate()))
                .eventId(ord.getEventId())
                .eventStatus(ord.getEventStatus())
                .movementReasonCode(maybeMovementReason.map(MovementReason::getMovementReasonCode).orElse(null))
                .movementReasonDescription(maybeMovementReason.map(MovementReason::getDescription).orElse(null))
                .movementReasonType(maybeMovementReason.map(MovementReason::getMovementType).orElse(null))
                .releaseDate(typesTransformer.localDateOf(ord.getReleaseDate()))
                .verified(typesTransformer.ynToBoolean(ord.getVerifiedFlag()))
                .build();
    }
}
