package uk.gov.justice.digital.nomis.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.nomis.api.Offender;
import uk.gov.justice.digital.nomis.api.OffenderEvent;
import uk.gov.justice.digital.nomis.jpa.entity.MovementReason;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;
import uk.gov.justice.digital.nomis.jpa.entity.XtagEventNonJpa;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderEventsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.XtagEventsRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderEventsTransformer;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class XtagEventsServiceTest {

    private final Timestamp MOVEMENT_TIME = Timestamp.valueOf("2019-07-12 21:00:00.000");

    @Mock
    private XtagEventsRepository repository;

    @Mock
    private OffenderEventsTransformer transformer;

    @Mock
    private OffenderService offenderService;

    private XtagEventsService service;

    @Before
    public void setUp() {
        service = new XtagEventsService(repository, transformer, offenderService);
    }

    @Test
    public void shouldAddNomsIdToOffenderAliasEvent() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(Offender.builder().nomsId("A2345GB").build()));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("OFFENDER_ALIAS-CHANGED").offenderId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
    }

    @Test
    public void shouldDecorateWithExternalMovementData() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();
        final var offender = uk.gov.justice.digital.nomis.jpa.entity.Offender.builder().offenderIdDisplay("A2345GB").build();
        final var pk = OffenderExternalMovement.Pk.builder().movementSeq(2L).offenderBooking(OffenderBooking.builder().offender(offender).build()).build();
        final var externalMovementEntity = OffenderExternalMovement.builder()
                .fromAgyLocId("MDI")
                .toAgyLocId("BAI")
                .movementReason(MovementReason.builder().movementType("REL").build())
                .movementTime(MOVEMENT_TIME)
                .id(pk).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(offenderService.getExternalMovement(1L, 2L)).thenReturn(Optional.of(externalMovementEntity));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("EXTERNAL_MOVEMENT_RECORD-INSERTED").offenderId(1L).movementSeq(2L).bookingId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("fromAgencyLocationId").containsExactly("MDI");
        assertThat(offenderEventList).extracting("toAgencyLocationId").containsExactly("BAI");
        assertThat(offenderEventList).extracting("movementDateTime").containsExactly(MOVEMENT_TIME.toLocalDateTime());
        assertThat(offenderEventList).extracting("movementType").containsExactly("REL");
    }

    @Test
    public void shouldDecorateWithExternalMovementDataHandlesNullableFields() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var offender = uk.gov.justice.digital.nomis.jpa.entity.Offender.builder().offenderIdDisplay("A2345GB").build();
        final var pk = OffenderExternalMovement.Pk.builder().movementSeq(2L).offenderBooking(OffenderBooking.builder().offender(offender).build()).build();
        final var externalMovementEntity = OffenderExternalMovement.builder().id(pk).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(offenderService.getExternalMovement(1L, 2L)).thenReturn(Optional.of(externalMovementEntity));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("EXTERNAL_MOVEMENT_RECORD-INSERTED").offenderId(1L).movementSeq(2L).bookingId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("bookingId").containsExactly(1L);
        assertThat(offenderEventList).extracting("movementSeq").containsExactly(2L);
        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("fromAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("toAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("movementDateTime").containsNull();
        assertThat(offenderEventList).extracting("movementType").containsNull();
    }

    @Test
    public void shouldDecorateWithExternalMovementDataHandlesNoRecordFound() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));
        when(offenderService.getExternalMovement(1L, 2L)).thenReturn(Optional.empty());
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("EXTERNAL_MOVEMENT_RECORD-INSERTED").offenderId(1L).movementSeq(2L).bookingId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("bookingId").containsExactly(1L);
        assertThat(offenderEventList).extracting("movementSeq").containsExactly(2L);
        assertThat(offenderEventList).extracting("offenderIdDisplay").containsNull();
        assertThat(offenderEventList).extracting("fromAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("toAgencyLocationId").containsNull();
        assertThat(offenderEventList).extracting("movementDateTime").containsNull();
        assertThat(offenderEventList).extracting("movementType").containsNull();
    }

    @Test
    public void shouldDecorateOffenderUpdatedWithOffenderDisplayNo() {
        final var filter = OffenderEventsFilter.builder().from(LocalDateTime.now()).to(LocalDateTime.now()).build();
        final var offender = Offender.builder().nomsId("A2345GB").offenderId(1L).build();

        final var xTagEvent = XtagEventNonJpa.builder().build();
        when(repository.findAll(Mockito.any(OffenderEventsFilter.class))).thenReturn(List.of(xTagEvent));

        when(offenderService.getOffenderByOffenderId(1L)).thenReturn(Optional.of(offender));
        when(transformer.offenderEventOf(Mockito.any(XtagEventNonJpa.class))).thenReturn(
                OffenderEvent.builder().eventType("OFFENDER-UPDATED").offenderId(1L).build());

        final var offenderEventList = service.findAll(filter);

        assertThat(offenderEventList).extracting("offenderIdDisplay").containsExactly("A2345GB");
        assertThat(offenderEventList).extracting("offenderId").containsExactly(1L);
    }

    @Test
    public void appliesFudgeWhenNotCurrentlyInDaylightSavingsTime() {

        final var aWinterDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0);

        final var actual = XtagEventsService.asUtcPlusOne(aWinterDate);

        assertThat(actual).isEqualTo(aWinterDate.plusHours(1L));
    }

    @Test
    public void doesNotApplyFudgeWhenCurrentlyInDaylightSavingsTime() {

        final var aSummerDate = LocalDateTime.of(2020, 7, 1, 0, 0, 0, 0);

        final var actual = XtagEventsService.asUtcPlusOne(aSummerDate);

        assertThat(actual).isEqualTo(aSummerDate);
    }

}
