package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.RehabDecision;
import uk.gov.justice.digital.nomis.api.RehabProvider;
import uk.gov.justice.digital.nomis.api.RehabilitationDecisionProvider;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderRehabDecision;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderRehabProvider;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.entity.RehabilitationProvider;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class RehabDecisionTransformer {

    private static final String ALL_DEC = "ALL_DEC";

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public RehabDecisionTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public RehabDecision rehabDecisionOf(final OffenderRehabDecision offenderRehabDecision) {
        return Optional.ofNullable(offenderRehabDecision).map(
                ord -> RehabDecision.builder()
                        .active(typesTransformer.ynToBoolean(ord.getActiveFlag()))
                        .comments(ord.getCommentText())
                        .decision(getDecisionCodeOf(ord))
                        .decisionDate(typesTransformer.localDateOf(ord.getDecisionDate()))
                        .bookingId(ord.getOffenderBookId())
                        .offenderRehabDecisionId(ord.getOffenderRehabDecisionId())
                        .recordDateTime(typesTransformer.localDateTimeOf(ord.getRecordDatetime()))
                        .staffId(ord.getStaffId())
                        .build())
                .orElse(null);
    }

    public RehabProvider rehabProviderOf(final OffenderRehabProvider offenderRehabProvider) {
        return Optional.ofNullable(offenderRehabProvider).map(
                orp -> RehabProvider.builder()
                        .offenderRehabProviderId(orp.getOffenderRehabProviderId())
                        .active(typesTransformer.ynToBoolean(orp.getActiveFlag()))
                        .comments(orp.getCommentText())
                        .providerCode(orp.getProviderCode())
                        .provider(rehabilitationDecisionProviderOf(orp.getRehabilitationProvider()))
                        .build())
                .orElse(null);
    }

    public RehabilitationDecisionProvider rehabilitationDecisionProviderOf(final RehabilitationProvider rehabilitationProvider) {
        return Optional.ofNullable(rehabilitationProvider).map(
                orp -> RehabilitationDecisionProvider.builder()
                        .active(typesTransformer.ynToBoolean(orp.getActiveFlag()))
                        .description(orp.getDescription())
                        .code(orp.getProviderCode())
                        .type(orp.getProvideType())
                        .Sequence(orp.getListSeq())
                        .employmentDate(typesTransformer.localDateOf(orp.getEmploymentDate()))
                        .build())
                .orElse(null);
    }

    private KeyValue getDecisionCodeOf(final OffenderRehabDecision em) {
        return Optional.ofNullable(em.getDecisionCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(em.getDecisionCode())
                        .domain(ALL_DEC)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }
}
