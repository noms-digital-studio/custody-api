package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "PERSONS")
public class Person {
    @Id
    @Column(name = "PERSON_ID")
    private Long personId;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "MIDDLE_NAME")
    private String middleName;
    @Column(name = "BIRTHDATE")
    private Timestamp birthdate;
    @Column(name = "OCCUPATION_CODE")
    private String occupationCode;
    @Column(name = "CRIMINAL_HISTORY_TEXT")
    private String criminalHistoryText;
    @Column(name = "NAME_TYPE")
    private String nameType;
    @Column(name = "ALIAS_PERSON_ID")
    private Long aliasPersonId;
    @Column(name = "ROOT_PERSON_ID")
    private Long rootPersonId;
    @Column(name = "LANGUAGE_CODE")
    private String languageCode;
    @Column(name = "COMPREHEND_ENGLISH_FLAG")
    private String comprehendEnglishFlag;
    @Column(name = "SEX")
    private String sex;
    @Column(name = "BIRTH_PLACE")
    private String birthPlace;
    @Column(name = "EMPLOYER")
    private String employer;
    @Column(name = "PROFILE_CODE")
    private String profileCode;
    @Column(name = "INTERPRETER_REQUIRED")
    private String interpreterRequired;
    @Column(name = "PRIMARY_LANGUAGE_CODE")
    private String primaryLanguageCode;
    @Column(name = "MEMO_TEXT")
    private String memoText;
    @Column(name = "SUSPENDED_FLAG")
    private String suspendedFlag;
    @Column(name = "MARITAL_STATUS")
    private String maritalStatus;
    @Column(name = "CITIZENSHIP")
    private String citizenship;
    @Column(name = "DECEASED_DATE")
    private Timestamp deceasedDate;
    @Column(name = "CORONER_NUMBER")
    private String coronerNumber;
    @Column(name = "ATTENTION")
    private String attention;
    @Column(name = "CARE_OF")
    private String careOf;
    @Column(name = "SUSPENDED_DATE")
    private Timestamp suspendedDate;
    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;
    @Column(name = "CREATE_USER_ID")
    private String createUserId;
    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;
    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
    @Column(name = "NAME_SEQUENCE")
    private String nameSequence;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "STAFF_FLAG")
    private String staffFlag;
    @Column(name = "REMITTER_FLAG")
    private String remitterFlag;
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
    @Column(name = "KEEP_BIOMETRICS")
    private String keepBiometrics;

}
