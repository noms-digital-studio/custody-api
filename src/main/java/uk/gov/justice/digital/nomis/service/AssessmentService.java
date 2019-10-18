package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.OffenderAssessment;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.filters.AssessmentsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderAssessmentRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.AssessmentsTransformer;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AssessmentService {

    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.OffenderAssessment> BY_ASSESSMENT_PRIORITY =
            Comparator
                    .comparing((uk.gov.justice.digital.nomis.jpa.entity.OffenderAssessment oa) -> Optional.ofNullable(oa.getModifyDatetime()).orElse(new Timestamp(0)))
                    .thenComparing(oa -> Optional.ofNullable(oa.getCreateDatetime()).orElse(new Timestamp(0)))
                    .reversed();
    private static final Comparator<OffenderAssessment> BY_ASSESSMENT_STATUS_SEQUENCE =
            Comparator
                .comparing(OffenderAssessment::getAssessStatus)
                .thenComparing(OffenderAssessment::getAssessmentSequence, Comparator.reverseOrder()); // DESC
    private final OffenderAssessmentRepository offenderAssessmentRepository;
    private final OffenderRepository offenderRepository;
    private final AssessmentsTransformer assessmentsTransformer;

    @Autowired
    public AssessmentService(final OffenderAssessmentRepository offenderAssessmentRepository, final OffenderRepository offenderRepository, final AssessmentsTransformer assessmentsTransformer) {
        this.offenderAssessmentRepository = offenderAssessmentRepository;
        this.offenderRepository = offenderRepository;
        this.assessmentsTransformer = assessmentsTransformer;
    }

    @Transactional
    public Page<OffenderAssessment> getAssessments(final Pageable pageable, final Optional<LocalDateTime> maybeFrom, final Optional<LocalDateTime> maybeTo) {
        final var assessmentsFilter = AssessmentsFilter.builder()
                .from(maybeFrom)
                .to(maybeTo)
                .build();

        final var rawOffenderAssessmentsPage = offenderAssessmentRepository.findAll(assessmentsFilter, pageable);

        final var assessments = rawOffenderAssessmentsPage.getContent()
                .stream()
                .sorted(BY_ASSESSMENT_PRIORITY)
                .map(assessmentsTransformer::assessmentOf)
                .collect(Collectors.toList());

        return new PageImpl<>(assessments, pageable, rawOffenderAssessmentsPage.getTotalElements());
    }

    @Transactional
    public Optional<List<OffenderAssessment>> getOffenderAssessments(final Long offenderId) {
        final var maybeOffenderAssessments = offenderRepository.findById(offenderId)
                .map(offender ->
                        offender.getOffenderBookings()
                                .stream()
                                .map(OffenderBooking::getOffenderAssessments)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));

        return maybeOffenderAssessments.map(offenderAssessments -> offenderAssessments
                .stream()
                .map(assessmentsTransformer::assessmentOf)
                .sorted(BY_ASSESSMENT_STATUS_SEQUENCE)
                .collect(Collectors.toList()));
    }

    public Optional<List<OffenderAssessment>> assessmentsForOffenderIdAndBookingId(final Long offenderId, final Long bookingId) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderAssessments()
                        .stream()
                        .map(assessmentsTransformer::assessmentOf)
                        .sorted(BY_ASSESSMENT_STATUS_SEQUENCE)
                        .collect(Collectors.toList()));
    }
}
