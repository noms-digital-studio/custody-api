package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.DiaryDetail;
import uk.gov.justice.digital.nomis.jpa.entity.*;

import java.util.Optional;

@Component
public class DiaryDetailTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public DiaryDetailTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public DiaryDetail diaryDetailOf(final CourtEvent courtEvent) {
        return DiaryDetail.builder()
                .bookingId(courtEvent.getOffenderBookId())
                .movementDateTime(typesTransformer.localDateTimeOf(courtEvent.getEventDate(), courtEvent.getStartTime()))
                .movementReasonCode(courtEvent.getCourtEventType())
                .comments(courtEvent.getCommentText())
                .build();
    }

    public DiaryDetail diaryDetailOf(final OffenderIndSchedule offenderIndSchedule, final ReferenceCode escort) {
        return DiaryDetail.builder()
                .bookingId(offenderIndSchedule.getOffenderBookId())
                .movementDateTime(typesTransformer.localDateTimeOf(offenderIndSchedule.getEventDate(), offenderIndSchedule.getStartTime()))
                .movementReasonCode(offenderIndSchedule.getEventSubType())
                .comments(offenderIndSchedule.getCommentText())
                .escortType(Optional.ofNullable(escort).map(ReferenceCode::getDescription).orElse(null))
                .build();
    }

    public DiaryDetail diaryDetailOf(final OffenderReleaseDetails offenderReleaseDetails) {
        final var maybeMovementReason = Optional.ofNullable(offenderReleaseDetails.getMovementReason());

        return DiaryDetail.builder()
                .bookingId(offenderReleaseDetails.getOffenderBookId())
                .movementDateTime(typesTransformer.localDateTimeOf(offenderReleaseDetails.getReleaseDate()))
                .movementReasonCode(maybeMovementReason.map(MovementReason::getMovementReasonCode).orElse(null))
                .movementReasonDescription(maybeMovementReason.map(MovementReason::getDescription).orElse(null))
                .movementReasonType(maybeMovementReason.map(MovementReason::getMovementType).orElse(null))
                .comments(offenderReleaseDetails.getCommentText())
                .build();
    }
}
