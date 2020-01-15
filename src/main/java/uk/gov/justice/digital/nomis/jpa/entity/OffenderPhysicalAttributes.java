package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "OFFENDER_PHYSICAL_ATTRIBUTES")
@IdClass(OffenderPhysicalAttributesPK.class)
public class OffenderPhysicalAttributes {
    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long offenderBookId;
    @Id
    @Column(name = "ATTRIBUTE_SEQ")
    private Long attributeSeq;
    @Column(name = "HEIGHT_FT")
    private Integer heightFt;
    @Column(name = "HEIGHT_IN")
    private Integer heightIn;
    @Column(name = "HEIGHT_CM")
    private Integer heightCm;
    @Column(name = "WEIGHT_LBS")
    private Integer weightLbs;
    @Column(name = "WEIGHT_KG")
    private Integer weightKg;
    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;
    @Column(name = "CREATE_USER_ID")
    private String createUserId;
    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;
    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
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

}
