package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Employment;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderEmployment;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;

import java.util.Optional;

@Component
public class EmploymentsTransformer {

    private static final String OCCUPATION = "OCCUPATION";
    private static final String EMPLOY_STS = "EMPLOY_STS";

    private final TypesTransformer typesTransformer;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public EmploymentsTransformer(final TypesTransformer typesTransformer, final ReferenceCodesRepository referenceCodesRepository) {
        this.typesTransformer = typesTransformer;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public Employment employmentOf(final OffenderEmployment offenderEmployment) {
        return Employment.builder()
                .bookingId(offenderEmployment.getOffenderBookId())
                .caseloadType(offenderEmployment.getCaseloadType())
                .certification(offenderEmployment.getCertification())
                .comments(offenderEmployment.getCommentText())
                .contactEmployer(typesTransformer.ynToBoolean(offenderEmployment.getContactEmployerFlag()))
                .contactNumber(offenderEmployment.getContactNumber())
                .contactType(offenderEmployment.getContactType())
                .employerAware(typesTransformer.ynToBoolean(offenderEmployment.getEmployerAwareFlag()))
                .employerName(offenderEmployment.getEmployerName())
                .employmentDate(typesTransformer.localDateOf(offenderEmployment.getEmploymentDate()))
                .employmentPostCode(employmentPostCodeOf(offenderEmployment))
                .employmentSchedule(offenderEmployment.getEmploymentSchedule())
                .employmentSequence(offenderEmployment.getEmploySeq())
                .employmentType(offenderEmployment.getEmploymentType())
                .hoursWeek(offenderEmployment.getHoursWeek())
                .occupationsCode(occupationCodeOf(offenderEmployment))
                .offenderEmploymentId(offenderEmployment.getOffenderEmploymentId())
                .partialEmploymentDate(typesTransformer.ynToBoolean(offenderEmployment.getPartialEmploymentDateFlag()))
                .partialTerminationDate(typesTransformer.ynToBoolean(offenderEmployment.getPartialTerminationDateFlag()))
                .position(offenderEmployment.getPosition())
                .scheduleHours(offenderEmployment.getScheduleHours())
                .scheduleType(offenderEmployment.getScheduleType())
                .supervisorName(offenderEmployment.getSupervisorName())
                .terminationDate(typesTransformer.localDateOf(offenderEmployment.getTerminationDate()))
                .terminationReason(offenderEmployment.getTerminationReasonText())
                .wage(offenderEmployment.getWage())
                .wagePeriodCode(offenderEmployment.getWagePeriodCode())
                .createdDateTime(typesTransformer.localDateTimeOf(offenderEmployment.getCreateDatetime()))
                .modifiedDateTime(typesTransformer.localDateTimeOf(offenderEmployment.getModifyDatetime()))
                .build();
    }

    private KeyValue employmentPostCodeOf(final OffenderEmployment offenderEmployment) {
        return Optional.ofNullable(offenderEmployment.getEmploymentPostCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(offenderEmployment.getEmploymentPostCode())
                        .domain(EMPLOY_STS)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }

    private KeyValue occupationCodeOf(final OffenderEmployment offenderEmployment) {
        return Optional.ofNullable(offenderEmployment.getOccupationsCode() != null ?
                referenceCodesRepository.findById(ReferenceCodePK.builder()
                        .code(offenderEmployment.getOccupationsCode())
                        .domain(OCCUPATION)
                        .build()).orElse(null) : null)
                .map(rc -> KeyValue.builder().code(rc.getCode()).description(rc.getDescription()).build())
                .orElse(null);
    }
}
