package uk.gov.justice.digital.nomis.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.OffenderEvent;
import uk.gov.justice.digital.nomis.controller.OffenderEventsController;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderEventsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderEventsRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderEventsTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OffenderEventsService {

    private static final Comparator<uk.gov.justice.digital.nomis.api.OffenderEvent> BY_OFFENDER_EVENT_TIMESTAMP =
            Comparator.comparing(uk.gov.justice.digital.nomis.api.OffenderEvent::getEventDatetime);

    private static final Comparator<uk.gov.justice.digital.nomis.api.OffenderEvent> BY_OFFENDER_EVENT_TIMESTAMP_DESC = BY_OFFENDER_EVENT_TIMESTAMP.reversed();

    private final OffenderEventsTransformer offenderEventsTransformer;
    private final OffenderEventsRepository offenderEventsRepository;
    private final XtagEventsService xtagEventsService;

    @Autowired
    public OffenderEventsService(OffenderEventsTransformer offenderEventsTransformer,
                                 OffenderEventsRepository offenderEventsRepository,
                                 XtagEventsService xtagEventsService) {
        this.offenderEventsTransformer = offenderEventsTransformer;
        this.offenderEventsRepository = offenderEventsRepository;
        this.xtagEventsService = xtagEventsService;
    }

    public Optional<List<OffenderEvent>> getEvents(Optional<LocalDateTime> maybeFrom,
                                                   Optional<LocalDateTime> maybeTo,
                                                   Optional<Set<String>> maybeTypeFilter,
                                                   Optional<OffenderEventsController.SortTypes> maybeSortBy) {
        LocalDateTime from = fromOrDefault(maybeFrom, maybeTo);
        LocalDateTime to = toOrDefault(maybeTo, from);

        final OffenderEventsFilter oeFilter = OffenderEventsFilter.builder().from(from).to(to).types(maybeTypeFilter).build();
        return getFilteredOffenderEvents(oeFilter, maybeSortBy);
    }

    private LocalDateTime toOrDefault(Optional<LocalDateTime> maybeTo, LocalDateTime from) {
        return maybeTo.orElse(from.plusDays(1));
    }

    private LocalDateTime fromOrDefault(Optional<LocalDateTime> maybeFrom, Optional<LocalDateTime> maybeTo) {
        return maybeFrom.orElse(maybeTo.map(to -> to.minusDays(1)).orElse(LocalDate.now().atStartOfDay()));
    }

    public Optional<List<OffenderEvent>> getEventsForOffenderId(Long offenderId,
                                                                Optional<LocalDateTime> maybeFrom,
                                                                Optional<LocalDateTime> maybeTo,
                                                                Optional<Set<String>> maybeTypeFilter,
                                                                Optional<OffenderEventsController.SortTypes> maybeSortBy) {
        LocalDateTime from = fromOrDefault(maybeFrom, maybeTo);
        LocalDateTime to = toOrDefault(maybeTo, from);

        final OffenderEventsFilter offenderEventsFilter = OffenderEventsFilter.builder().from(from).to(to).types(maybeTypeFilter).offenderId(Optional.of(offenderId)).build();
        return getFilteredOffenderEvents(offenderEventsFilter, maybeSortBy);
    }

    private Optional<List<OffenderEvent>> getFilteredOffenderEvents(OffenderEventsFilter oeFilter, Optional<OffenderEventsController.SortTypes> maybeSortBy) {

        List<OffenderEvent> offenderEvents = Optional.ofNullable(offenderEventsRepository.findAll(oeFilter))
                .map(ev -> ev.stream()
                        .map(offenderEventsTransformer::offenderEventOf)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        List<OffenderEvent> xtagEvents = xtagEventsService.findAll(oeFilter)
                .stream()
                .filter(oe -> isOffenderRelated(oeFilter, oe))
                .collect(Collectors.toList());

        Set<String> typeFilter = oeFilter.getTypes()
                .map(types -> types.stream().map(String::toUpperCase).collect(Collectors.toSet()))
                .orElse(ImmutableSet.of());

        List<OffenderEvent> allEvents = ImmutableList.<OffenderEvent>builder().addAll(offenderEvents).addAll(xtagEvents).build();

        return Optional.of(allEvents.stream()
                .filter(oe -> typeFilter.isEmpty() || typeFilter.contains(oe.getEventType()))
                .sorted(sortFunctionOf(maybeSortBy))
                .collect(Collectors.toList()));

    }

    private Comparator<? super OffenderEvent> sortFunctionOf(Optional<OffenderEventsController.SortTypes> maybeSortBy) {
        return maybeSortBy.filter(sortTypes -> sortTypes.equals(OffenderEventsController.SortTypes.TIMESTAMP_ASC))
                .map(sortTypes -> BY_OFFENDER_EVENT_TIMESTAMP)
                .orElse(BY_OFFENDER_EVENT_TIMESTAMP_DESC);
    }

    private Boolean isOffenderRelated(OffenderEventsFilter oeFilter, OffenderEvent oe) {
        return oeFilter.getOffenderId().map(id -> id.equals(oe.getOffenderId()) ||
                id.equals(oe.getRootOffenderId()) ||
                (id.equals(oe.getOwnerId()) && "OFF".equals(oe.getOwnerClass())))
                .orElse(true);
    }
}
