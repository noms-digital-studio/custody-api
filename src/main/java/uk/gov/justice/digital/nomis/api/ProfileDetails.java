package uk.gov.justice.digital.nomis.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDetails {
    private Long profileSeq;
    private String profileType;
    private String profileCode;
    private String profileDescription;
    private Long listSeq;
    private String comments;
    private String caseloadType;
}
