package uk.gov.justice.digital.nomis.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.digital.nomis.api.Offender;
import uk.gov.justice.digital.nomis.api.OffenderEvent;
import uk.gov.justice.digital.nomis.jpa.entity.XtagEventNonJpa;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderEventsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.XtagEventsRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderEventsTransformer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class XtagEventsServiceTest {

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
