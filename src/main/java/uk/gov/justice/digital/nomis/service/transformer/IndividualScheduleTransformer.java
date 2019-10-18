package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.IndividualSchedule;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderIndSchedule;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCode;

import java.util.Optional;

@Component
public class IndividualScheduleTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public IndividualScheduleTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public IndividualSchedule individualScheduleOf(final OffenderIndSchedule ois, final ReferenceCode escort) {
        return IndividualSchedule.builder()
                .eventId(ois.getEventId())
                .eventType(ois.getEventType())
                .eventSubType(ois.getEventSubType())
                .eventStatus(ois.getEventStatus())
                .eventStartDateTime(typesTransformer.localDateTimeOf(ois.getEventDate(), ois.getStartTime()))
                .eventEndDateTime(typesTransformer.localDateTimeOf(ois.getEventDate(), ois.getEndTime()))
                .bookingId(ois.getOffenderBookId())
                .escortType(referenceOf(escort))
                .returnDateTime(typesTransformer.localDateTimeOf(ois.getReturnDate(), ois.getReturnTime()))
                .comments(ois.getCommentText())
                .build();
    }

    private KeyValue referenceOf(final ReferenceCode referenceCode) {
        return Optional.ofNullable(referenceCode)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

}
