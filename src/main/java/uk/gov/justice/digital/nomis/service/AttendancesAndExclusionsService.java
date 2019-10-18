package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.CourseAttendance;
import uk.gov.justice.digital.nomis.api.Exclusion;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCourseAttendance;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExcludeActsSchds;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderCourseAttendancesFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderCourseAttendancesRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderExcludeActsSchdsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.AttendanceAndExclusionTransformer;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AttendancesAndExclusionsService {

    private static final Comparator<OffenderExcludeActsSchds> BY_EXCLUSION_PRIORITY = Comparator
            .comparing((OffenderExcludeActsSchds oe) -> Optional.ofNullable(oe.getModifyDatetime()).orElse(new Timestamp(0)))
            .thenComparing((OffenderExcludeActsSchds oe) -> Optional.ofNullable(oe.getCreateDatetime()).orElse(new Timestamp(0)))
            .reversed();
    private static final Comparator<? super OffenderCourseAttendance> BY_COURSE_ATTENDENCE_PRIORITY = Comparator
            .comparing((OffenderCourseAttendance oca) -> Optional.ofNullable(oca.getEventDate()).orElse(new Timestamp(0)))
            .thenComparing((OffenderCourseAttendance oca) -> Optional.ofNullable(oca.getStartTime()).orElse(new Timestamp(0)))
            .reversed();
    private final OffenderRepository offenderRepository;
    private final AttendanceAndExclusionTransformer attendanceAndExclusionTransformer;
    private final OffenderCourseAttendancesRepository offenderCourseAttendancesRepository;
    private final OffenderExcludeActsSchdsRepository offenderExcludeActsSchdsRepository;

    @Autowired
    public AttendancesAndExclusionsService(final OffenderRepository offenderRepository,
                                           final AttendanceAndExclusionTransformer attendanceAndExclusionTransformer,
                                           final OffenderCourseAttendancesRepository offenderCourseAttendancesRepository,
                                           final OffenderExcludeActsSchdsRepository offenderExcludeActsSchdsRepository) {
        this.offenderRepository = offenderRepository;
        this.attendanceAndExclusionTransformer = attendanceAndExclusionTransformer;
        this.offenderCourseAttendancesRepository = offenderCourseAttendancesRepository;
        this.offenderExcludeActsSchdsRepository = offenderExcludeActsSchdsRepository;
    }

    public Optional<List<CourseAttendance>> offenderCourseAttendancesForOffenderId(final Long offenderId, final Optional<LocalDateTime> maybeFrom, final Optional<LocalDateTime> maybeTo) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        final var maybeOffenderBookings = maybeOffender.map(Offender::getOffenderBookings);

        final var from = maybeFrom.orElse(maybeTo.orElse(LocalDate.now().atStartOfDay()));
        final var to = maybeTo.orElse(from.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1));

        return maybeOffenderBookings.map(bookings -> bookings
                .stream()
                .flatMap(ob -> offenderCourseAttendancesRepository.findAll(OffenderCourseAttendancesFilter.builder()
                        .bookingId(ob.getOffenderBookId())
                        .from(from)
                        .to(to)
                        .build())
                        .stream())
                .sorted(BY_COURSE_ATTENDENCE_PRIORITY)
                .map(attendanceAndExclusionTransformer::courseAttendanceOf)
                .collect(Collectors.toList()));
    }

    public Optional<List<CourseAttendance>> offenderCourseAttendancesForOffenderIdAndBookingId(final Long offenderId, final Long bookingId, final Optional<LocalDateTime> maybeFrom, final Optional<LocalDateTime> maybeTo) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        final var from = maybeFrom.orElse(maybeTo.orElse(LocalDate.now().atStartOfDay()));
        final var to = maybeTo.orElse(from.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1));

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> offenderCourseAttendancesRepository.findAll(OffenderCourseAttendancesFilter.builder()
                        .bookingId(ob.getOffenderBookId())
                        .from(from)
                        .to(to)
                        .build())
                        .stream()
                        .sorted(BY_COURSE_ATTENDENCE_PRIORITY)
                        .map(attendanceAndExclusionTransformer::courseAttendanceOf)
                        .collect(Collectors.toList()));
    }

    public Optional<List<Exclusion>> exclusionsForOffenderIdAndBookingId(final Long offenderId, final Long bookingId, final Optional<LocalDateTime> maybeFrom, final Optional<LocalDateTime> maybeTo) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        final var from = maybeFrom.orElse(maybeTo.orElse(LocalDate.now().atStartOfDay()));
        final var to = maybeTo.orElse(from.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1));

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderExcludeActsSchds()
                        .stream()
                        .sorted(BY_EXCLUSION_PRIORITY)
                        .map(attendanceAndExclusionTransformer::exclusionOf)
                        .collect(Collectors.toList()));
    }

    public Optional<List<Exclusion>> exclusionsForOffenderId(final Long offenderId, final Optional<LocalDateTime> maybeFrom, final Optional<LocalDateTime> maybeTo) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        final var maybeOffenderBookings = maybeOffender.map(Offender::getOffenderBookings);

        final var from = maybeFrom.orElse(maybeTo.orElse(LocalDate.now().atStartOfDay()));
        final var to = maybeTo.orElse(from.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1));

        return maybeOffenderBookings.map(bookings -> bookings
                .stream()
                .flatMap(ob -> ob.getOffenderExcludeActsSchds().stream())
                .sorted(BY_EXCLUSION_PRIORITY)
                .map(attendanceAndExclusionTransformer::exclusionOf)
                .collect(Collectors.toList()));
    }
}
