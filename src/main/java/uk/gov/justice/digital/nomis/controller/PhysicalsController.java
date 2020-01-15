package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.nomis.api.Physicals;
import uk.gov.justice.digital.nomis.service.PhysicalsService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api( description = "Offender physicals resources", tags = "Offender Physicals")
public class PhysicalsController {

    private final PhysicalsService physicalsService;

    @Autowired
    public PhysicalsController(final PhysicalsService physicalsService) {
        this.physicalsService = physicalsService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/physicals", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<Physicals>> getOffenderPhysicals(@PathVariable("offenderId") final Long offenderId) {

        return physicalsService.physicalsForOffenderId(offenderId)
                .map(physicalsList -> new ResponseEntity<>(physicalsList, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
