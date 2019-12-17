package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.nomis.api.Offender;
import uk.gov.justice.digital.nomis.api.OffenderActiveBooking;
import uk.gov.justice.digital.nomis.service.OffenderService;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender resources", tags = "Offenders")
public class OffenderController {

    private final OffenderService offenderService;

    @Autowired
    public OffenderController(final OffenderService offenderService) {
        this.offenderService = offenderService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<Offender> getOffender(@PathVariable("offenderId") final Long offenderId) {

        return offenderService.getOffenderByOffenderId(offenderId)
                .map(offender -> new ResponseEntity<>(offender, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(path = "/offenders/prison/{agencyLocationId}", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Retrieves a list of active offenders at a prison",
            nickname = "retrieve offenders at prison")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Prison not found"),
            @ApiResponse(code = 200, message = "OK")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.", example = "20", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query",
                    value = "Sort column and direction, eg sort=offender.lastName,asc. Multiple sort params allowed.")})
    public Page<OffenderActiveBooking> getOffendersByPrison(@PathVariable("agencyLocationId") final String agencyLocationId,
                                                            @PageableDefault(page = 0, size = 10, sort = {"offender.lastName", "offender.firstName"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return offenderService.getOffendersByPrison(agencyLocationId, pageable);
    }

}
