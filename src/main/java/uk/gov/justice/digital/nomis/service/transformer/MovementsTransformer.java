package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.ExternalMovement;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class MovementsTransformer {

    private static final String ESCORT = "ESCORT";

    private final TypesTransformer typesTransformer;
    private final ReferenceDataTransformer referenceDataTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public MovementsTransformer(final TypesTransformer typesTransformer, final ReferenceDataTransformer referenceDataTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceDataTransformer = referenceDataTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }


    public ExternalMovement movementOf(final OffenderExternalMovement em) {

        final var movementReason = em.getMovementReason();

        return ExternalMovement.builder()
                .active(typesTransformer.ynToBoolean(em.getActiveFlag()))
                .comments(em.getCommentText())
                .fromAddressId(em.getFromAddressId())
                .toAddressId(em.getToAddressId())
                .fromAgencyLocation(referenceDataTransformer.agencyLocationOf(em.getFromAgencyLocation()))
                .toAgencyLocation(referenceDataTransformer.agencyLocationOf(em.getToAgencyLocation()))
                .movementDirection(em.getDirectionCode())
                .movementReasonCode(movementReason.getMovementReasonCode())
                .movementReasonDescription(movementReason.getDescription())
                .movementTypeCode(movementReason.getMovementType())
                .bookingId(em.getId().getOffenderBooking().getOffenderBookId())
                .offenderId(em.getId().getOffenderBooking().getRootOffenderId())
                .sequenceNumber(em.getId().getMovementSeq())
                .movementDateTime(typesTransformer.localDateTimeOf(em.getMovementDate(), em.getMovementTime()))
                .unemploymentPay(typesTransformer.ynToBoolean(movementReason.getUnemploymentPay()))
                .escapeRecapture(typesTransformer.ynToBoolean(movementReason.getEscRecapFlag()))
                .inMovementType(movementReason.getInMovementType())
                .inMovementReasonCode(movementReason.getMovementReasonCode())
                .transportation(typesTransformer.ynToBoolean(movementReason.getTransportationFlag()))
                .internalScheduleType(em.getInternalScheduleType())
                .internalScheduleReasonCode(em.getInternalScheduleReasonCode())
                .arrestAgencyLocationId(em.getArrestAgencyLocId())
                .toProvStatCode(em.getToProvStatCode())
                .escortCode(getEscortCodeOf(em))
                .escortText(em.getEscortText())
                .reportingDateTime(typesTransformer.localDateTimeOf(em.getReportingDate(), em.getReportingTime()))
                .applicationDateTime(typesTransformer.localDateTimeOf(em.getApplicationDate(), em.getApplicationTime()))
                .toCityCode(em.getToCity())
                .fromCityCode(em.getFromCity())
                .toCountryCode(em.getToCountryCode())
                .ojLocationCode(em.getOjLocationCode())
                .build();
    }

    private KeyValue getEscortCodeOf(final OffenderExternalMovement em) {
        return Optional.ofNullable(em.getEscortCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(em.getEscortCode())
                        .domain(ESCORT)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

}
