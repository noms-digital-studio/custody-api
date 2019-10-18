package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.AssessmentType;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.OffenderAssessment;
import uk.gov.justice.digital.nomis.jpa.entity.Assessment;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class AssessmentsTransformer {

    private static final String SUP_LVL_TYPE = "SUP_LVL_TYPE";

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public AssessmentsTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public OffenderAssessment assessmentOf(final uk.gov.justice.digital.nomis.jpa.entity.OffenderAssessment offenderAssessment) {
        return OffenderAssessment.builder()
                .approvedSupLevelType(offenderAssessment.getApprovedSupLevelType())
                .assessCommitteeCode(offenderAssessment.getAssessCommitteCode())
                .assessmentCreateLocationId(offenderAssessment.getAssessmentCreateLocation())
                .assessmentDate(typesTransformer.localDateOf(offenderAssessment.getAssessmentDate()))
                .assessmentSequence(offenderAssessment.getId().getAssessmentSeq())
                .assessmentType(assessmentTypeOf(offenderAssessment.getAssessment()))
                .assessorStaffId(offenderAssessment.getAssessorStaffId())
                .assessStaffId(offenderAssessment.getAssessStaffId())
                .assessStatus(offenderAssessment.getAssessStatus())
                .calcSupLevelType(offenderAssessment.getCalcSupLevelType())
                .assessComments(offenderAssessment.getAssessCommentText())
                .committeeComments(offenderAssessment.getCommitteCommentText())
                .createDateTime(typesTransformer.localDateTimeOf(offenderAssessment.getCreateDatetime()))
                .createUserId(offenderAssessment.getCreateUserId())
                .creationDate(typesTransformer.localDateOf(offenderAssessment.getCreationDate()))
                .evaluationDate(typesTransformer.localDateOf(offenderAssessment.getEvaluationDate()))
                .evaluationResultCode(offenderAssessment.getEvaluationResultCode())
                .nextReviewDate(typesTransformer.localDateOf(offenderAssessment.getNextReviewDate()))
                .bookingId(offenderAssessment.getId().getOffenderBooking().getOffenderBookId())
                .offenderId(offenderAssessment.getId().getOffenderBooking().getOffenderId())
                .overrideComments(offenderAssessment.getOverrideCommentText())
                .overridedSupLevelType(offenderAssessment.getOverridedSupLevelType())
                .overrideReason(offenderAssessment.getOverrideReason())
                .overrideReasonText(offenderAssessment.getOverrideReasonText())
                .overrideStaffId(offenderAssessment.getOverrideStaffId())
                .overrideUserId(offenderAssessment.getOverrideUserId())
                .placeAgencyLocationId(offenderAssessment.getPlaceAgyLocId())
                .reviewCommitteeCode(offenderAssessment.getReviewCommitteCode())
                .reviewPlacementText(offenderAssessment.getReviewPlacementText())
                .reviewSupLevelText(offenderAssessment.getReviewSupLevelText())
                .reviewSupLevelType(getSecurityCategoryOf(offenderAssessment))
                .reviewPlaceAgencyLocationId(offenderAssessment.getReviewPlaceAgyLocId())
                .score(offenderAssessment.getScore())
                .build();

    }

    private KeyValue getSecurityCategoryOf(final uk.gov.justice.digital.nomis.jpa.entity.OffenderAssessment oa) {
        return Optional.ofNullable(oa.getReviewSupLevelType() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(oa.getReviewSupLevelType())
                        .domain(SUP_LVL_TYPE)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

    private AssessmentType assessmentTypeOf(final Assessment assessment) {
        return Optional.ofNullable(assessment)
                .map(a -> AssessmentType.builder()
                        .assessmentClass(a.getAssessmentClass())
                        .assessmentCode(a.getAssessmentCode())
                        .assessmentId(a.getAssessmentId())
                        .cellSharingAlertFlag(typesTransformer.ynToBoolean(a.getCellSharingAlertFlag()))
                        .determineSupLevelFlag(typesTransformer.ynToBoolean(a.getDetermineSupLevelFlag()))
                        .build())
                .orElse(null);
    }
}
