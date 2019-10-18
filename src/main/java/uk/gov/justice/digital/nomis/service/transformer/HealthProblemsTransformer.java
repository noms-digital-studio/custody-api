package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.HealthProblem;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderHealthProblem;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.sql.Date;
import java.util.Optional;

@Component
public class HealthProblemsTransformer {

    private static final String HEALTH_PBLM = "HEALTH_PBLM";

    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public HealthProblemsTransformer(final ReferenceCodesRepository referenceCodesRepository) {
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public HealthProblem healthProblemOf(final OffenderHealthProblem offenderHealthProblem) {
        return HealthProblem.builder()
                .bookingId(offenderHealthProblem.getOffenderBooking().getOffenderBookId())
                .caseloadType(offenderHealthProblem.getCaseloadType())
                .description(offenderHealthProblem.getDescription())
                .endDate(Optional.ofNullable(offenderHealthProblem.getEndDate()).map(Date::toLocalDate).orElse(null))
                .offenderHealthProblemId(offenderHealthProblem.getOffenderHealthProblemId())
                .offenderId(offenderHealthProblem.getOffenderBooking().getOffenderId())
                .problemCode(problemCodeOf(offenderHealthProblem))
                .problemStatus(offenderHealthProblem.getProblemStatus())
                .problemType(offenderHealthProblem.getProblemType())
                .startDate(Optional.ofNullable(offenderHealthProblem.getStartDate()).map(Date::toLocalDate).orElse(null))
                .build();
    }

    private KeyValue problemCodeOf(final OffenderHealthProblem offenderHealthProblem) {
        return Optional.ofNullable(offenderHealthProblem.getProblemCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(offenderHealthProblem.getProblemCode())
                        .domain(HEALTH_PBLM)
                        .build()).orElse(null) : null)
                .map(pc -> KeyValue.builder().code(pc.getCode()).description(pc.getDescription()).build())
                .orElse(null);
    }
}
