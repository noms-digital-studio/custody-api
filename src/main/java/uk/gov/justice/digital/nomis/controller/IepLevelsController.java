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
import uk.gov.justice.digital.nomis.api.OffenderIepLevel;
import uk.gov.justice.digital.nomis.service.IEPService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Incentives and Earned Priviliges resources", tags = "Offender IEPs")
public class IepLevelsController {

    private final IEPService iepService;

    @Autowired
    public IepLevelsController(final IEPService iepService) {
        this.iepService = iepService;
    }


    @RequestMapping(path = "/offenders/offenderId/{offenderId}/ieps", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<OffenderIepLevel>> getOffenderIEPs(@PathVariable("offenderId") final Long offenderId) {

        return iepService.iepsForOffenderId(offenderId)
                .map(iepList -> new ResponseEntity<>(iepList, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
