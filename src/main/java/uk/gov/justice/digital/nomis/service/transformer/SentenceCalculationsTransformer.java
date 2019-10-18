package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.SentenceCalculation;
import uk.gov.justice.digital.nomis.jpa.entity.LabelledTimestamp;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderSentCalculation;

import java.util.Optional;

@Component
public class SentenceCalculationsTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public SentenceCalculationsTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public SentenceCalculation sentenceCalculationOf(final OffenderSentCalculation osc) {
        return SentenceCalculation.builder()
                .apdCalculatedDate(typesTransformer.localDateTimeOf(osc.getApdCalculatedDate()))
                .apdOverridedDate(typesTransformer.localDateTimeOf(osc.getApdOverridedDate()))
                .ardCalculatedDate(typesTransformer.localDateTimeOf(osc.getArdCalculatedDate()))
                .ardOverridedDate(typesTransformer.localDateTimeOf(osc.getArdOverridedDate()))
                .bookingId(osc.getOffenderBooking().getOffenderBookId())
                .calcReasonCode(osc.getCalcReasonCode())
                .calculationDate(typesTransformer.localDateTimeOf(osc.getCalculationDate()))
                .comments(osc.getCommentText())
                .crdCalculatedDate(typesTransformer.localDateTimeOf(osc.getCrdCalculatedDate()))
                .crdOverridedDate(typesTransformer.localDateTimeOf(osc.getCrdOverridedDate()))
                .dprrdCalculatedDate(typesTransformer.localDateTimeOf(osc.getDprrdCalculatedDate()))
                .dprrdOverridedDate(typesTransformer.localDateTimeOf(osc.getDprrdOverridedDate()))
                .effectiveSentenceEndDate(typesTransformer.localDateTimeOf(osc.getEffectiveSentenceEndDate()))
                .effectiveSentenceLength(osc.getEffectiveSentenceLength())
                .ersedOverridedDate(typesTransformer.localDateTimeOf(osc.getErsedOverridedDate()))
                .etdCalculatedDate(typesTransformer.localDateTimeOf(osc.getEtdCalculatedDate()))
                .etdOverridedDate(typesTransformer.localDateTimeOf(osc.getEtdOverridedDate()))
                .hdcadCalculatedDate(typesTransformer.localDateTimeOf(osc.getHdcadCalculatedDate()))
                .hdcadOverridedDate(typesTransformer.localDateTimeOf(osc.getHdcadOverridedDate()))
                .hdcedCalculatedDate(typesTransformer.localDateTimeOf(osc.getHdcedCalculatedDate()))
                .hdcedOverridedDate(typesTransformer.localDateTimeOf(osc.getHdcedOverridedDate()))
                .hdcEligibleWf(typesTransformer.ynToBoolean(osc.getHdcEligibleWf()))
                .judiciallyImposedSentenceLength(osc.getJudiciallyImposedSentLength())
                .ledCalculatedDate(typesTransformer.localDateTimeOf(osc.getLedCalculatedDate()))
                .ledOverridedDate(typesTransformer.localDateTimeOf(osc.getLedOverridedDate()))
                .ltdCalculatedDate(typesTransformer.localDateTimeOf(osc.getLtdCalculatedDate()))
                .ltdOverridedDate(typesTransformer.localDateTimeOf(osc.getLtdOverridedDate()))
                .mtdCalculatedDate(typesTransformer.localDateTimeOf(osc.getMtdCalculatedDate()))
                .mtdOverridedDate(typesTransformer.localDateTimeOf(osc.getMtdOverridedDate()))
                .npdCalculatedDate(typesTransformer.localDateTimeOf(osc.getNpdCalculatedDate()))
                .npdOverridedDate(typesTransformer.localDateTimeOf(osc.getNpdOverridedDate()))
                .pedCalculatedDate(typesTransformer.localDateTimeOf(osc.getPedCalculatedDate()))
                .pedOverridedDate(typesTransformer.localDateTimeOf(osc.getPedOverridedDate()))
                .prrdCalculatedDate(typesTransformer.localDateTimeOf(osc.getPrrdCalculatedDate()))
                .prrdOverridedDate(typesTransformer.localDateTimeOf(osc.getPrrdOverridedDate()))
                .rotlOverridedDate(typesTransformer.localDateTimeOf(osc.getRotlOverridedDate()))
                .sedCalculatedDate(typesTransformer.localDateTimeOf(osc.getSedCalculatedDate()))
                .sedOverridedDate(typesTransformer.localDateTimeOf(osc.getSedOverridedDate()))
                .sentenceCalculationId(osc.getOffenderSentCalculationId())
                .staffId(osc.getStaffId())
                .tariffCalculatedDate(typesTransformer.localDateTimeOf(osc.getTariffCalculatedDate()))
                .tariffOverridedDate(typesTransformer.localDateTimeOf(osc.getTariffOverridedDate()))
                .tersedOverridedDate(typesTransformer.localDateTimeOf(osc.getTersedOverridedDate()))
                .tusedCalculatedDate(typesTransformer.localDateTimeOf(osc.getTusedCalculatedDate()))
                .tusedOverridedDate(typesTransformer.localDateTimeOf(osc.getTusedOverridedDate()))
                .workflowHistoryId(osc.getWorkflowHistoryId())
                .releaseDate(typesTransformer.localDateOf(osc.getDerivedReleaseDate()))
                .releaseType(Optional.ofNullable(osc.getDerivedReleaseDate()).map(LabelledTimestamp::getLabel).orElse(null))
                .confirmedReleaseDate(typesTransformer.localDateOf(osc.getConfirmedReleaseDate()))
                .confirmedReleaseType(Optional.ofNullable(osc.getConfirmedReleaseDate()).map(LabelledTimestamp::getLabel).orElse(null))
                .nonDtoReleaseDate(typesTransformer.localDateOf(osc.getNonDtoReleaseDate()))
                .nonDtoReleaseType(Optional.ofNullable(osc.getNonDtoReleaseDate()).map(LabelledTimestamp::getLabel).orElse(null))
                .midTermDate(typesTransformer.localDateOf(osc.getMidTermDate()))
                .midTermType(Optional.ofNullable(osc.getMidTermDate()).map(LabelledTimestamp::getLabel).orElse(null))
                .build();
    }

}
