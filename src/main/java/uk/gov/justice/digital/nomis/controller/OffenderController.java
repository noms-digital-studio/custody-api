package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.nomis.api.Offender;
import uk.gov.justice.digital.nomis.api.OffenderActiveBooking;
import uk.gov.justice.digital.nomis.service.OffenderService;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender resources", tags = "Offenders")
public class OffenderController {

    private final OffenderService offenderService;

    @Autowired
    public OffenderController(OffenderService offenderService) {
        this.offenderService = offenderService;
    }

    @RequestMapping(path = "/offenders", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.")})
    public PagedResources<Resource<Offender>> getOffenders(final Pageable pageable) {
        Page<Offender> offenders = offenderService.getOffenders(pageable);

        return new PagedResourcesAssembler<Offender>(null, null).toResource(offenders);
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<Offender> getOffender(@PathVariable("offenderId") Long offenderId) {

        return offenderService.getOffenderByOffenderId(offenderId)
                .map(offender -> new ResponseEntity<>(offender, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(path = "/offenders/nomsId/{nomsId}", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<Offender> getOffender(@PathVariable("nomsId") String nomsId) {

        return offenderService.getOffenderByNomsId(nomsId)
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
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query",
                    value = "Sort column and direction, eg sort=offender.lastName,asc. Multiple sort params allowed.")})
    public PagedResources<Resource<OffenderActiveBooking>> getOffendersByPrison(@PathVariable("agencyLocationId") String agencyLocationId,
                                                                                @PageableDefault(page = 0, size = 10, sort = {"offender.lastName", "offender.firstName"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        Page<OffenderActiveBooking> offenders = offenderService.getOffendersByPrison(agencyLocationId, pageable);
        return new PagedResourcesAssembler<OffenderActiveBooking>(null, null).toResource(offenders);
    }

}
