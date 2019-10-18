package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.nomis.api.OffenderEvent;
import uk.gov.justice.digital.nomis.service.OffenderEventsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender case record events", tags = "Offender Events")
public class OffenderEventsController {

    public static final String NOTES = "**from** and **to** query params are optional.\n" +
            "An awful lot of events occur occur every day. To guard against unintentionally heavy queries, the following rules are applied:\n" +
            "If **both** are absent, scope will be limited to 24 hours starting from midnight yesterday.\n" +
            "If **to** is present but **from** is absent, **from** will be defaulted to 24 hours before **to**.\n" +
            "If **from** is present but **to** is absent, **to** will be defaulted to 24 hours after **from**.";

    private final OffenderEventsService offenderEventsService;

    @Autowired
    public OffenderEventsController(final OffenderEventsService offenderEventsService) {
        this.offenderEventsService = offenderEventsService;
    }

    @RequestMapping(path = "/events", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "from", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "to", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "type", dataType = "string", paramType = "query", allowMultiple = true,
                    value = "Comma separated list of event types to filter inclusively:\n" +
                            "BALANCE_UPDATE\nCOURT_SENTENCE-CHANGED\nALERT-DELETED\nPERSON_ADDRESS-UPDATED\nOFFENDER_ADDRESS-UPDATED\nADDRESS-UPDATED\nPERSON_ADDRESS-DELETED\nOFFENDER_ADDRESS-DELETED\nADDRESS-DELETED\nPERSON_ADDRESS-INSERTED\nHDC_FINE-INSERTED\nHDC_CONDITION-CHANGED\nOFFENDER_EMPLOYMENT-INSERTED\nOFFENDER_EMPLOYMENT-UPDATED\nOFFENDER_EMPLOYMENT-DELETED\nPHONE-INSERTED\nPHONE-UPDATED\nPHONE-DELETED\nHEARING_RESULT-CHANGED\nHEARING_RESULT-DELETED\nHEARING_DATE-CHANGED\nSENTENCE_CALCULATION_DATES-CHANGED\nOFFENDER_PROFILE_DETAILS-UPDATED\nOFFENDER_PROFILE_DETAILS-INSERTED\nALERT-INSERTED\nALERT-UPDATED\nASSESSMENT-CHANGED\nIMPRISONMENT_STATUS-CHANGED\nOFFENDER_IDENTIFIER-INSERTED\nOFFENDER_IDENTIFIER-DELETED\nEDUCATION_LEVEL-INSERTED\nEDUCATION_LEVEL-UPDATED\nEDUCATION_LEVEL-DELETED\nCONTACT_PERSON-INSERTED\nCONTACT_PERSON-UPDATED\nCONTACT_PERSON-DELETED\nOFFENDER-UPDATED\nOFFENDER_ALIAS-CHANGED\nADDRESS_USAGE-INSERTED\nADDRESS_USAGE-UPDATED\nADDRESS_USAGE-DELETED\nOFFENDER_DETAILS-CHANGED\nOFFENDER_BOOKING-INSERTED\nOFFENDER_BOOKING-CHANGED\nOFFENDER_BOOKING-REASSIGNED\nEXTERNAL_MOVEMENT_RECORD-INSERTED\nEXTERNAL_MOVEMENT_RECORD-DELETED\nEXTERNAL_MOVEMENT_RECORD-UPDATED\nOFFENDER_MOVEMENT-DISCHARGE\nOFFENDER_MOVEMENT-RECEPTION\nMATERNITY_STATUS-INSERTED\nMATERNITY_STATUS-UPDATED\nRISK_SCORE-CHANGED\nRISK_SCORE-DELETED\nOFFENDER_SANCTION-CHANGED\nBOOKING_NUMBER-CHANGED"),
            @ApiImplicitParam(name = "sortBy", dataType = "string", paramType = "query", value = "Sort order")

    })
    @ApiOperation(value = "Get events", notes = NOTES)
    public ResponseEntity<List<OffenderEvent>> getEvents(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> maybeFrom,
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> maybeTo,
                                                         final @RequestParam("type") Optional<Set<String>> maybeTypeFilter,
                                                         final @RequestParam("sortBy") Optional<SortTypes> maybeSortBy) {
        return offenderEventsService.getEvents(maybeFrom, maybeTo, maybeTypeFilter, maybeSortBy)
                .map(events -> new ResponseEntity<>(events, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/events", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "from", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "to", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "type", dataType = "string", paramType = "query", allowMultiple = true,
                    value = "Comma separated list of event types to filter inclusively:\n" +
                            "BALANCE_UPDATE\nALERT-DELETED\nOFFENDER_ADDRESS-UPDATED\nOFFENDER_ADDRESS-DELETED\nPHONE-INSERTED\nPHONE-UPDATED\nPHONE-DELETED\nALERT-INSERTED\nALERT-UPDATED\nOFFENDER-UPDATED\nOFFENDER_DETAILS-CHANGED\nOFFENDER_BOOKING-INSERTED\nOFFENDER_BOOKING-CHANGED\nOFFENDER_BOOKING-REASSIGNED\nBOOKING_NUMBER-CHANGED"),
            @ApiImplicitParam(name = "sortBy", dataType = "string", paramType = "query", value = "Sort order")
    })
    @ApiOperation(value = "Get events by offender id", notes = NOTES)
    public ResponseEntity<List<OffenderEvent>> getEventsByOffenderId(@PathVariable("offenderId") final Long offenderId,
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> maybeFrom,
                                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> maybeTo,
                                                                     final @RequestParam("type") Optional<Set<String>> maybeTypeFilter,
                                                                     final @RequestParam("sortBy") Optional<SortTypes> maybeSortBy) {
        return offenderEventsService.getEventsForOffenderId(offenderId, maybeFrom, maybeTo, maybeTypeFilter, maybeSortBy)
                .map(events -> new ResponseEntity<>(events, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    public enum SortTypes {
        TIMESTAMP_ASC ("TIMESTAMP_ASC"),
        TIMESTAMP_DESC("TIMESTAMP_DESC");

        private final String sortType;

        SortTypes(final String sortType) {
            this.sortType = sortType;
        }
    }
}
