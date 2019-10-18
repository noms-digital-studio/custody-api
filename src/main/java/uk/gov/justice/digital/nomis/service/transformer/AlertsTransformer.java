package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Alert;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderAlert;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class AlertsTransformer {
    private static final String ALERT_CODE = "ALERT_CODE";

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public AlertsTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public Alert alertOf(final OffenderAlert offenderAlert) {
        return Optional.ofNullable(offenderAlert).map(
                alert -> Alert.builder()
                        .alertCode(alertCodeOf(alert))
                        .alertDate(typesTransformer.localDateOf(alert.getAlertDate()))
                        .createdDate(typesTransformer.localDateOf(alert.getCreateDate()))
                        .alertSeq(alert.getAlertSeq())
                        .alertStatus(alert.getAlertStatus())
                        .alertType(alert.getAlertType())
                        .authorizePersonText(alert.getAuthorizePersonText())
                        .bookingId(alert.getOffenderBookId())
                        .caseloadId(alert.getCaseloadId())
                        .caseloadType(alert.getCaseloadType())
                        .comments(alert.getCommentText())
                        .expiryDate(typesTransformer.localDateOf(alert.getExpiryDate()))
                        .offenderId(alert.getRootOffenderId())
                        .verified(typesTransformer.ynToBoolean(alert.getVerifiedFlag()))
                        .build()).orElse(null);
    }

    private KeyValue alertCodeOf(final OffenderAlert alert) {
        return Optional.ofNullable(alert.getAlertCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(alert.getAlertCode())
                        .domain(ALERT_CODE)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }
}

