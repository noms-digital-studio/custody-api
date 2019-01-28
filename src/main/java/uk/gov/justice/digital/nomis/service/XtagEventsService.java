package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderEventsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.XtagEventsRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderEventsTransformer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class XtagEventsService {

    private final XtagEventsRepository xtagEventsRepository;
    private final OffenderEventsTransformer offenderEventsTransformer;

    @Autowired
    public XtagEventsService(XtagEventsRepository xtagEventsRepository, OffenderEventsTransformer offenderEventsTransformer) {
        this.xtagEventsRepository = xtagEventsRepository;
        this.offenderEventsTransformer = offenderEventsTransformer;
    }

    public List<uk.gov.justice.digital.nomis.api.OffenderEvent> findAll(OffenderEventsFilter oeFilter) {
        return xtagEventsRepository.findAll(fudgedXtagFilterOf(oeFilter)).stream()
                .map(offenderEventsTransformer::offenderEventOf)
                .collect(Collectors.toList());
    }

    private OffenderEventsFilter fudgedXtagFilterOf(OffenderEventsFilter oeFilter) {
        // Xtag events are in British Summer Time all year round at rest in Oracle.
        // So we have to compensate when filtering by date. The Nomis data set
        // is stored at rest as Europe/London and so is affected by daylight savings.
        final Instant now = Instant.now();

        return oeFilter.toBuilder()
                .from(asUtcPlusOne(oeFilter.getFrom(), now))
                .to(asUtcPlusOne(oeFilter.getTo(), now))
                .build();
    }

    public static LocalDateTime asUtcPlusOne(LocalDateTime localDateTime, Instant now) {
        if (ZoneId.of("Europe/London").getRules().isDaylightSavings(now)) {
            return localDateTime;
        }
        return localDateTime.plusHours(1L);

    }
}
