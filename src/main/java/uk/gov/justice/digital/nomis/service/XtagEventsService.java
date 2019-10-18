package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderEventsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.XtagEventsRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderEventsTransformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class XtagEventsService {

    private final XtagEventsRepository xtagEventsRepository;
    private final OffenderEventsTransformer offenderEventsTransformer;

    @Autowired
    public XtagEventsService(final XtagEventsRepository xtagEventsRepository, final OffenderEventsTransformer offenderEventsTransformer) {
        this.xtagEventsRepository = xtagEventsRepository;
        this.offenderEventsTransformer = offenderEventsTransformer;
    }

    public List<uk.gov.justice.digital.nomis.api.OffenderEvent> findAll(final OffenderEventsFilter oeFilter) {
        return xtagEventsRepository.findAll(fudgedXtagFilterOf(oeFilter)).stream()
                .map(offenderEventsTransformer::offenderEventOf)
                .collect(Collectors.toList());
    }

    private OffenderEventsFilter fudgedXtagFilterOf(final OffenderEventsFilter oeFilter) {
        // Xtag events are in British Summer Time all year round at rest in Oracle.
        // So we have to compensate when filtering by date. The Nomis data set
        // is stored at rest as Europe/London and so is affected by daylight savings.
        return oeFilter.toBuilder()
                .from(asUtcPlusOne(oeFilter.getFrom()))
                .to(asUtcPlusOne(oeFilter.getTo()))
                .build();
    }

    public static LocalDateTime asUtcPlusOne(final LocalDateTime localDateTime) {
        if (ZoneId.of("Europe/London").getRules().isDaylightSavings(localDateTime.toInstant(ZoneOffset.UTC))) {
            return localDateTime;
        }
        return localDateTime.plusHours(1L);
    }
}
