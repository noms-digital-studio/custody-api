package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.digital.nomis.api.AgencyInternalLocation;
import uk.gov.justice.digital.nomis.api.AgencyLocation;
import uk.gov.justice.digital.nomis.service.ReferenceDataService;

@RestController
@Api(description = "Reference data resources", tags = "Reference data sets")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @Autowired
    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @RequestMapping(path = "/agencyLocations", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.")})
    public PagedResources<Resource<AgencyLocation>> getAgencyLocations(final @ApiParam Pageable pageable,
                                                                       final PagedResourcesAssembler<AgencyLocation> assembler) {

        Page<AgencyLocation> agencyLocations = referenceDataService.getLocations(pageable);
        return assembler.toResource(agencyLocations);
    }

    @RequestMapping(path = "/agencyInternalLocations", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.")})
    public PagedResources<Resource<AgencyInternalLocation>> getAgencyInternalLocations(final @ApiParam Pageable pageable,
                                                                                       final PagedResourcesAssembler<AgencyInternalLocation> assembler) {

        Page<AgencyInternalLocation> agencyInternalLocations = referenceDataService.getInternalLocations(pageable);
        return assembler.toResource(agencyInternalLocations);
    }

}
