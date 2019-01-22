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
import uk.gov.justice.digital.nomis.api.NomisStaffUser;
import uk.gov.justice.digital.nomis.service.StaffUsersService;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "NOMIS Staff User Details", tags = "NOMIS Staff User Details")
public class NomisStaffUsersController {

    private final StaffUsersService staffUsersService;

    @Autowired
    public NomisStaffUsersController(StaffUsersService userService) {
        this.staffUsersService = userService;
    }

    @RequestMapping(path = "/nomis-staff-users/{username}", method = RequestMethod.GET)
    @ApiOperation(value = "Retrieves the NOMIS staff user for this username", notes = "Username is a unique reference to a NOMIS staff member",
            nickname = "retrieve NOMIS staff user")
    @ApiResponses({
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<NomisStaffUser> getNomisStaffUser(@PathVariable("username") String username) {

        return staffUsersService.staffUserForUsername(username)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @RequestMapping(path = "/nomis-staff-users", method = RequestMethod.GET)
    @ApiOperation(value = "Retrieves a pageable list of NOMIS staff users", notes = "Paged list of NOMIS staff users",
            nickname = "retrieve page list of NOMIS staff users")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.")})
    public PagedResources<Resource<NomisStaffUser>> getNomisStaffUsers(final Pageable pageable) {

        Page<NomisStaffUser> staffUsers = staffUsersService.getStaffUsers(pageable);
        return new PagedResourcesAssembler<NomisStaffUser>(null, null).toResource(staffUsers);
    }
}
