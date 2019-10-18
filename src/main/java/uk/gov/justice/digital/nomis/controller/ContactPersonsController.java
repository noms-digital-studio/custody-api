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
import uk.gov.justice.digital.nomis.api.OffenderContactPerson;
import uk.gov.justice.digital.nomis.service.ContactPersonsService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Contact Person resources", tags = "Offender Contact Persons")
public class ContactPersonsController {

    private final ContactPersonsService contactPersonsService;

    @Autowired
    public ContactPersonsController(final ContactPersonsService contactPersonsService) {
        this.contactPersonsService = contactPersonsService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/contactPersons", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<OffenderContactPerson>> getOffenderContactPersons(@PathVariable("offenderId") final Long offenderId) {

        return contactPersonsService.contactPersonsForOffender(offenderId)
                .map(iepList -> new ResponseEntity<>(iepList, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
