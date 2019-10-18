package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.CourtEvent;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.CourtEventsTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourtEventsService {

    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.CourtEvent> BY_COURT_EVENT_DATE_NEWEST_FIRST =
            Comparator.comparing(uk.gov.justice.digital.nomis.jpa.entity.CourtEvent::getEventDate)
                .thenComparing(uk.gov.justice.digital.nomis.jpa.entity.CourtEvent::getStartTime)
                .thenComparing(uk.gov.justice.digital.nomis.jpa.entity.CourtEvent::getEventId)
                .reversed();

    private final CourtEventsTransformer courtEventsTransformer;
    private final OffenderRepository offenderRepository;


    @Autowired
    public CourtEventsService(final CourtEventsTransformer courtEventsTransformer, final OffenderRepository offenderRepository) {
        this.courtEventsTransformer = courtEventsTransformer;
        this.offenderRepository = offenderRepository;
    }


    public Optional<List<CourtEvent>> courtEventsForOffenderId(final Long offenderId) {

        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings()
                        .stream()
                        .flatMap(offenderBooking -> offenderBooking.getCourtEvents().stream())
                        .sorted(BY_COURT_EVENT_DATE_NEWEST_FIRST)
                        .map(courtEventsTransformer::courtEventOf)
                        .collect(Collectors.toList()));

    }

}
