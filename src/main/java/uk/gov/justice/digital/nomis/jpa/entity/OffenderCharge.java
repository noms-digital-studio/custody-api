package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@ToString(exclude = "offenderBooking")
@Entity
@Table(name = "OFFENDER_CHARGES")
public class OffenderCharge {
    @Id
    @Column(name = "OFFENDER_CHARGE_ID")
    private Long offenderChargeId;
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    @OneToOne
    private OffenderBooking offenderBooking;
    @Column(name = "NO_OF_OFFENCES")
    private Integer noOfOffences;
    @Column(name = "OFFENCE_DATE")
    private Timestamp offenceDate;
    @Column(name = "OFFENCE_RANGE_DATE")
    private Timestamp offenceRangeDate;
    @Column(name = "PLEA_CODE")
    private String pleaCode;
    @Column(name = "PROPERTY_VALUE")
    private BigDecimal propertyValue;
    @Column(name = "TOTAL_PROPERTY_VALUE")
    private BigDecimal totalPropertyValue;
    @Column(name = "CJIT_OFFENCE_CODE_1")
    private String cjitOffenceCode1;
    @Column(name = "CJIT_OFFENCE_CODE_2")
    private String cjitOffenceCode2;
    @Column(name = "CJIT_OFFENCE_CODE_3")
    private String cjitOffenceCode3;
    @Column(name = "CHARGE_STATUS")
    private String chargeStatus;
    @Column(name = "CREATE_USER_ID")
    private String createUserId;
    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;
    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;
    @Column(name = "RESULT_CODE_1_INDICATOR")
    private String resultCode1Indicator;
    @Column(name = "RESULT_CODE_2_INDICATOR")
    private String resultCode2Indicator;
    @Column(name = "MOST_SERIOUS_FLAG")
    private String mostSeriousFlag;
    @Column(name = "CHARGE_SEQ")
    private Integer chargeSeq;
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
    @Column(name = "LIDS_OFFENCE_NUMBER")
    private Integer lidsOffenceNumber;
    @OneToOne
    @JoinColumn(name = "CASE_ID", referencedColumnName = "CASE_ID", nullable = false)
    private OffenderCase offenderCase;
    @Column(name = "OFFENCE_CODE")
    private String offenceCode;
    @Column(name = "STATUTE_CODE")
    private String statuteCode;
    @Column(name = "RESULT_CODE_1")
    private String resultCode1;
    @Column(name = "RESULT_CODE_2")
    private String resultCode2;
    @OneToOne
    @JoinColumn(name = "ORDER_ID")
    private Order order;

    @OneToMany
    @BatchSize(size = 1000)
    @JoinTable(name = "OFFENDER_SENTENCE_CHARGES",
            joinColumns = {@JoinColumn(name = "OFFENDER_CHARGE_ID", referencedColumnName = "OFFENDER_CHARGE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "OFFENDER_BOOK_ID", referencedColumnName = "OFFENDER_BOOK_ID"), @JoinColumn(name = "SENTENCE_SEQ", referencedColumnName = "SENTENCE_SEQ")},
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private List<OffenderSentence> sentences;


}
