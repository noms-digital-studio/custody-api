package uk.gov.justice.digital.nomis.jpa.entity;

import com.google.common.collect.ImmutableList;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@Entity
@ToString(exclude = "offenderBooking")
@Table(name = "OFFENDER_SENT_CALCULATIONS")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OffenderSentCalculation {
    @Id
    @Column(name = "OFFENDER_SENT_CALCULATION_ID")
    private Long offenderSentCalculationId;
    @JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID")
    @ManyToOne
    private OffenderBooking offenderBooking;
    @Column(name = "CALCULATION_DATE")
    private Timestamp calculationDate;
    @Column(name = "STAFF_ID")
    private Long staffId;
    @Column(name = "COMMENT_TEXT")
    private String commentText;
    @Column(name = "HDCED_CALCULATED_DATE")
    private Timestamp hdcedCalculatedDate;
    @Column(name = "HDCED_OVERRIDED_DATE")
    private Timestamp hdcedOverridedDate;
    @Column(name = "HDCAD_CALCULATED_DATE")
    private Timestamp hdcadCalculatedDate;
    @Column(name = "HDCAD_OVERRIDED_DATE")
    private Timestamp hdcadOverridedDate;
    @Column(name = "ETD_CALCULATED_DATE")
    private Timestamp etdCalculatedDate;
    @Column(name = "ETD_OVERRIDED_DATE")
    private Timestamp etdOverridedDate;
    @Column(name = "MTD_CALCULATED_DATE")
    private Timestamp mtdCalculatedDate;
    @Column(name = "MTD_OVERRIDED_DATE")
    private Timestamp mtdOverridedDate;
    @Column(name = "LTD_CALCULATED_DATE")
    private Timestamp ltdCalculatedDate;
    @Column(name = "LTD_OVERRIDED_DATE")
    private Timestamp ltdOverridedDate;
    @Column(name = "ARD_CALCULATED_DATE")
    private Timestamp ardCalculatedDate;
    @Column(name = "ARD_OVERRIDED_DATE")
    private Timestamp ardOverridedDate;
    @Column(name = "CRD_CALCULATED_DATE")
    private Timestamp crdCalculatedDate;
    @Column(name = "CRD_OVERRIDED_DATE")
    private Timestamp crdOverridedDate;
    @Column(name = "PED_CALCULATED_DATE")
    private Timestamp pedCalculatedDate;
    @Column(name = "PED_OVERRIDED_DATE")
    private Timestamp pedOverridedDate;
    @Column(name = "APD_CALCULATED_DATE")
    private Timestamp apdCalculatedDate;
    @Column(name = "APD_OVERRIDED_DATE")
    private Timestamp apdOverridedDate;
    @Column(name = "NPD_CALCULATED_DATE")
    private Timestamp npdCalculatedDate;
    @Column(name = "NPD_OVERRIDED_DATE")
    private Timestamp npdOverridedDate;
    @Column(name = "LED_CALCULATED_DATE")
    private Timestamp ledCalculatedDate;
    @Column(name = "LED_OVERRIDED_DATE")
    private Timestamp ledOverridedDate;
    @Column(name = "SED_CALCULATED_DATE")
    private Timestamp sedCalculatedDate;
    @Column(name = "SED_OVERRIDED_DATE")
    private Timestamp sedOverridedDate;
    @Column(name = "PRRD_CALCULATED_DATE")
    private Timestamp prrdCalculatedDate;
    @Column(name = "PRRD_OVERRIDED_DATE")
    private Timestamp prrdOverridedDate;
    @Column(name = "TARIFF_CALCULATED_DATE")
    private Timestamp tariffCalculatedDate;
    @Column(name = "TARIFF_OVERRIDED_DATE")
    private Timestamp tariffOverridedDate;
    @Column(name = "CALC_REASON_CODE")
    private String calcReasonCode;
    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;
    @Column(name = "CREATE_USER_ID")
    private String createUserId;
    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;
    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
    @Column(name = "RECORD_DATETIME")
    private Timestamp recordDatetime;
    @Column(name = "RECORD_USER_ID")
    private String recordUserId;
    @Column(name = "AUDIT_TIMESTAMP")
    private Timestamp auditTimestamp;
    @Column(name = "AUDIT_USER_ID")
    private String auditUserId;
    @Column(name = "AUDIT_MODULE_NAME")
    private String auditModuleName;
    @Column(name = "AUDIT_CLIENT_USER_ID")
    private String auditClientUserId;
    @Column(name = "AUDIT_CLIENT_IP_ADDRESS")
    private String auditClientIpAddress;
    @Column(name = "AUDIT_CLIENT_WORKSTATION_NAME")
    private String auditClientWorkstationName;
    @Column(name = "AUDIT_ADDITIONAL_INFO")
    private String auditAdditionalInfo;
    @Column(name = "EFFECTIVE_SENTENCE_END_DATE")
    private Timestamp effectiveSentenceEndDate;
    @Column(name = "HDC_ELIGIBLE_WF")
    private String hdcEligibleWf;
    @Column(name = "WORKFLOW_HISTORY_ID")
    private Long workflowHistoryId;
    @Column(name = "EFFECTIVE_SENTENCE_LENGTH")
    private String effectiveSentenceLength;
    @Column(name = "JUDICIALLY_IMPOSED_SENT_LENGTH")
    private String judiciallyImposedSentLength;
    @Column(name = "DPRRD_CALCULATED_DATE")
    private Timestamp dprrdCalculatedDate;
    @Column(name = "DPRRD_OVERRIDED_DATE")
    private Timestamp dprrdOverridedDate;
    @Column(name = "ERSED_OVERRIDED_DATE")
    private Timestamp ersedOverridedDate;
    @Column(name = "TERSED_OVERRIDED_DATE")
    private Timestamp tersedOverridedDate;
    @Column(name = "ROTL_OVERRIDED_DATE")
    private Timestamp rotlOverridedDate;
    @Column(name = "TUSED_CALCULATED_DATE")
    private Timestamp tusedCalculatedDate;
    @Column(name = "TUSED_OVERRIDED_DATE")
    private Timestamp tusedOverridedDate;


    @Transient
    private LabelledTimestamp derivedReleaseDate;
    @Transient
    private LabelledTimestamp confirmedReleaseDate;
    @Transient
    private LabelledTimestamp nonDtoReleaseDate;
    @Transient
    private LabelledTimestamp midTermDate;

    @PostLoad
    private void postLoadSignificantReleaseDates() {
        confirmedReleaseDate = calculateConfirmedReleaseDate().orElse(null);
        nonDtoReleaseDate = calculateNonDtoReleaseDate().orElse(null);
        midTermDate = calculateMidTermDate().orElse(null);
        derivedReleaseDate = calculateDerivedReleaseDate();
    }

    public LabelledTimestamp calculateDerivedReleaseDate() {

        final var maybeConfirmedReleaseDate = Optional.ofNullable(confirmedReleaseDate);

        if (maybeConfirmedReleaseDate.isPresent()) {
            return maybeConfirmedReleaseDate.get();
        }

        final var maybeActualParoleDate = Optional.ofNullable(apdOverridedDate);

        if (maybeActualParoleDate.isPresent()) {
            return new LabelledTimestamp("APD", maybeActualParoleDate.get());
        }

        final var maybeHdcActualDate = Optional.ofNullable(hdcadOverridedDate);

        if (maybeHdcActualDate.isPresent()) {
            return new LabelledTimestamp("HDCAD", maybeHdcActualDate.get());
        }

        final var maybeNonDtoReleaseDate = Optional.ofNullable(nonDtoReleaseDate);
        final var maybeMidTermDate = Optional.ofNullable(midTermDate);

        return greaterOf(maybeNonDtoReleaseDate, maybeMidTermDate).orElse(null);
    }

    public Optional<LabelledTimestamp> greaterOf(final Optional<LabelledTimestamp> maybeDate1, final Optional<LabelledTimestamp> maybeDate2) {

        return Stream.of(maybeDate1, maybeDate2)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(LabelledTimestamp::compareTo);
    }

    public Optional<LabelledTimestamp> calculateConfirmedReleaseDate() {
        return Optional.ofNullable(offenderBooking.getOffenderReleaseDetails())
                .map(ord -> firstNonNullDateOf(ord.getReleaseDate(), ord.getAutoReleaseDate())
                        .map(t -> LabelledTimestamp.builder()
                                .label(Optional.ofNullable(ord.getMovementReason())
                                        .map(mr -> "REL-" + mr.getMovementReasonCode())
                                        .orElse("REL"))
                                .timestamp(t)
                                .build())
                        .orElse(null));
    }

    public Optional<Timestamp> firstNonNullDateOf(final Timestamp a, final Timestamp b) {
        return Optional.ofNullable(Optional.ofNullable(a).orElse(b));
    }

    public Optional<LabelledTimestamp> calculateNonDtoReleaseDate() {
        final List<Optional<LabelledTimestamp>> dates = ImmutableList.<Optional<LabelledTimestamp>>builder()
                .add(labelledTimestampOf("ARD", firstNonNullDateOf(ardOverridedDate, ardCalculatedDate)))
                .add(labelledTimestampOf("CRD", firstNonNullDateOf(crdOverridedDate, crdCalculatedDate)))
                .add(labelledTimestampOf("NPD", firstNonNullDateOf(npdOverridedDate, npdCalculatedDate)))
                .add(labelledTimestampOf("PRRD", firstNonNullDateOf(prrdOverridedDate, prrdCalculatedDate)))
                .build();

        return dates
                .stream()
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .min(LabelledTimestamp::compareTo);

    }

    private Optional<LabelledTimestamp> labelledTimestampOf(final String label, final Optional<Timestamp> timestamp) {
        return timestamp.map(t -> new LabelledTimestamp(label, t));
    }

    public Optional<LabelledTimestamp> calculateMidTermDate() {
        return labelledTimestampOf("MTD", firstNonNullDateOf(mtdOverridedDate, mtdCalculatedDate));
    }


}
