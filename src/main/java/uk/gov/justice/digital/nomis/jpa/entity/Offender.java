package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.Data;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "OFFENDERS")
public class Offender {
    @Id
    @Column(name = "OFFENDER_ID")
    private Long offenderId;
    @Column(name = "OFFENDER_NAME_SEQ")
    private Long offenderNameSeq;
    @Column(name = "ID_SOURCE_CODE")
    private String idSourceCode;
    @Column(name = "LAST_NAME")
    private String lastName;
    @Column(name = "NAME_TYPE")
    private String nameType;
    @Column(name = "FIRST_NAME")
    private String firstName;
    @Column(name = "MIDDLE_NAME")
    private String middleName;
    @Column(name = "BIRTH_DATE")
    private Timestamp birthDate;
    @Column(name = "SEX_CODE")
    private String sexCode;
    @Column(name = "SUFFIX")
    private String suffix;
    @Column(name = "LAST_NAME_SOUNDEX")
    private String lastNameSoundex;
    @Column(name = "BIRTH_PLACE")
    private String birthPlace;
    @Column(name = "BIRTH_COUNTRY_CODE")
    private String birthCountryCode;
    @Column(name = "CREATE_DATE")
    private Timestamp createDate;
    @Column(name = "LAST_NAME_KEY")
    private String lastNameKey;
    @Column(name = "FIRST_NAME_KEY")
    private String firstNameKey;
    @Column(name = "MIDDLE_NAME_KEY")
    private String middleNameKey;
    @Column(name = "OFFENDER_ID_DISPLAY")
    private String offenderIdDisplay;
    @Column(name = "ROOT_OFFENDER_ID", insertable = false, updatable = false)
    private Long rootOffenderId;
    @Column(name = "CASELOAD_TYPE")
    private String caseloadType;
    @Column(name = "MODIFY_USER_ID")
    private String modifyUserId;
    @Column(name = "MODIFY_DATETIME")
    private Timestamp modifyDatetime;
    @Column(name = "ALIAS_NAME_TYPE")
    private String aliasNameType;
    @Column(name = "PARENT_OFFENDER_ID")
    private Long parentOffenderId;
    @Column(name = "UNIQUE_OBLIGATION_FLAG")
    private String uniqueObligationFlag;
    @Column(name = "SUSPENDED_FLAG")
    private String suspendedFlag;
    @Column(name = "SUSPENDED_DATE")
    private Timestamp suspendedDate;
    @Column(name = "RACE_CODE")
    private String raceCode;
    @Column(name = "REMARK_CODE")
    private String remarkCode;
    @Column(name = "ADD_INFO_CODE")
    private String addInfoCode;
    @Column(name = "BIRTH_COUNTY")
    private String birthCounty;
    @Column(name = "BIRTH_STATE")
    private String birthState;
    @Column(name = "MIDDLE_NAME_2")
    private String middleName2;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "AGE")
    private Long age;
    @Column(name = "CREATE_USER_ID")
    private String createUserId;
    @Column(name = "LAST_NAME_ALPHA_KEY")
    private String lastNameAlphaKey;
    @Column(name = "CREATE_DATETIME")
    private Timestamp createDatetime;
    @Column(name = "NAME_SEQUENCE")
    private String nameSequence;
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
    //@Column(name = "SINGLE_OFFENDER_IDENTITY_ID")
    //private String singleOffenderIdentityId;
    @Column(name = "ALIAS_OFFENDER_ID")
    private Long aliasOffenderId;

    @OneToMany
    @BatchSize(size = 1000)
    @JoinColumn(name = "ROOT_OFFENDER_ID", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = true)
    private List<OffenderBooking> offenderBookings;

    @OneToMany
    @BatchSize(size = 1000)
    @JoinColumn(name = "ROOT_OFFENDER_ID", referencedColumnName = "OFFENDER_ID")
    private List<Offender> offenderAliases;

    @OneToMany
    @BatchSize(size = 1000)
    @JoinColumn(name = "OFFENDER_ID")
    private List<OffenderIdentifier> offenderIdentifiers;

    public List<Offender> getOffenderAliases() {
        return offenderAliases.stream().filter(alias -> !alias.getOffenderId().equals(this.offenderId)).collect(Collectors.toList());
    }

    @OneToMany
    @BatchSize(size = 1000)
    @JoinColumn(name = "ROOT_OFFENDER_ID", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = true)
    private List<OffenderEvent> offenderEvents;



}
