package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.digital.nomis.api.HealthProblem;
import uk.gov.justice.digital.nomis.service.HealthProblemsService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Health problem resources", tags = "Health Problems")
public class HealthProblemsController {

    private final HealthProblemsService healthProblemsService;

    @Autowired
    public HealthProblemsController(final HealthProblemsService healthProblemsService) {
        this.healthProblemsService = healthProblemsService;
    }

    @RequestMapping(path = "/healthProblems", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.", example = "20", defaultValue = "20")})
    public PagedResources<Resource<HealthProblem>> getHealthProblems(final @ApiIgnore Pageable pageable) {

        final var healthProblems = healthProblemsService.getHealthProblems(pageable);
        return new PagedResourcesAssembler<HealthProblem>(null, null).toResource(healthProblems);
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/healthProblems", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<HealthProblem>> getOffenderHealthProblems(@PathVariable("offenderId") final Long offenderId,
                                                                         @RequestParam("bookingId") final Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> healthProblemsService.healthProblemsForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(healthProblemsService.healthProblemsForOffenderId(offenderId))
                .map(healthProblems -> new ResponseEntity<>(healthProblems, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
