package uk.gov.justice.digital.nomis.service.transformer;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Booking;
import uk.gov.justice.digital.nomis.api.Identifier;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.OffenderAlias;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderIdentifier;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OffenderTransformer {

    private static final String ETHNICITY = "ETHNICITY";
    private static final String SEX = "SEX";

    private final TypesTransformer typesTransformer;
    private final ReferenceDataTransformer referenceDataTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;
    private final MovementsTransformer movementsTransformer;

    private static final Comparator<OffenderExternalMovement> BY_MOVEMENT_DATE = Comparator
            .comparing(OffenderExternalMovement::getMovementDate)
            .thenComparing((OffenderExternalMovement::getMovementTime))
            .thenComparingLong((OffenderExternalMovement oem) -> oem.getId().getMovementSeq())
            .reversed();
    private static final Comparator<OffenderBooking> BY_BOOKING_SEQUENCE = Comparator
            .comparing(OffenderBooking::getBookingSeq)
            .thenComparing(OffenderBooking::getBookingBeginDate, Comparator.reverseOrder());

    @Autowired
    public OffenderTransformer(TypesTransformer typesTransformer, ReferenceDataTransformer referenceDataTransformer, ReferenceCodesRepository referenceCodesRepository, MovementsTransformer movementsTransformer) {
        this.typesTransformer = typesTransformer;
        this.referenceDataTransformer = referenceDataTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
        this.movementsTransformer = movementsTransformer;
    }

    private List<OffenderAlias> aliasesOf(List<Offender> offenderList) {
        return Optional.ofNullable(offenderList)
                .map(offenders -> offenders
                        .stream()
                        .map(offender -> aliasOf(offender))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public OffenderAlias aliasOf(Offender offender) {
        return OffenderAlias.builder()
                .offenderId(offender.getOffenderId())
                .firstName(offender.getFirstName())
                .middleNames(combinedMiddlenamesOf(offender))
                .surname(offender.getLastName())
                .dateOfBirth(offender.getBirthDate().toLocalDateTime().toLocalDate())
                .identifiers(identifiersOf(offender.getOffenderIdentifiers()))
                .nomsId(offender.getOffenderIdDisplay())
                .gender(genderOf(offender))
                .ethnicity(ethnicityOf(offender))
                .build();
    }

    private List<Identifier> identifiersOf(List<OffenderIdentifier> offenderIdentifiers) {
        return Optional.ofNullable(offenderIdentifiers)
                .map(identifiers -> identifiers
                        .stream()
                        .sorted(Comparator.comparing(OffenderIdentifier::getOffenderIdSeq).reversed())
                        .map(identifier -> Identifier.builder()
                                .identifier(identifier.getIdentifier())
                                .identifierType(identifier.getIdentifierType())
                                .sequenceNumber(identifier.getOffenderIdSeq())
                                .createdDateTime(Optional.ofNullable(identifier.getCreateDatetime()).map(Timestamp::toLocalDateTime).orElse(null))
                                .build())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private List<Booking> bookingsOf(List<OffenderBooking> offenderBookings) {
        return offenderBookings.stream()
                .sorted(BY_BOOKING_SEQUENCE)
                .map(this::bookingOf)
                .collect(Collectors.toList());
    }

    private Booking bookingOf(OffenderBooking booking) {
        return Booking.builder()
                .bookingSequence(booking.getBookingSeq())
                .startDate(booking.getBookingBeginDate().toLocalDateTime().toLocalDate())
                .endDate(Optional.ofNullable(booking.getBookingEndDate()).map(end -> end.toLocalDateTime().toLocalDate()).orElse(null))
                .activeFlag(typesTransformer.ynToBoolean(booking.getActiveFlag()))
                .agencyLocation(referenceDataTransformer.agencyLocationOf(booking.getAgencyLocation()))
                .bookingNo(booking.getBookingNo())
                .bookingStatus(booking.getBookingStatus())
                .inOutStatus(booking.getInOutStatus())
                .livingUnit(referenceDataTransformer.agencyInternalLocationOf(booking.getLivingUnit()))
                .offenderId(booking.getOffenderId())
                .bookingId(booking.getOffenderBookId())
                .rootOffenderId(booking.getRootOffenderId())
                .statusReason(booking.getStatusReason())
                .caseDateTime(typesTransformer.localDateTimeOf(booking.getCaseDate(), booking.getCaseTime()))
                .lastMovement(booking.getOffenderExternalMovements()
                        .stream()
                        .min(BY_MOVEMENT_DATE)
                        .map(movementsTransformer::movementOf)
                        .orElse(null))
                .build();
    }

    private String combinedMiddlenamesOf(Offender offender) {
        return String.join(" ", middleNamesOf(offender.getMiddleName(), offender.getMiddleName2()));
    }

    private List<String> middleNamesOf(String secondName, String thirdName) {
        Optional<String> maybeSecondName = Optional.ofNullable(secondName);
        Optional<String> maybeThirdName = Optional.ofNullable(thirdName);

        return ImmutableList.of(maybeSecondName, maybeThirdName)
                .stream()
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public uk.gov.justice.digital.nomis.api.Offender offenderOf(uk.gov.justice.digital.nomis.jpa.entity.Offender offender) {
        return uk.gov.justice.digital.nomis.api.Offender.builder()
                .dateOfBirth(offender.getBirthDate().toLocalDateTime().toLocalDate())
                .firstName(offender.getFirstName())
                .middleNames(combinedMiddlenamesOf(offender))
                .surname(offender.getLastName())
                .bookings(bookingsOf(offender.getOffenderBookings()))
                .identifiers(identifiersOf(offender.getOffenderIdentifiers()))
                .offenderId(offender.getOffenderId())
                .aliases(aliasesOf(offender.getOffenderAliases()))
                .nomsId(offender.getOffenderIdDisplay())
                .gender(genderOf(offender))
                .ethnicity(ethnicityOf(offender))
                .build();
    }

    private KeyValue ethnicityOf(Offender o) {
        return Optional.ofNullable(o.getRaceCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(o.getRaceCode())
                        .domain(ETHNICITY)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

    private KeyValue genderOf(Offender o) {
        return Optional.ofNullable(o.getSexCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(o.getSexCode())
                        .domain(SEX)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

}