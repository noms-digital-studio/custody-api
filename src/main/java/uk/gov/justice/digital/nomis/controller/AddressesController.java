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
import uk.gov.justice.digital.nomis.api.Address;
import uk.gov.justice.digital.nomis.service.AddressService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Address resources", tags = "Addresses")
public class AddressesController {

    private final AddressService addressService;

    @Autowired
    public AddressesController(final AddressService addressService) {
        this.addressService = addressService;
    }

    @RequestMapping(path = "/addresses", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.", example = "20", defaultValue = "20")})
    public PagedResources<Resource<Address>> getAddresses(final @ApiIgnore Pageable pageable) {

        final var addresses = addressService.getAddresses(pageable);
        return new PagedResourcesAssembler<Address>(null, null).toResource(addresses);
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/addresses", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<Address>> getOffenderAddresses(@PathVariable("offenderId") final Long offenderId) {

        return addressService.getOffenderAddresses(offenderId)
                .map(addresses -> new ResponseEntity<>(addresses, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }
}
