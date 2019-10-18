package uk.gov.justice.digital.nomis.service.transformer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderImage;

@Component
@Slf4j
public class OffenderImageTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public OffenderImageTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public uk.gov.justice.digital.nomis.api.OffenderImage offenderImageMetaDataOf(final OffenderImage offenderImage) {
        return uk.gov.justice.digital.nomis.api.OffenderImage.builder()
            .activeFlag(typesTransformer.ynToBoolean(offenderImage.getActiveFlag()))
            .bookingId(offenderImage.getOffenderBookingId())
            .captureDateTime(typesTransformer.localDateTimeOf(offenderImage.getCaptureDateTime()))
            .createDatetime(typesTransformer.localDateTimeOf(offenderImage.getCreateDatetime()))
            .imageObjectId(offenderImage.getImageObjectId())
            .imageObjectSeq(offenderImage.getImageObjectSeq())
            .imageObjectType(imageObjectTypeOf(offenderImage.getImageObjectType()))
            .imageViewType(imageViewTypeOf(offenderImage.getImageViewType()))
            .modifyDatetime(typesTransformer.localDateTimeOf(offenderImage.getModifyDatetime()))
            .offenderImageId(offenderImage.getOffenderImageId())
            .orientationType(offenderImage.getOrientationType())
            .build();
    }

    private String imageObjectTypeOf(final String imageObjectType) {
        switch(imageObjectType) {
            case "OIC" : return "INCIDENT";
            case "OFF_IDM" : return "IDENTIFYING_MARK";
            case "OFF_BKG" : return "BOOKING";
        }
        return imageObjectType;
    }

    private String imageViewTypeOf(final String imageViewType) {
        switch(imageViewType) {
            case "OIC" : return "INCIDENT";
            case "FACE" : return "FACE";
            case "TAT" : return "TATTOO";
            case "MARK" : return "MARK";
            case "SCAR" : return "SCAR";
            case "OTH" : return "OTHER";
        }

        return imageViewType;
    }


    public byte[] thumbnailOf(final OffenderImage offenderImage) {
        return offenderImage.getThumbnailImage();
    }
}
