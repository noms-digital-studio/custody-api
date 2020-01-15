package uk.gov.justice.digital.nomis.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalAttribute {
    private Long attributeSeq;
    private Integer heightFeet;
    private Integer heightInches;
    private Integer heightCm;
    private Integer weightLbs;
    private Integer weightKg;
}
