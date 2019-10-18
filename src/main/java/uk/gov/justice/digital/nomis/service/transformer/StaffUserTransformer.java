package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.NomisStaffUser;
import uk.gov.justice.digital.nomis.jpa.entity.StaffUserAccount;

import java.util.stream.Collectors;

@Component
public class StaffUserTransformer {

    private final CaseloadTransformer caseloadTransformer;

    @Autowired
    public StaffUserTransformer(final CaseloadTransformer caseloadTransformer) {
        this.caseloadTransformer = caseloadTransformer;
    }

    public NomisStaffUser userOf(final StaffUserAccount staffUserAccount) {
        final var nomisStaffUser = NomisStaffUser.builder()
                .username(staffUserAccount.getUsername())
                .firstName(staffUserAccount.getStaff().getFirstName())
                .lastName(staffUserAccount.getStaff().getLastName())
                .staffId(staffUserAccount.getStaff().getStaffId())
                .status(staffUserAccount.getStaff().getStatus())
                .nomisCaseloads(staffUserAccount.getCaseloads().stream()
                        .collect(Collectors.toMap(uac -> uac.getCaseload().getId(), uac -> caseloadTransformer.caseloadsOf(uac.getCaseload()))))
                .build();

        // mark which caseload is current
        if (staffUserAccount.getActiveCaseload() != null) {
            nomisStaffUser.getNomisCaseloads().values().stream()
                    .filter(c -> c.getId().equals(staffUserAccount.getActiveCaseload().getId()))
                    .findFirst()
                    .ifPresent(f -> f.setCurrentActive(true));

            nomisStaffUser.setActiveNomisCaseload(staffUserAccount.getActiveCaseload().getId());
        }

        return nomisStaffUser;


    }


}
