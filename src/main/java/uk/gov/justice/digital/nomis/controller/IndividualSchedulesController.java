package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.nomis.api.IndividualSchedule;
import uk.gov.justice.digital.nomis.service.IndividualSchedulesService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Individual Schedules", tags = "Offender Individual Schedules")
public class IndividualSchedulesController {

    private final IndividualSchedulesService individualSchedulesService;

    @Autowired
    public IndividualSchedulesController(final IndividualSchedulesService individualSchedulesService) {
        this.individualSchedulesService = individualSchedulesService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/individualSchedules", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")
    })
    public ResponseEntity<List<IndividualSchedule>> getOffenderIndividualSchedules(@PathVariable("offenderId") final Long offenderId,
                                                                                   @RequestParam("bookingId") final Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> individualSchedulesService.individualSchedulesForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(individualSchedulesService.individualSchedulesForOffenderId(offenderId))
                .map(individualSchedules -> new ResponseEntity<>(individualSchedules, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }
}
