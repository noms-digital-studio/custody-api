package uk.gov.justice.digital.nomis.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentifyingMark {
    private Long idMarkSeq;
    private String bodyPartCode;
    private String markType;
    private String sideCode;
    private String partOrientationCode;
    private String comments;
}
