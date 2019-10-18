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
import uk.gov.justice.digital.nomis.api.DiaryDetail;
import uk.gov.justice.digital.nomis.service.DiaryDetailService;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Api(description = "Offender Diary Details and associated resources", tags = "Offender Diary Details")
public class DiaryDetailsController {

    private final DiaryDetailService diaryDetailService;

    @Autowired
    public DiaryDetailsController(final DiaryDetailService diaryDetailService) {
        this.diaryDetailService = diaryDetailService;
    }

    @RequestMapping(path = "/offenders/offenderId/{offenderId}/diaryDetails", method = RequestMethod.GET)
    @ApiResponses({
            @ApiResponse(code = 404, message = "Offender not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<DiaryDetail>> getOffenderDiaryDetails(@PathVariable("offenderId") final Long offenderId) {

        return diaryDetailService.diaryDetailsForOffenderId(offenderId)
                .map(events -> new ResponseEntity<>(events, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }
}
