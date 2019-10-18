package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.NomisCaseload;
import uk.gov.justice.digital.nomis.jpa.entity.Caseload;

import java.util.stream.Collectors;

@Component
public class CaseloadTransformer {

    private final ReferenceDataTransformer referenceDataTransformer;

    @Autowired
    public CaseloadTransformer(final ReferenceDataTransformer referenceDataTransformer) {
        this.referenceDataTransformer = referenceDataTransformer;
    }

    public NomisCaseload caseloadsOf(final Caseload caseload) {
        return NomisCaseload.builder()
                .id(caseload.getId())
                .name(caseload.getName())
                .type(caseload.getType())
                .agencies(caseload.getAgencyLocations().stream()
                        .map(agency -> referenceDataTransformer.agencyLocationOf(agency.getAgencyLocation()))
                        .collect(Collectors.toList()))
                .build();
    }


}
