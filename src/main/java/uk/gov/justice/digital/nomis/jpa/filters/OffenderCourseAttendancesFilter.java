package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCourseAttendance;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
public class OffenderCourseAttendancesFilter implements Specification<OffenderCourseAttendance> {

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime from = null;

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final LocalDateTime to = null;

    private final Long bookingId;

    @Override
    public Predicate toPredicate(final Root<OffenderCourseAttendance> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        var tsFrom = Timestamp.valueOf(from);
        var tsTo = Timestamp.valueOf(to);

        if (tsFrom.after(tsTo)) {
            final var tsTemp = tsFrom;
            tsFrom = tsTo;
            tsTo = tsTemp;
        }

        predicateBuilder.add(cb.equal(root.get("offenderBookId"), bookingId))
                .add(cb.or(cb.lessThanOrEqualTo(root.get("eventDate"), tsTo), root.get("eventDate").isNull()))

                .add(cb.or(cb.greaterThanOrEqualTo(root.get("eventDate"), tsFrom), root.get("eventDate").isNull()));

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

}

