package uk.gov.justice.digital.nomis.jpa.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = {"agencyLocationId", "caseloadId"})
@Embeddable
@ToString(of = {"agencyLocationId", "caseloadId"})
public class CaseloadAgencyIdentity implements Serializable {

    @Column(name = "AGY_LOC_ID")
    private String agencyLocationId;

    @Column(name = "caseload_id")
    private String caseloadId;

}
