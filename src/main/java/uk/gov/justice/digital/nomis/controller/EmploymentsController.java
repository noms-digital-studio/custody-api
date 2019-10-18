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
import uk.gov.justice.digital.nomis.api.Employment;
import uk.gov.justice.digital.nomis.service.EmploymentsService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Employment resources", tags = "Offender Employments")
public class EmploymentsController {

    private final EmploymentsService employmentsService;

    @Autowired
    public EmploymentsController(final EmploymentsService employmentsService) {
        this.employmentsService = employmentsService;
    }


    @RequestMapping(path = "/offenders/offenderId/{offenderId}/employments", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<Employment>> getOffenderEmployments(@PathVariable("offenderId") final Long offenderId) {

        return employmentsService.employmentsForOffenderId(offenderId)
                .map(employments -> new ResponseEntity<>(employments, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
