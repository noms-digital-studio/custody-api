package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.digital.nomis.api.Alert;
import uk.gov.justice.digital.nomis.service.AlertsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender alerts", tags = "Offender Alerts")
public class AlertsController {

    private final AlertsService alertsService;

    @Autowired
    public AlertsController(AlertsService alertsService) {
        this.alertsService = alertsService;
    }

    @RequestMapping(path = "/alerts", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query", value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query", value = "Number of records per page.", example = "20", defaultValue = "20"),
            @ApiImplicitParam(name = "from", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "to", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15")
    })
    public PagedResources<Resource<Alert>> getAlerts(final @ApiIgnore Pageable pageable,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> maybeFrom,
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> maybeTo) {

        return new PagedResourcesAssembler<Alert>(null, null).toResource(alertsService.getAlerts(pageable, maybeFrom, maybeTo));
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/alerts", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")
    })
    public ResponseEntity<List<Alert>> getOffenderAlerts(@PathVariable("offenderId") Long offenderId,
                                                         @RequestParam("bookingId") Optional<Long> maybeBookingId,
                                                         @RequestParam("alertType") Optional<String> maybeAlertType,
                                                         @RequestParam("alertCode") Optional<String> maybeAlertCode,
                                                         @RequestParam("alertStatus") Optional<String> maybeAlertStatus) {

        return maybeBookingId
                .map(bookingId -> alertsService.offenderAlertsForOffenderIdAndBookingId(offenderId, bookingId, maybeAlertCode, maybeAlertStatus, maybeAlertType))
                .orElse(alertsService.getOffenderAlerts(offenderId, maybeAlertCode, maybeAlertStatus, maybeAlertType))
                .map(attendances -> new ResponseEntity<>(attendances, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }

}
