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
import uk.gov.justice.digital.nomis.api.SentenceCalculation;
import uk.gov.justice.digital.nomis.service.SentenceCalculationsService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Sentence Calculation resources", tags = "Offender Sentence calculations")
public class SentenceCalculationsController {

    private final SentenceCalculationsService sentenceCalculationsService;

    @Autowired
    public SentenceCalculationsController(final SentenceCalculationsService sentenceCalculationsService) {
        this.sentenceCalculationsService = sentenceCalculationsService;
    }

    @RequestMapping(path = "/sentenceCalculations", method = RequestMethod.GET)
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "Results page you want to retrieve (0..N)", example = "0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "Number of records per page.", example = "20", defaultValue = "20")})
    public PagedResources<Resource<SentenceCalculation>> getSentenceCalculations(
            final @ApiIgnore Pageable pageable,
            @ApiParam(allowMultiple = true,
                    value = "Optionally specifies multiple individual offenders' bookings (i.e. prison terms)")
            @RequestParam(value = "bookingId", required = false) final Set<Long> bookingId) {

        final var sentenceCalculations = sentenceCalculationsService.getSentenceCalculations(pageable, bookingId);
        return new PagedResourcesAssembler<SentenceCalculation>(null, null).toResource(sentenceCalculations);
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/sentenceCalculations", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<SentenceCalculation>> getOffenderSentenceCalculations(@PathVariable("offenderId") final Long offenderId,
                                                                                     @RequestParam("bookingId") final Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> sentenceCalculationsService.sentenceCalculationsForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(sentenceCalculationsService.sentenceCalculationsForOffenderId(offenderId))
                .map(sentenceCalculations -> new ResponseEntity<>(sentenceCalculations, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
