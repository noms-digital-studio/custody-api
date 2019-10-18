package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.IepLevel;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.OffenderIepLevel;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class IEPTransformer {

    private static final String IEP_LEVEL = "IEP_LEVEL";

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public IEPTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public OffenderIepLevel offenderIepLevelOf(final uk.gov.justice.digital.nomis.jpa.entity.OffenderIepLevel offenderIepLevel) {
        return Optional.ofNullable(offenderIepLevel)
                .map(iep -> OffenderIepLevel.builder()
                        .bookingId(iep.getOffenderBookId())
                        .comments(iep.getCommentText())
                        .iepDateTime(typesTransformer.localDateTimeOf(iep.getIepDate(), iep.getIepTime()))
                        .iepLevel(iepLevelOf(iep.getIepLevelThing()))
                        .iepLevelSeq(iep.getIepLevelSeq())
                        .build())
                .orElse(null);
    }

    private IepLevel iepLevelOf(final uk.gov.justice.digital.nomis.jpa.entity.IepLevel iep) {
        return Optional.ofNullable(iep)
                .map(iepLevel -> IepLevel.builder()
                        .active(typesTransformer.ynToBoolean(iep.getActiveFlag()))
                        .agencyLocationId(iep.getAgyLocId())
                        .convictedSpendLimit(iep.getConvictedSpendLimit())
                        .convictedTransferLimit(iep.getConvictedTransferLimit())
                        .expiryDate(typesTransformer.localDateTimeOf(iep.getExpiryDate()))
                        .iepLevel(getIepLevelOf(iep))
                        .isDefault(typesTransformer.ynToBoolean(iep.getDefaultFlag()))
                        .remandSpendLimit(iep.getRemandSpendLimit())
                        .remandTransferLimit(iep.getRemandTransferLimit())
                        .build())
                .orElse(null);
    }

    private KeyValue getIepLevelOf(final uk.gov.justice.digital.nomis.jpa.entity.IepLevel iep) {
        return Optional.ofNullable(iep.getIepLevel() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(iep.getIepLevel())
                        .domain(IEP_LEVEL)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

}
