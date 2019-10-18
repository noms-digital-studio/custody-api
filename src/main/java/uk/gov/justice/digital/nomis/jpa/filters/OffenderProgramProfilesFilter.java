package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderProgramProfile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
public class OffenderProgramProfilesFilter implements Specification<OffenderProgramProfile> {

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime from = null;

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime to = null;

    private final Long bookingId;

    @Override
    public Predicate toPredicate(final Root<OffenderProgramProfile> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        var tsFrom = Timestamp.valueOf(from);
        var tsTo = Timestamp.valueOf(to);

        if (tsFrom.after(tsTo)) {
            final var tsTemp = tsFrom;
            tsFrom = tsTo;
            tsTo = tsTemp;
        }

        final Root offenderProgramProfileTable = root;
        final var courseActivityTable = offenderProgramProfileTable.join("courseActivity");
        final var courseSchedulesTable = courseActivityTable.join("courseSchedules");

        predicateBuilder.add(cb.equal(offenderProgramProfileTable.get("offenderBookId"), bookingId))
                .add(cb.or(cb.lessThanOrEqualTo(offenderProgramProfileTable.get("offenderStartDate"), tsTo), offenderProgramProfileTable.get("offenderStartDate").isNull()))
                .add(cb.or(cb.lessThanOrEqualTo(courseActivityTable.get("scheduleStartDate"), tsTo), courseActivityTable.get("scheduleStartDate").isNull()))
                .add(cb.or(cb.lessThanOrEqualTo(courseSchedulesTable.get("scheduleDate"), tsTo), courseSchedulesTable.get("scheduleDate").isNull()))

                .add(cb.or(cb.greaterThanOrEqualTo(offenderProgramProfileTable.get("offenderEndDate"), tsFrom), offenderProgramProfileTable.get("offenderEndDate").isNull()))
                .add(cb.or(cb.greaterThanOrEqualTo(courseActivityTable.get("scheduleEndDate"), tsFrom), courseActivityTable.get("scheduleEndDate").isNull()))
                .add(cb.or(cb.greaterThanOrEqualTo(courseSchedulesTable.get("scheduleDate"), tsFrom), courseSchedulesTable.get("scheduleDate").isNull()));

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

}

