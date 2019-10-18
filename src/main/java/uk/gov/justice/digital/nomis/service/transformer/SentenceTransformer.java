package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Sentence;
import uk.gov.justice.digital.nomis.api.SentenceCalculationType;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderSentence;
import uk.gov.justice.digital.nomis.jpa.entity.SentenceCalcTypePK;
import uk.gov.justice.digital.nomis.jpa.repository.SentenceCalcTypeRepository;

@Component
public class SentenceTransformer {

    private final TypesTransformer typesTransformer;
    private final SentenceCalcTypeRepository sentenceCalcTypeRepository;

    @Autowired
    public SentenceTransformer(final TypesTransformer typesTransformer, final SentenceCalcTypeRepository sentenceCalcTypeRepository) {
        this.typesTransformer = typesTransformer;
        this.sentenceCalcTypeRepository = sentenceCalcTypeRepository;
    }

    public Sentence sentenceOf(final OffenderSentence offenderSentence) {

        final var offenderBooking = offenderSentence.getOffenderBooking();

        return Sentence.builder()
                .bookingId(offenderBooking.getOffenderBookId())
                .offenderId(offenderBooking.getOffenderId())
                .sentenceSequenceNumber(offenderSentence.getSentenceSeq())
                .aggregateAdjustDays(offenderSentence.getAggregateAdjustDays())
                .aggregateTerm(offenderSentence.getAggregateTerm())
                .apdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getApdCalculatedDate()))
                .ardCalculatedDate(typesTransformer.localDateOf(offenderSentence.getArdCalculatedDate()))
                .breachLevel(offenderSentence.getBreachLevel())
                .comments(offenderSentence.getCommentText())
                .consecToSentenceSeq(offenderSentence.getConsecToSentenceSeq())
                .counts(offenderSentence.getCounts())
                .crdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getCrdCalculatedDate()))
                .createdAt(typesTransformer.localDateTimeOf(offenderSentence.getCreateDatetime()))
                .dischargeDate(typesTransformer.localDateOf(offenderSentence.getDischargeDate()))
                .dprrdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getDprrdCalculatedDate()))
                .endDate(typesTransformer.localDateOf(offenderSentence.getEndDate()))
                .etdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getEtdCalculatedDate()))
                .extendedDays(offenderSentence.getExtendedDays())
                .fineAmount(offenderSentence.getFineAmount())
                .hdcedCalculatedDate(typesTransformer.localDateOf(offenderSentence.getHdcedCalculatedDate()))
                .hdcExclusion(typesTransformer.ynToBoolean(offenderSentence.getHdcExclusionFlag()))
                .hdcExclusionReason(offenderSentence.getHdcExclusionReason())
                .ledCalculatedDate(typesTransformer.localDateOf(offenderSentence.getLedCalculatedDate()))
                .ltdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getLtdCalculatedDate()))
                .mtdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getMtdCalculatedDate()))
                .npdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getNpdCalculatedDate()))
                .pedCalculatedDate(typesTransformer.localDateOf(offenderSentence.getPedCalculatedDate()))
                .numberOfUnexcusedAbsences(offenderSentence.getNoOfUnexcusedAbsence())
                .prrdCalculatedDate(typesTransformer.localDateOf(offenderSentence.getPrrdCalculatedDate()))
                .revokedDate(typesTransformer.localDateOf(offenderSentence.getRevokedDate()))
                .sedCalculatedDate(typesTransformer.localDateOf(offenderSentence.getSedCalculatedDate()))
                .sentenceCategory(offenderSentence.getSentenceCategory())
                .sentenceCalculationType(sentenceCalculationTypeOf(offenderSentence))
                .sentenceLevel(offenderSentence.getSentenceLevel())
                .isActive(typesTransformer.isActiveOf(offenderSentence.getSentenceStatus()))
                .sentenceText(offenderSentence.getSentenceText())
                .sled2CalcDate(typesTransformer.localDateOf(offenderSentence.getSled2Calc()))
                .startDate(typesTransformer.localDateOf(offenderSentence.getStartDate()))
                .startDate2Calc(typesTransformer.localDateOf(offenderSentence.getStartDate2Calc()))
                .tariffCalculatedDate(typesTransformer.localDateOf(offenderSentence.getTariffCalculatedDate()))
                .terminationDate(typesTransformer.localDateOf(offenderSentence.getTerminationDate()))
                .terminationReason(offenderSentence.getTerminationReason())
                .tusedCalculatedDate(typesTransformer.localDateOf(offenderSentence.getTusedCalculatedDate()))
                .build();
    }

    private SentenceCalculationType sentenceCalculationTypeOf(final OffenderSentence os) {
        return os.getSentenceCategory() != null && os.getSentenceCalcType() != null ?
        sentenceCalcTypeRepository.findById(SentenceCalcTypePK.builder()
                .sentenceCalcType(os.getSentenceCalcType())
                .sentenceCategory(os.getSentenceCategory())
                .build())
                .map(sct -> SentenceCalculationType.builder()
                        .active(typesTransformer.isActiveOf(sct.getActiveFlag()))
                        .description(sct.getDescription())
                        .expiryDate(typesTransformer.localDateTimeOf(sct.getExpiryDate()))
                        .functionType(sct.getFunctionType())
                        .ProgramMethod(sct.getProgramMethod())
                        .sentenceCategory(sct.getSentenceCategory())
                        .sentenceType(sct.getSentenceType())
                        .sentenceCalculationType(sct.getSentenceCalcType())
                        .build())
                .orElse(null) : null;
    }
}
