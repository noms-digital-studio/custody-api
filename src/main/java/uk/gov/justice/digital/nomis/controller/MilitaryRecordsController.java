package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.digital.nomis.api.MilitaryRecord;
import uk.gov.justice.digital.nomis.service.MilitaryRecordsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender military records", tags = "Offender Military Records")
public class MilitaryRecordsController {

    private final MilitaryRecordsService militaryRecordsService;

    @Autowired
    public MilitaryRecordsController(MilitaryRecordsService militaryRecordsService) {
        this.militaryRecordsService = militaryRecordsService;
    }

    @RequestMapping(path = "/militaryRecords", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page."),
            @ApiImplicitParam(name = "from", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "to", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15")})
    public PagedResources<Resource<MilitaryRecord>> getMilitaryRecords(final @ApiIgnore Pageable pageable,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> maybeFrom,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> maybeTo) {

        Page<MilitaryRecord> militaryRecords = militaryRecordsService.getMilitaryRecords(pageable, maybeFrom, maybeTo);
        return new PagedResourcesAssembler<MilitaryRecord>(null, null).toResource(militaryRecords);
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/militaryRecords", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or bookingId not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<MilitaryRecord>> getOffenderMilitaryRecords(@PathVariable("offenderId") Long offenderId,
                                                                           @RequestParam("bookingId") Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> militaryRecordsService.getMilitaryRecordsForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(militaryRecordsService.getMilitaryRecordsForOffenderId(offenderId))
                .map(movements -> new ResponseEntity<>(movements, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }

}
