package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OFFENDER_EXTERNAL_MOVEMENTS")
public class OffenderExternalMovement {

    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        @ManyToOne
        @JoinColumn(name = "OFFENDER_BOOK_ID")
        private OffenderBooking offenderBooking;

        @Column(name = "MOVEMENT_SEQ")
        private Long movementSeq;
    }

    @EmbeddedId
    private Pk id;

    @Column(name = "MOVEMENT_SEQ", insertable = false, updatable = false)
    private Long movementSeq;
    @Column(name = "OFFENDER_BOOK_ID", insertable = false, updatable = false)
    private Long offenderBookId;
    @Column(name = "MOVEMENT_DATE")
    private Timestamp movementDate;
    @Column(name = "MOVEMENT_TIME")
    private Timestamp movementTime;
    @Column(name = "INTERNAL_SCHEDULE_TYPE")
    private String internalScheduleType;
    @Column(name = "INTERNAL_SCHEDULE_REASON_CODE")
    private String internalScheduleReasonCode;
    @Column(name = "DIRECTION_CODE")
    private String directionCode;
    @Column(name = "ARREST_AGENCY_LOC_ID")
    private String arrestAgencyLocId;
    @Column(name = "TO_PROV_STAT_CODE")
    private String toProvStatCode;
    @Column(name = "ESCORT_CODE")
    private String escortCode;
    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;
    @Column(name = "ESCORT_TEXT")
    private String escortText;
    @Column(name = "COMMENT_TEXT")
    private String commentText;
    @Column(name = "REPORTING_DATE")
    private Timestamp reportingDate;
    @Column(name = "TO_CITY")
    private String toCity;
    @Column(name = "FROM_CITY")
    private String fromCity;
    @Column(name = "REPORTING_TIME")
    private Timestamp reportingTime;
    @Column(name = "EVENT_ID")
    private Integer eventId;
    @Column(name = "PARENT_EVENT_ID")
    private Integer parentEventId;
    @Column(name = "TO_COUNTRY_CODE")
    private String toCountryCode;
    @Column(name = "OJ_LOCATION_CODE")
    private String ojLocationCode;
    @Column(name = "APPLICATION_DATE")
    private Timestamp applicationDate;
    @Column(name = "APPLICATION_TIME")
    private Timestamp applicationTime;
    @Column(name = "FROM_AGY_LOC_ID")
    private String fromAgyLocId;
    @Column(name = "TO_AGY_LOC_ID")
    private String toAgyLocId;
    @Column(name = "FROM_ADDRESS_ID")
    private Long fromAddressId;
    @Column(name = "TO_ADDRESS_ID")
    private Long toAddressId;

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

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "MOVEMENT_REASON_CODE", referencedColumnName = "MOVEMENT_REASON_CODE"),
            @JoinColumn(name = "MOVEMENT_TYPE", referencedColumnName = "MOVEMENT_TYPE")
    })
    private MovementReason movementReason;

    @OneToOne
    @JoinColumn(name = "TO_AGY_LOC_ID", insertable = false, updatable = false)
    private AgencyLocation toAgencyLocation;

    @OneToOne
    @JoinColumn(name = "FROM_AGY_LOC_ID", insertable = false, updatable = false)
    private AgencyLocation fromAgencyLocation;

}
