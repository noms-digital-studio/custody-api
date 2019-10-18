package uk.gov.justice.digital.nomis.service.transformer;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Booking;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OffenderActiveBookingTransformer {

    private final TypesTransformer typesTransformer;
    private final ReferenceDataTransformer referenceDataTransformer;


    @Autowired
    public OffenderActiveBookingTransformer(final TypesTransformer typesTransformer, final ReferenceDataTransformer referenceDataTransformer) {
        this.typesTransformer = typesTransformer;
        this.referenceDataTransformer = referenceDataTransformer;
    }


    private Booking bookingOf(final OffenderBooking booking) {
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
                .build();
    }

    private String combinedMiddlenamesOf(final Offender offender) {
        return String.join(" ", middleNamesOf(offender.getMiddleName(), offender.getMiddleName2()));
    }

    private List<String> middleNamesOf(final String secondName, final String thirdName) {
        final var maybeSecondName = Optional.ofNullable(secondName);
        final var maybeThirdName = Optional.ofNullable(thirdName);

        return ImmutableList.of(maybeSecondName, maybeThirdName)
                .stream()
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());
    }

    public uk.gov.justice.digital.nomis.api.OffenderActiveBooking offenderOf(final OffenderBooking offenderBooking) {
        return uk.gov.justice.digital.nomis.api.OffenderActiveBooking.builder()
                .dateOfBirth(offenderBooking.getOffender().getBirthDate().toLocalDateTime().toLocalDate())
                .firstName(offenderBooking.getOffender().getFirstName())
                .middleNames(combinedMiddlenamesOf(offenderBooking.getOffender()))
                .surname(offenderBooking.getOffender().getLastName())
                .activeBooking(bookingOf(offenderBooking))
                .offenderId(offenderBooking.getOffender().getOffenderId())
                .nomsId(offenderBooking.getOffender().getOffenderIdDisplay())
                .build();
    }

}
