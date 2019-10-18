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
import uk.gov.justice.digital.nomis.api.OffenderImprisonmentStatus;
import uk.gov.justice.digital.nomis.service.ImprisonStatusService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Imprisonment Status resources", tags = "Offender Imprisonment statuses")
public class ImprisonStatusController {

    private final ImprisonStatusService imprisonStatusService;

    @Autowired
    public ImprisonStatusController(final ImprisonStatusService imprisonStatusService) {
        this.imprisonStatusService = imprisonStatusService;
    }

    @RequestMapping(path = "/imprisonmentStatuses", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.", example = "20", defaultValue = "20")})
    public PagedResources<Resource<OffenderImprisonmentStatus>> getImprisonmentStatuses(
            final @ApiIgnore Pageable pageable) {

        final var imprisonStatuses = imprisonStatusService.getOffenderImprisonStatuses(pageable);
        return new PagedResourcesAssembler<OffenderImprisonmentStatus>(null, null).toResource(imprisonStatuses);
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/imprisonmentStatuses", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<OffenderImprisonmentStatus>> getOffenderImprisonmentStatuses(@PathVariable("offenderId") final Long offenderId,
                                                                                            @RequestParam("bookingId") final Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> imprisonStatusService.offenderImprisonStatusForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(imprisonStatusService.offenderImprisonStatusForOffenderId(offenderId))
                .map(imprisonStatuses -> new ResponseEntity<>(imprisonStatuses, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
