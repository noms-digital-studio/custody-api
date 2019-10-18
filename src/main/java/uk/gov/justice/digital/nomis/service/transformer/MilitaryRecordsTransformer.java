package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.MilitaryRecord;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderMilitaryRecord;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class MilitaryRecordsTransformer {
    private static final String MLTY_BRANCH = "MLTY_BRANCH";
    private static final String MLTY_DISCP = "MLTY_DISCP";
    private static final String MLTY_DSCHRG = "MLTY_DSCHRG";
    private static final String MLTY_RANK = "MLTY_RANK";
    private static final String MLTY_WZONE = "MLTY_WZONE";

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public MilitaryRecordsTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public MilitaryRecord militaryRecordOf(final OffenderMilitaryRecord omr) {
        return MilitaryRecord.builder()
                .bookingId(omr.getId().getOffenderBooking().getOffenderBookId())
                .militarySeq(omr.getId().getMilitarySeq())
                .description(omr.getDescription())
                .startDate(typesTransformer.localDateOf(omr.getStartDate()))
                .endDate(typesTransformer.localDateOf(omr.getEndDate()))
                .unitNumber(omr.getUnitNumber())
                .serviceNumber(omr.getServiceNumber())
                .selectiveServices(typesTransformer.ynToBoolean(omr.getSelectiveServicesFlag()))
                .militaryBranchCode(getReferenceDataOf(omr.getMilitaryBranchCode(), MLTY_BRANCH))
                .militaryRankCode(getReferenceDataOf(omr.getMilitaryRankCode(), MLTY_RANK))
                .militaryDischargeCode(getReferenceDataOf(omr.getMilitaryDischargeCode(), MLTY_DSCHRG))
                .enlistmentLocation(omr.getEnlistmentLocation())
                .dischargeLocation(omr.getDischargeLocation())
                .disciplinaryActionCode(getReferenceDataOf(omr.getDisciplinaryActionCode(), MLTY_DISCP))
                .warZoneCode(getReferenceDataOf(omr.getWarZoneCode(), MLTY_WZONE))
                .build();
    }

    private KeyValue getReferenceDataOf(final String code, final String domain) {
        return Optional.ofNullable(code != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(code)
                        .domain(domain)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }
}
