package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.digital.nomis.api.OffenderAssessment;
import uk.gov.justice.digital.nomis.service.AssessmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Assessment resources", tags = "Offender Assessments")
public class AssessmentsController {

    private final AssessmentService assessmentService;

    @Autowired
    public AssessmentsController(final AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @RequestMapping(path = "/assessments", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.", example = "20", defaultValue = "20"),
            @ApiImplicitParam(name = "from", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15"),
            @ApiImplicitParam(name = "to", dataType = "date", paramType = "query",
                    value = "ISO 8601 Date Time without zone or offset (local date time), eg 2017-07-24T09:18:15")})
    public PagedResources<Resource<OffenderAssessment>> getAssessments(final @ApiIgnore Pageable pageable,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("from") Optional<LocalDateTime> maybeFrom,
                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final @RequestParam("to") Optional<LocalDateTime> maybeTo) {

        return new PagedResourcesAssembler<OffenderAssessment>(null, null).toResource(assessmentService.getAssessments(pageable, maybeFrom, maybeTo));
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/assessments", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or bookingId not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<OffenderAssessment>> getOffenderAssessments(@PathVariable("offenderId") final Long offenderId,
                                                                           @RequestParam("bookingId") final Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> assessmentService.assessmentsForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(assessmentService.getOffenderAssessments(offenderId))
                .map(assessments -> new ResponseEntity<>(assessments, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }
}
