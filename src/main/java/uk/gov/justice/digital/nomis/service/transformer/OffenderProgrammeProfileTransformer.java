package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.Activity;
import uk.gov.justice.digital.nomis.api.ProgrammeProfile;
import uk.gov.justice.digital.nomis.api.Schedule;
import uk.gov.justice.digital.nomis.jpa.entity.CourseActivity;
import uk.gov.justice.digital.nomis.jpa.entity.CourseSchedule;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderProgramProfile;
import uk.gov.justice.digital.nomis.jpa.filters.CourseActivitiesFilter;
import uk.gov.justice.digital.nomis.jpa.filters.CourseSchedulesFilter;
import uk.gov.justice.digital.nomis.jpa.repository.CourseActivitiesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.CourseSchedulesRepository;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OffenderProgrammeProfileTransformer {

    private final TypesTransformer typesTransformer;
    private final CourseActivitiesRepository courseActivitiesRepository;
    private final CourseSchedulesRepository courseSchedulesRepository;
    private final ReferenceDataTransformer referenceDataTransformer;

    @Autowired
    public OffenderProgrammeProfileTransformer(final TypesTransformer typesTransformer, final CourseActivitiesRepository courseActivitiesRepository, final CourseSchedulesRepository courseSchedulesRepository, final ReferenceDataTransformer referenceDataTransformer) {
        this.typesTransformer = typesTransformer;
        this.courseActivitiesRepository = courseActivitiesRepository;
        this.courseSchedulesRepository = courseSchedulesRepository;
        this.referenceDataTransformer = referenceDataTransformer;
    }

    public ProgrammeProfile programmeProfileOf(final OffenderProgramProfile offenderProgramProfile, final LocalDateTime from, final LocalDateTime to) {
        return ProgrammeProfile.builder()
                .agencyLocation(referenceDataTransformer.agencyLocationOf(offenderProgramProfile.getAgencyLocation()))
                .bookingId(offenderProgramProfile.getOffenderBookId())
                .courseActivity(activityAndSchedulesOf(courseActivitiesRepository.findOne(CourseActivitiesFilter.builder()
                        .courseActivityId(offenderProgramProfile.getCrsActyId())
                        .from(from)
                        .to(to)
                        .build()), from, to))
                .offenderEndDate(typesTransformer.localDateOf(offenderProgramProfile.getOffenderEndDate()))
                .offenderProgramStatus(offenderProgramProfile.getOffenderProgramStatus())
                .offenderStartDate(typesTransformer.localDateOf(offenderProgramProfile.getOffenderStartDate()))
                .programProfileId(offenderProgramProfile.getOffPrgrefId())
                .startSessionNo(offenderProgramProfile.getStartSessionNo())
                .suspended(typesTransformer.ynToBoolean(offenderProgramProfile.getSuspendedFlag()))
                .build();
    }

    public Activity activityAndSchedulesOf(final Optional<CourseActivity> courseActivity, final LocalDateTime from, final LocalDateTime to) {
        return courseActivity
                .map(ca -> activityOf(ca).toBuilder()
                        .schedules(courseSchedulesRepository.findAll(CourseSchedulesFilter.builder()
                                .courseActivityId(ca.getCrsActyId())
                                .from(from)
                                .to(to)
                                .build())
                                .stream()
                                .map(this::scheduleOf)
                                .collect(Collectors.toList()))
                        .build())
                .orElse(null);
    }

    public Activity activityOf(final CourseActivity courseActivity) {
        return Optional.ofNullable(courseActivity)
                .map(ca -> Activity.builder()
                        .courseActivityId(ca.getCrsActyId())
                        .agencyLocation(referenceDataTransformer.agencyLocationOf(ca.getAgencyLocation()))
                        .livingUnit(referenceDataTransformer.agencyInternalLocationOf(ca.getInternalLocation()))
                        .description(ca.getDescription())
                        .active(typesTransformer.ynToBoolean(ca.getActiveFlag()))
                        .outsideWork(typesTransformer.ynToBoolean(ca.getOutsideWorkFlag()))
                        .scheduledStartDate(typesTransformer.localDateOf(ca.getScheduleStartDate()))
                        .scheduledEndDate(typesTransformer.localDateOf(ca.getScheduleEndDate()))
                        .build())
                .orElse(null);
    }

    public Schedule scheduleOf(final CourseSchedule courseSchedule) {
        return Optional.ofNullable(courseSchedule)
                .map(cs -> Schedule.builder()
                        .catchUpCourseScheduleId(cs.getCatchUpCrsSchId())
                        .courseScheduleId(cs.getCrsSchId())
                        .endTime(typesTransformer.localDateTimeOf(cs.getEndTime()).toLocalTime())
                        .scheduleDay(typesTransformer.localDateOf(cs.getScheduleDate()).getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .scheduledDate(typesTransformer.localDateOf(cs.getScheduleDate()))
                        .sessionNo(cs.getSessionNo())
                        .startTime(typesTransformer.localDateTimeOf(cs.getStartTime()).toLocalTime())
                        .build())
                .orElse(null);
    }
}
