package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Offender;
import uk.gov.justice.digital.nomis.api.OffenderEvent;
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
    private final OffenderService offenderService;

    @Autowired
    public XtagEventsService(final XtagEventsRepository xtagEventsRepository, final OffenderEventsTransformer offenderEventsTransformer, OffenderService offenderService) {
        this.xtagEventsRepository = xtagEventsRepository;
        this.offenderEventsTransformer = offenderEventsTransformer;
        this.offenderService = offenderService;
    }

    public List<uk.gov.justice.digital.nomis.api.OffenderEvent> findAll(final OffenderEventsFilter oeFilter) {
        return xtagEventsRepository.findAll(fudgedXtagFilterOf(oeFilter)).stream()
                .map(offenderEventsTransformer::offenderEventOf)
                .map(this::addAdditionalEventData)
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


    private OffenderEvent addAdditionalEventData(final OffenderEvent oe) {
        switch (oe.getEventType()) {
            case "OFFENDER_DETAILS-CHANGED":
            case "OFFENDER_ALIAS-CHANGED":
            case "OFFENDER-UPDATED": {
                final var nomsId = offenderService.getOffenderByOffenderId(oe.getOffenderId()).map(Offender::getNomsId)
                        .orElse(null);
                oe.setOffenderIdDisplay(nomsId);
                break;
            }
            case "BED_ASSIGNMENT_HISTORY-INSERTED":
            case "CONFIRMED_RELEASE_DATE-CHANGED":
            case "SENTENCE_DATES-CHANGED": {
                final var nomsId = offenderService.getOffenderByBookingId(oe.getBookingId()).map(Offender::getNomsId)
                        .orElse(null);
                oe.setOffenderIdDisplay(nomsId);
                break;
            }
            case "EXTERNAL_MOVEMENT_RECORD-INSERTED":
                offenderService.getExternalMovement(oe.getBookingId(), oe.getMovementSeq()).ifPresent(em -> {
                    final var offenderBooking = em.getId().getOffenderBooking();
                    oe.setOffenderIdDisplay(offenderBooking.getOffender().getOffenderIdDisplay());
                    oe.setFromAgencyLocationId(em.getFromAgyLocId());
                    oe.setToAgencyLocationId(em.getToAgyLocId());
                    oe.setDirectionCode(em.getDirectionCode());
                    oe.setMovementDateTime(em.getMovementTime() != null ? em.getMovementTime().toLocalDateTime() : null);
                    oe.setMovementType(em.getMovementReason() != null ? em.getMovementReason().getMovementType() : null);
                });
                break;
        }
        return oe;
    }
}
