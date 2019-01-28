package uk.gov.justice.digital.nomis.service.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import oracle.sql.Datum;
import oracle.sql.RAW;
import oracle.sql.STRUCT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.OffenderEvent;
import uk.gov.justice.digital.nomis.jpa.entity.XtagEventNonJpa;
import uk.gov.justice.digital.nomis.xtag.Xtag;
import uk.gov.justice.digital.nomis.xtag.XtagContent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class OffenderEventsTransformer {

    private final TypesTransformer typesTransformer;
    private final ObjectMapper objectMapper;

    @Autowired
    public OffenderEventsTransformer(TypesTransformer typesTransformer, @Qualifier("globalObjectMapper") ObjectMapper objectMapper) {
        this.typesTransformer = typesTransformer;
        this.objectMapper = objectMapper;
    }

    public static LocalDateTime xtagFudgedTimestampOf(LocalDateTime xtagEnqueueTime) {
        final ZoneId london = ZoneId.of("Europe/London");
        if (london.getRules().isDaylightSavings(xtagEnqueueTime.atZone(london).toInstant())) {
            return xtagEnqueueTime;
        }
        return xtagEnqueueTime.minusHours(1L);
    }

    public OffenderEvent offenderEventOf(uk.gov.justice.digital.nomis.jpa.entity.OffenderEvent offenderEvent) {
        return Optional.ofNullable(offenderEvent)
                .map(event -> OffenderEvent.builder()
                        .eventId(event.getEventId().toString())
                        .eventDatetime(typesTransformer.localDateTimeOf(event.getEventTimestamp()))
                        .eventType(withEventTypeOf(event))
                        .rootOffenderId(event.getRootOffenderId())
                        .offenderIdDisplay(event.getOffenderIdDisplay())
                        .agencyLocationId(event.getAgencyLocId())
                        .build()).orElse(null);
    }

    public OffenderEvent offenderEventOf(XtagEventNonJpa xtagEvent) {
        final STRUCT s = xtagEvent.getUserData();
        try {
            return getOffenderEvent(s, xtagEvent.getEnqTime());
        } catch (SQLException e) {
            log.error("Failed to convert STRUCT {} to OffenderEvent: {}", s, e.getMessage());
            return null;
        }
    }

    private OffenderEvent getOffenderEvent(STRUCT s, Timestamp enqTime) throws SQLException {
        final Optional<Datum> maybeStruct = Arrays.asList(s.getOracleAttributes()).stream().filter(a -> a instanceof STRUCT).findFirst();
        final Optional<Datum> maybeRaw = Arrays.asList(s.getOracleAttributes()).stream().filter(a -> a instanceof RAW).findFirst();

        final Optional<String> maybeType = maybeStruct.flatMap(d -> {
            final STRUCT d1 = (STRUCT) d;
            try {
                return Optional.ofNullable(d1.getAttributes()[1].toString());
            } catch (SQLException e) {
                log.error("Failed to derive Type from STRUCT {} : {}", d, e.getMessage());
                return Optional.empty();
            }
        });

        final Optional<Map<String, String>> maybeMap = maybeRaw.flatMap(d -> {
            try {
                return Optional.ofNullable(deserialize(d.getBytes()));
            } catch (IOException | ClassNotFoundException e) {
                log.error("Failed to derive Map from Datum {} : {}", d, e.getMessage());
                return Optional.empty();
            }
        });

        return offenderEventOf(Xtag.builder()
                .eventType(maybeType.orElse("?"))
                .nomisTimestamp(xtagFudgedTimestampOf(enqTime.toLocalDateTime()))
                .content(maybeMap.map(this::xtagContentOf).orElse(null))
                .build());
    }

    public XtagContent xtagContentOf(Map<String, String> map) {
        try {
            String stringValue = objectMapper.writeValueAsString(map);
            return objectMapper.readValue(stringValue, XtagContent.class);
        } catch (IOException e) {
            log.error("Failed to deserialize Map {} into XtagContent: {}", map.toString(), e.getMessage());
            return null;
        }
    }

    private Map<String, String> deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        final Object o = new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        return (Map<String, String>) o;
    }

    private OffenderEvent offenderEventOf(Xtag xtag) {
        if (xtag == null) {
            log.warn("Null xtag");
            return null;
        }

        switch (xtag.getEventType()) {
            case "P8_RESULT":
                return riskScoreEventOf(xtag);
            case "A3_RESULT":
                return offenderSanctionEventOf(xtag);
            case "P1_RESULT":
            case "BOOK_UPD_OASYS":
                return bookingNumberEventOf(xtag);
            case "OFF_HEALTH_PROB_INS":
                return maternityStatusInsertedEventOf(xtag);
            case "OFF_HEALTH_PROB_UPD":
                return maternityStatusUpdatedEventOf(xtag);
            case "OFF_RECEP_OASYS":
                return offenderMovementReceptionEventOf(xtag);
            case "OFF_DISCH_OASYS":
                return offenderMovementDischargeEventOf(xtag);
            case "M1_RESULT":
            case "M1_UPD_RESULT":
                return externalMovementRecordEventOf(xtag);
            case "OFF_UPD_OASYS":
                return !Strings.isNullOrEmpty(xtag.getContent().getP_offender_book_id()) ?
                        offenderBookingChangedEventOf(xtag) :
                        offenderDetailsChangedEventOf(xtag);
            case "ADDR_USG_INS":
                return addressUsageInsertedEventOf(xtag);
            case "ADDR_USG_UPD":
                return xtag.getContent().getP_address_deleted().equals("Y") ?
                        addressUsageDeletedEventOf(xtag) :
                        addressUsageUpdatedEventOf(xtag);
            case "P4_RESULT":
                return offenderAliasChangedEventOf(xtag);
            case "P2_RESULT":
                return offenderUpdatedEventOf(xtag);
            case "OFF_BKB_INS":
                return offenderBookingInsertedEventOf(xtag);
            case "OFF_BKB_UPD":
                return offenderBookingReassignedEventOf(xtag);
            case "OFF_CONT_PER_INS":
                return contactPersonInsertedEventOf(xtag);
            case "OFF_CONT_PER_UPD":
                return xtag.getContent().getP_address_deleted().equals("Y") ?
                        contactPersonDeletedEventOf(xtag) :
                        contactPersonUpdatedEventOf(xtag);
            case "OFF_EDUCATION_INS":
                return educationLevelInsertedEventOf(xtag);
            case "OFF_EDUCATION_UPD":
                return educationLevelUpdatedEventOf(xtag);
            case "OFF_EDUCATION_DEL":
                return educationLevelDeletedEventOf(xtag);
            case "P3_RESULT":
                return (xtag.getContent().getP_identifier_type().equals("NOMISP3")) ?
                        offenderBookingInsertedEventOf(xtag) :
                        !Strings.isNullOrEmpty(xtag.getContent().getP_identifier_value()) ?
                                offenderIdentifierInsertedEventOf(xtag) :
                                offenderIdentifierDeletedEventOf(xtag);
            case "S1_RESULT":
                return !Strings.isNullOrEmpty(xtag.getContent().getP_imprison_status_seq()) ?
                        imprisonmentStatusChangedEventOf(xtag) :
                        !Strings.isNullOrEmpty(xtag.getContent().getP_assessment_seq()) ?
                                assessmentChangedEventOf(xtag) :
                                !Strings.isNullOrEmpty(xtag.getContent().getP_alert_date()) ?
                                        alertUpdatedEventOf(xtag) :
                                        alertInsertedEventOf(xtag);
            case "OFF_IMP_STAT_OASYS":
                return imprisonmentStatusChangedEventOf(xtag);
            case "OFF_PROF_DETAIL_INS":
                return offenderProfileDetailInsertedEventOf(xtag);
            case "OFF_PROF_DETAIL_UPD":
                return offenderProfileUpdatedEventOf(xtag);
            case "S2_RESULT":
                return sentenceCaclulationDateChangedEventOf(xtag);
            case "A2_CALLBACK":
                return hearingDateChangedEventOf(xtag);
            case "A2_RESULT":
                return "Y".equals(xtag.getContent().getP_delete_flag()) ?
                        hearingResultDeletedEventOf(xtag) :
                        hearingResultChangedEventOf(xtag);
            case "PHONES_INS":
                return phoneInsertedEventOf(xtag);
            case "PHONES_UPD":
                return phoneUpdatedEventOf(xtag);
            case "PHONES_DEL":
                return phoneDeletedEventOf(xtag);
            case "OFF_EMPLOYMENTS_INS":
                return offenderEmploymentInsertedEventOf(xtag);
            case "OFF_EMPLOYMENTS_UPD":
                return offenderEmploymentUpdatedEventOf(xtag);
            case "OFF_EMPLOYMENTS_DEL":
                return offenderEmploymentDeletedEventOf(xtag);
            case "D5_RESULT":
                return hdcConditionChanged(xtag);
            case "D4_RESULT":
                return hdcFineInserted(xtag);
            case "ADDR_INS":
                return personAddressInserted(xtag);
            case "ADDR_UPD":
                if (xtag.getContent().getP_owner_class().equals("PER")) {
                    return xtag.getContent().getP_address_deleted().equals("N") ?
                            personAddressUpdatedEventOf(xtag) :
                            personAddressDeletedEventOf(xtag);
                }
                if (xtag.getContent().getP_owner_class().equals("OFF")) {
                    return xtag.getContent().getP_address_deleted().equals("N") ?
                            offenderAddressUpdatedEventOf(xtag) :
                            offenderAddressDeletedEventOf(xtag);
                }
                return xtag.getContent().getP_address_deleted().equals("N") ?
                        addressUpdatedEventOf(xtag) :
                        addressDeletedEventOf(xtag);
            case "S1_DEL_RESULT":
                return alertDeletedEventOf(xtag);
            case "OFF_SENT_OASYS":
                return sentenceCaclulationDateChangedEventOf(xtag);
            case "C_NOTIFICATION":
                return courtSentenceChangedEventOf(xtag);
            case "IEDT_OUT":
                return offenderTransferOutOfLidsEventOf(xtag);
            default:
                return OffenderEvent.builder()
                        .eventType(xtag.getEventType())
                        .eventDatetime(xtag.getNomisTimestamp())
                        .offenderId(longOf(xtag.getContent().getP_offender_id()))
                        .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                        .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                        .build();
        }
    }

    private OffenderEvent offenderTransferOutOfLidsEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_TRANSFER-OUT_OF_LIDS")
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent courtSentenceChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("COURT_SENTENCE-CHANGED")
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent alertDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ALERT-DELETED")
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .alertDateTime(localDateTimeOf(xtag.getContent().getP_old_alert_date(), xtag.getContent().getP_old_alert_time()))
                .alertType(xtag.getContent().getP_alert_type())
                .alertCode(xtag.getContent().getP_alert_code())
                .expiryDateTime(localDateTimeOf(xtag.getContent().getP_expiry_date(), xtag.getContent().getP_expiry_time()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent personAddressUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("PERSON_ADDRESS-UPDATED")
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderAddressUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_ADDRESS-UPDATED")
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent addressUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ADDRESS-UPDATED")
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent personAddressDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("PERSON_ADDRESS-DELETED")
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderAddressDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_ADDRESS-DELETED")
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent addressDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ADDRESS-DELETED")
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent personAddressInserted(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("PERSON_ADDRESS-INSERTED")
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .addressEndDate(localDateOf(xtag.getContent().getP_address_end_date()))
                .primaryAddressFlag(xtag.getContent().getP_primary_addr_flag())
                .mailAddressFlag(xtag.getContent().getP_mail_addr_flag())
                .personId(longOf(xtag.getContent().getP_person_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent hdcFineInserted(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("HDC_FINE-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .sentenceSeq(longOf(xtag.getContent().getP_sentence_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent hdcConditionChanged(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("HDC_CONDITION-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .sentenceSeq(longOf(xtag.getContent().getP_sentence_seq()))
                .conditionCode(xtag.getContent().getP_condition_code())
                .offenderSentenceConditionId(longOf(xtag.getContent().getP_offender_sent_calculation_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderEmploymentInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_EMPLOYMENT-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderEmploymentUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_EMPLOYMENT-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderEmploymentDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_EMPLOYMENT-DELETED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent phoneInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("PHONE-INSERTED")
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent phoneUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("PHONE-UPDATED")
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent phoneDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("PHONE-DELETED")
                .ownerId(longOf(xtag.getContent().getP_owner_id()))
                .ownerClass(xtag.getContent().getP_owner_class())
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent hearingResultChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("HEARING_RESULT-CHANGED")
                .oicHearingId(longOf(xtag.getContent().getP_oic_hearing_id()))
                .resultSeq(longOf(xtag.getContent().getP_result_seq()))
                .agencyIncidentId(longOf(xtag.getContent().getP_agency_incident_id()))
                .chargeSeq(longOf(xtag.getContent().getP_charge_seq()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent hearingResultDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("HEARING_RESULT-DELETED")
                .oicHearingId(longOf(xtag.getContent().getP_oic_hearing_id()))
                .resultSeq(longOf(xtag.getContent().getP_result_seq()))
                .agencyIncidentId(longOf(xtag.getContent().getP_agency_incident_id()))
                .chargeSeq(longOf(xtag.getContent().getP_charge_seq()))
                .oicOffenceId(longOf(xtag.getContent().getP_oic_offence_id()))
                .pleaFindingCode(longOf(xtag.getContent().getP_plea_finding_code()))
                .findingCode(longOf(xtag.getContent().getP_finding_code()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent hearingDateChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("HEARING_DATE-CHANGED")
                .oicHearingId(longOf(xtag.getContent().getP_oic_hearing_id()))
                .eventDatetime(xtag.getNomisTimestamp())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent sentenceCaclulationDateChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("SENTENCE_CALCULATION_DATES-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .sentenceCalculationId(longOf(xtag.getContent().getP_offender_sent_calculation_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderProfileUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_PROFILE_DETAILS-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderProfileDetailInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_PROFILE_DETAILS-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent alertInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ALERT-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .alertSeq(longOf(xtag.getContent().getP_alert_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent alertUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ALERT-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .alertSeq(longOf(xtag.getContent().getP_alert_seq()))
                .alertDateTime(localDateTimeOf(xtag.getContent().getP_old_alert_date(), xtag.getContent().getP_old_alert_time()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent assessmentChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ASSESSMENT-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .assessmentSeq(longOf(xtag.getContent().getP_assessment_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent imprisonmentStatusChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("IMPRISONMENT_STATUS-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .imprisonmentStatusSeq(longOf(xtag.getContent().getP_imprison_status_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderIdentifierInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_IDENTIFIER-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .identifierType(xtag.getContent().getP_identifier_type())
                .identifierValue(xtag.getContent().getP_identifier_value())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderIdentifierDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_IDENTIFIER-DELETED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .identifierType(xtag.getContent().getP_identifier_type())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent educationLevelInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("EDUCATION_LEVEL-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent educationLevelUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("EDUCATION_LEVEL-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent educationLevelDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("EDUCATION_LEVEL-DELETED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent contactPersonInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("CONTACT_PERSON-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .personId(longOf(xtag.getContent().getP_person_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent contactPersonUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("CONTACT_PERSON-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .personId(longOf(xtag.getContent().getP_person_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent contactPersonDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("CONTACT_PERSON-DELETED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .personId(longOf(xtag.getContent().getP_person_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderAliasChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_ALIAS-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .aliasOffenderId(longOf(xtag.getContent().getP_alias_offender_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent addressUsageInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ADDRESS_USAGE-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .addressUsage(xtag.getContent().getP_address_usage())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent addressUsageUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ADDRESS_USAGE-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .addressUsage(xtag.getContent().getP_address_usage())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent addressUsageDeletedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("ADDRESS_USAGE-DELETED")
                .eventDatetime(xtag.getNomisTimestamp())
                .addressId(longOf(xtag.getContent().getP_address_id()))
                .addressUsage(xtag.getContent().getP_address_usage())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderDetailsChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_DETAILS-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderBookingInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_BOOKING-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .identifierType(xtag.getContent().getP_identifier_type())
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderBookingChangedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_BOOKING-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .rootOffenderId(longOf(xtag.getContent().getP_root_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderBookingReassignedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_BOOKING-REASSIGNED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .previousOffenderId(longOf(xtag.getContent().getP_old_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent externalMovementRecordEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType(externalMovementEventOf(xtag))
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
                .movementDateTime(localDateTimeOf(xtag.getContent().getP_movement_date(), xtag.getContent().getP_movement_time()))
                .movementType(xtag.getContent().getP_movement_type())
                .movementReasonCode(xtag.getContent().getP_movement_reason_code())
                .directionCode(xtag.getContent().getP_direction_code())
                .escortCode(xtag.getContent().getP_escort_code())
                .fromAgencyLocationId(longOf(xtag.getContent().getP_from_agy_loc_id()))
                .toAgencyLocationId(longOf(xtag.getContent().getP_to_agy_loc_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private String externalMovementEventOf(Xtag xtag) {
        String del = xtag.getContent().getP_record_deleted();
        switch (del) {
            case "N":
                return "EXTERNAL_MOVEMENT_RECORD-INSERTED";
            case "Y":
                return "EXTERNAL_MOVEMENT_RECORD-DELETED";
            default:
                return Strings.isNullOrEmpty(del) ?
                        "" :
                        "EXTERNAL_MOVEMENT_RECORD-UPDATED";
        }
    }

    private OffenderEvent offenderMovementDischargeEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_MOVEMENT-DISCHARGE")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderMovementReceptionEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_MOVEMENT-RECEPTION")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .movementSeq(longOf(xtag.getContent().getP_movement_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent maternityStatusInsertedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("MATERNITY_STATUS-INSERTED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent maternityStatusUpdatedEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("MATERNITY_STATUS-UPDATED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent riskScoreEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("RISK_SCORE-" + (xtag.getContent().getP_delete_flag().equals("Y") ? "CHANGED" : "DELETED"))
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .riskPredictorId(longOf(xtag.getContent().getP_offender_risk_predictor_id()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent offenderSanctionEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("OFFENDER_SANCTION-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .sanctionSeq(longOf(xtag.getContent().getP_sanction_seq()))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private OffenderEvent bookingNumberEventOf(Xtag xtag) {
        return OffenderEvent.builder()
                .eventType("BOOKING_NUMBER-CHANGED")
                .eventDatetime(xtag.getNomisTimestamp())
                .offenderId(longOf(xtag.getContent().getP_offender_id()))
                .bookingId(longOf(xtag.getContent().getP_offender_book_id()))
                .bookingNumber(xtag.getContent().getP_new_prison_num())
                .previousBookingNumber(Optional.ofNullable(xtag.getContent().getP_old_prison_num())
                        .orElse(Optional.ofNullable(xtag.getContent().getP_old_prision_num())
                                .orElse(xtag.getContent().getP_old_prison_number())))
                .nomisEventType(xtag.getEventType())
                .build();
    }

    private String withEventTypeOf(uk.gov.justice.digital.nomis.jpa.entity.OffenderEvent event) {
        if (event.getEventType().equalsIgnoreCase("CASE_NOTE")) {
            final String eventData1 = event.getEventData1();
            try {
                JsonNode json = new ObjectMapper().readTree(eventData1);

                return String.format("%s-%s", json.get("case_note").get("type").get("code").asText(), json.get("case_note").get("sub_type").get("code").asText());
            } catch (IOException e) {
                log.error("Could not deserialize {} into JsonNode: {}", eventData1, e.getMessage());
            }
        }

        return event.getEventType();
    }

    private Long longOf(String num) {
        return Optional.ofNullable(num).map(Long::valueOf).orElse(null);
    }

    private LocalDate localDateOf(String date) {
        final String pattern = "yyyy-MM-dd hh:mm:ss";
        try {
            return Optional.ofNullable(date)
                    .map(d -> LocalDateTime.parse(d, DateTimeFormatter.ofPattern(pattern)).toLocalDate())
                    .orElse(null);
        } catch (DateTimeParseException dtpe) {
            log.error("Unable to parse {} into a LocalDate using pattern {}", date, pattern);
        }
        return null;
    }

    private LocalDateTime localDateTimeOf(String date, String time) {
        return Optional.ofNullable(date)
                .map(d -> Optional.ofNullable(time)
                        .map(t -> typesTransformer.localDateTimeOf(Timestamp.valueOf(d), Timestamp.valueOf(t)))
                        .orElse(localDateOf(date).atStartOfDay()))
                .orElse(null);
    }
}
