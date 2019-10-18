package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.nomis.jpa.entity.CourseActivity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
public class CourseActivitiesFilter implements Specification<CourseActivity> {

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime from = null;

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime to = null;

    private final Long courseActivityId;

    @Override
    public Predicate toPredicate(final Root<CourseActivity> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        var tsFrom = Timestamp.valueOf(from);
        var tsTo = Timestamp.valueOf(to);

        if (tsFrom.after(tsTo)) {
            final var tsTemp = tsFrom;
            tsFrom = tsTo;
            tsTo = tsTemp;
        }

        final Root courseActivityTable = root;
        final var courseSchedulesTable = courseActivityTable.join("courseSchedules");

        predicateBuilder.add(cb.equal(courseActivityTable.get("crsActyId"), courseActivityId))
                .add(cb.or(cb.lessThanOrEqualTo(courseActivityTable.get("scheduleStartDate"), tsTo), courseActivityTable.get("scheduleStartDate").isNull()))
                .add(cb.or(cb.lessThanOrEqualTo(courseSchedulesTable.get("scheduleDate"), tsTo), courseSchedulesTable.get("scheduleDate").isNull()))

                .add(cb.or(cb.greaterThanOrEqualTo(courseActivityTable.get("scheduleEndDate"), tsFrom), courseActivityTable.get("scheduleEndDate").isNull()))
                .add(cb.or(cb.greaterThanOrEqualTo(courseSchedulesTable.get("scheduleDate"), tsFrom), courseSchedulesTable.get("scheduleDate").isNull()));

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

}
