package uk.gov.justice.digital.nomis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.digital.nomis.api.Sentence;
import uk.gov.justice.digital.nomis.service.SentencesService;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Sentence resources", tags = "Offender Sentences")
public class SentencesController {

    private final SentencesService sentencesService;

    @Autowired
    public SentencesController(final SentencesService sentencesService) {
        this.sentencesService = sentencesService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/sentences", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender or booking not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<Sentence>> getOffenderSentences(@PathVariable("offenderId") final Long offenderId,
                                                               @RequestParam("bookingId") final Optional<Long> maybeBookingId) {

        return maybeBookingId
                .map(bookingId -> sentencesService.sentencesForOffenderIdAndBookingId(offenderId, bookingId))
                .orElse(sentencesService.sentencesForOffenderId(offenderId))
                .map(sentences -> new ResponseEntity<>(sentences, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));

    }
}
