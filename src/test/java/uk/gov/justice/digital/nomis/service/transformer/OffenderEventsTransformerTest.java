package uk.gov.justice.digital.nomis.service.transformer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.justice.digital.nomis.CustodyApiApplication;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderEvent;
import uk.gov.justice.digital.nomis.xtag.Xtag;
import uk.gov.justice.digital.nomis.xtag.XtagContent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OffenderEventsTransformerTest {

    public static final String NOT_A_CASE_NOTE = "NOT_A_CASE_NOTE";
    public static final String CASE_NOTE_JSON = "{\"case_note\":{\"id\":61342651,\"contact_datetime\":\"1819-02-20 14:09:00\"\n" +
            ",\"source\":{\"code\":\"INST\"\n" +
            ",\"desc\":\"Prison\"\n" +
            "},\"type\":{\"code\":\"GEN\"\n" +
            ",\"desc\":\"General\"\n" +
            "},\"sub_type\":{\"code\":\"OSE\"\n" +
            ",\"desc\":\"\"\n" +
            "},\"staff_member\":{\"id\":483079,\"name\":\"White, Barry\"\n" +
            ",\"userid\":\"QWU90D\"\n" +
            "},\"text\":\"[redacted]stice.gov.uk \\n\\u260F 01811 8055 (Direct \\u2013 22222) \\u#### N/A [redacted]\"\n" +
            ",\"amended\":false}}";

    @Test
    public void canDeserializeIntoXtagContent() {

        final var objectMapper = new CustodyApiApplication().objectMapper();

        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class), objectMapper);

        assertThat(transformer.xtagContentOf(ImmutableMap.of("x", "y"))).isNotNull();
    }

    @Test
    public void xtagEnqueueTimestampIsSeasonallyAdjustedIntoDaylightSavings() {
        final var lastSecondOfWinter = LocalDateTime.of(2019, 3, 31, 00, 59, 59);
        final var firstSecondOfSummer = lastSecondOfWinter.plusSeconds(1L);
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(lastSecondOfWinter)).isEqualTo(lastSecondOfWinter.minusHours(1L));
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(firstSecondOfSummer)).isEqualTo(firstSecondOfSummer);
    }

    @Test
    public void xtagEnqueueTimestampIsSeasonallyAdjustedIntoUTC() {
        final var lastSecondOfSummer = LocalDateTime.of(2019, 10, 27, 01, 59, 59);
        final var firstSecondOfWinter = lastSecondOfSummer.plusSeconds(1L);
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(lastSecondOfSummer)).isEqualTo(lastSecondOfSummer);
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(firstSecondOfWinter)).isEqualTo(firstSecondOfWinter.minusHours(1L));
    }

    @Test
    public void externalMovementDescriptorBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().p_record_deleted("Y").build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-DELETED");
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().p_record_deleted("N").build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-INSERTED");
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().p_record_deleted(UUID.randomUUID().toString()).build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED");
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED");
    }

    @Test
    public void localDateOfBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.localDateOf("2019-02-14 10:11:12")).isEqualTo(LocalDate.of(2019, 02, 14));
        assertThat(OffenderEventsTransformer.localDateOf("14-FEB-2019")).isEqualTo(LocalDate.of(2019, 02, 14));
        assertThat(OffenderEventsTransformer.localDateOf("14-FEB-19")).isEqualTo(LocalDate.of(2019, 02, 14));
        assertThat(OffenderEventsTransformer.localDateOf(null)).isNull();
        assertThat(OffenderEventsTransformer.localDateOf("Some rubbish")).isNull();
    }

    @Test
    public void localDateTimeOfBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 10:11:12")).isEqualTo(LocalDateTime.of(2019, 02, 14, 10, 11, 12));
        assertThat(OffenderEventsTransformer.localDateOf(null)).isNull();
        assertThat(OffenderEventsTransformer.localDateOf("Some rubbish")).isNull();
    }


    @Test
    public void localDateTimeOfDateAndTimeBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 00:00:00", "1970-01-01 10:11:12")).isEqualTo(LocalDateTime.of(2019, 02, 14, 10, 11, 12));
        assertThat(OffenderEventsTransformer.localDateTimeOf(null, "1970-01-01 10:11:12")).isNull();
        assertThat(OffenderEventsTransformer.localDateTimeOf(null, "Some rubbish")).isNull();
        assertThat(OffenderEventsTransformer.localDateTimeOf(null, null)).isNull();
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 00:00:00", "Some rubbish")).isEqualTo(LocalDateTime.of(2019, 02, 14, 00, 00, 00));
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 00:00:00", null)).isEqualTo(LocalDateTime.of(2019, 02, 14, 00, 00, 00));
    }

    @Test
    public void externalMovementRecordEventOfHandlesAgyLocIdsAsStrings() {

        final var transformer = new OffenderEventsTransformer(null, null);
        assertThat(transformer.externalMovementRecordEventOf(Xtag.builder().content(
                XtagContent.builder()
                        .p_from_agy_loc_id("BARBECUE")
                        .p_to_agy_loc_id("SAUCE")
                        .build()
        ).build())).extracting("fromAgencyLocationId", "toAgencyLocationId").containsOnly("BARBECUE", "SAUCE");

    }

    @Test
    public void canCorrectlyDecodeCaseNoteEventTypes() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class), mock(ObjectMapper.class));

        assertThat(transformer.caseNoteEventTypeOf(OffenderEvent.builder()
                .eventType("CASE_NOTE")
                .eventData1(CASE_NOTE_JSON)
                .build())).isEqualTo("GEN-OSE");
    }

    @Test
    public void nonCaseNoteEventTypesAreNotDecoded() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class), mock(ObjectMapper.class));

        assertThat(transformer.caseNoteEventTypeOf(OffenderEvent.builder()
                .eventType(NOT_A_CASE_NOTE)
                .eventData1(CASE_NOTE_JSON)
                .build())).isEqualTo(NOT_A_CASE_NOTE);
    }

    @Test
    public void unkownEventTypesAreHandledAppropriately() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class), mock(ObjectMapper.class));

        assertThat(transformer.offenderEventOf((Xtag) null)).isNull();
        assertThat(transformer.offenderEventOf(Xtag.builder().build())).isNull();
        assertThat(transformer.offenderEventOf(Xtag.builder().eventType("meh").build())).isNotNull();
    }

}
