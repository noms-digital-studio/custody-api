package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.CourseAttendance;
import uk.gov.justice.digital.nomis.api.Exclusion;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCourseAttendance;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExcludeActsSchds;

import java.util.Optional;

@Component
public class AttendanceAndExclusionTransformer {

    private final TypesTransformer typesTransformer;
    private final OffenderProgrammeProfileTransformer offenderProgrammeProfileTransformer;
    private final ReferenceDataTransformer referenceDataTransformer;

    @Autowired
    public AttendanceAndExclusionTransformer(final TypesTransformer typesTransformer, final OffenderProgrammeProfileTransformer offenderProgrammeProfileTransformer, final ReferenceDataTransformer referenceDataTransformer) {
        this.typesTransformer = typesTransformer;
        this.offenderProgrammeProfileTransformer = offenderProgrammeProfileTransformer;
        this.referenceDataTransformer = referenceDataTransformer;
    }

    public CourseAttendance courseAttendanceOf(final OffenderCourseAttendance offenderCourseAttendance) {
        return Optional.ofNullable(offenderCourseAttendance)
                .map(ca -> CourseAttendance.builder()
                        .actionCode(ca.getActionCode())
                        .agreedTravelHour(ca.getAgreedTravelHour())
                        .agencyLocation(referenceDataTransformer.agencyLocationOf(ca.getAgencyLocation()))
                        .authorisedAbsence(typesTransformer.ynToBoolean(ca.getAuthorisedAbsenceFlag()))
                        .behaviourCode(ca.getBehaviourCode())
                        .bonusPay(ca.getBonusPay())
                        .bookingId(ca.getOffenderBookId())
                        .comments(ca.getCommentText())
                        .courseActivity(offenderProgrammeProfileTransformer.activityOf(ca.getCourseActivity()))
                        .courseSchedule(offenderProgrammeProfileTransformer.scheduleOf(ca.getCourseSchedule()))
                        .creditedHours(ca.getCreditedHours())
                        .crsApptId(ca.getCrsApptId())
                        .details(ca.getDetails())
                        .endTime(typesTransformer.localTimeOf(ca.getEndTime()))
                        .engagementCode(ca.getEngagementCode())
                        .eventDate(typesTransformer.localDateOf(ca.getEventDate()))
                        .eventId(ca.getEventId())
                        .eventOutcome(ca.getEventOutcome())
                        .eventStatus(ca.getEventStatus())
                        .eventSubType(ca.getEventSubType())
                        .eventType(ca.getEventType())
                        .hiddenComments(ca.getHiddenCommentText())
                        .inTime(typesTransformer.localTimeOf(ca.getInTime()))
                        .offenderCourseApptRuleId(ca.getOffenderCourseApptRuleId())
                        .offenderPrgObligationId(ca.getOffenderPrgObligationId())
                        .offenderProgramProfileId(ca.getOffPrgrefId())
                        .outcomeReasonCode(ca.getOutcomeReasonCode())
                        .outTime(typesTransformer.localTimeOf(ca.getOutTime()))
                        .payFlag(ca.getPayFlag())
                        .performanceCode(ca.getPerformanceCode())
                        .pieceWork(ca.getPieceWork())
                        .programId(ca.getProgramId())
                        .referenceId(ca.getReferenceId())
                        .sessionNo(ca.getSessionNo())
                        .sickNoteExpiryDate(typesTransformer.localDateOf(ca.getSickNoteExpiryDate()))
                        .sickNoteReceivedDate(typesTransformer.localDateOf(ca.getSickNoteReceivedDate()))
                        .startTime(typesTransformer.localTimeOf(ca.getStartTime()))
                        .supervisorName(ca.getSupervisorName())
                        .supervisorStaffId(ca.getSupervisorStaffId())
                        .toAddressId(ca.getToAddressId())
                        .toAddressOwnerClass(ca.getToAddressOwnerClass())
                        .toAgencyLocation(referenceDataTransformer.agencyLocationOf(ca.getToAgencyLocation()))
                        .toInternalLocation(referenceDataTransformer.agencyInternalLocationOf(ca.getToInternalLocation()))
                        .understandingCode(ca.getUnderstandingCode())
                        .unexcusedAbsence(typesTransformer.ynToBoolean(ca.getUnexcusedAbsenceFlag()))
                        .build())
                .orElse(null);
    }

    public Exclusion exclusionOf(final OffenderExcludeActsSchds offenderExcludeActsSchds) {
        return Optional.ofNullable(offenderExcludeActsSchds).map(
                exclude -> Exclusion.builder()
                        .bookingId(exclude.getOffenderBookId())
                        .courseActivity(offenderProgrammeProfileTransformer.activityOf(exclude.getCourseActivity()))
                        .excludeDay(exclude.getExcludeDay())
                        .offenderExcludeActSchdId(exclude.getOffenderExcludeActSchdId())
                        .offenderProgramProfileId(exclude.getOffPrgrefId())
                        .slotCategoryCode(exclude.getSlotCategoryCode())
                        .build())
                .orElse(null);

    }
}
