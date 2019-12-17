package uk.gov.justice.digital.nomis.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenderActiveBooking {
    @ApiModelProperty(required = true, value = "Display id of the offender", example = "G3641VI", position = 0)
    private String nomsId;
    @ApiModelProperty(required = true, value = "Internal id of the offender", example = "3456456", position = 1)
    private Long offenderId;
    @ApiModelProperty(required = true, value = "First name of the offender", example = "John", position = 2)
    private String firstName;
    @ApiModelProperty(value = "Middle names of the offender", example = "Jim", position = 3)
    private String middleNames;
    @ApiModelProperty(required = true, value = "Surname of the offender", example = "Smith", position = 4)
    private String surname;
    @ApiModelProperty(required = true, value = "Date of birth of the offender", example = "1991-09-17", position = 5)
    private LocalDate dateOfBirth;
    @ApiModelProperty(required = true, value = "Active booking for the offender", position = 6)
    private Booking activeBooking;
}
