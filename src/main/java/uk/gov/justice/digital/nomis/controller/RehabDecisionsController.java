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
import uk.gov.justice.digital.nomis.api.RehabDecision;
import uk.gov.justice.digital.nomis.service.RehabDecisionService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Rehab Decisions and associated resources", tags = "Offender IEPs")
public class RehabDecisionsController {

    private final RehabDecisionService rehabDecisionService;

    @Autowired
    public RehabDecisionsController(final RehabDecisionService rehabDecisionService) {
        this.rehabDecisionService = rehabDecisionService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/rehabDecisions", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<RehabDecision>> getOffenderRehabDecisions(@PathVariable("offenderId") final Long offenderId) {

        return rehabDecisionService.rehabDecisionsForOffenderId(offenderId)
                .map(events -> new ResponseEntity<>(events, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
