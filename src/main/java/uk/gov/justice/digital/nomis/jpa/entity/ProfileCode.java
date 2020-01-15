package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "PROFILE_CODES")
@IdClass(ProfileCodePK.class)
public class ProfileCode {
    @Id
    @Column(name = "PROFILE_TYPE")
    private String profileType;
    @Id
    @Column(name = "PROFILE_CODE")
    private String profileCode;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "LIST_SEQ")
    private Integer listSeq;
    @Column(name = "UPDATE_ALLOWED_FLAG")
    private String updateAllowedFlag;
    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;
    @Column(name = "EXPIRY_DATE")
    private Timestamp expiryDate;
    @Column(name = "USER_ID")
    private String userId;
    private Timestamp modifiedDate;

    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;
    @Column(name = "CREATE_USER_ID")
    private String createUserId;
    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;
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
