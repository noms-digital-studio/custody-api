package uk.gov.justice.digital.nomis.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Physicals {
    private Long bookingId;
    private List<IdentifyingMark> identifyingMarks;
    private List<PhysicalAttribute> physicalAttributes;
    private List<ProfileDetails> profileDetails;
}
