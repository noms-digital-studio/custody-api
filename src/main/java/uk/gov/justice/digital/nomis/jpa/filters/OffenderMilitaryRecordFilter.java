package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderMilitaryRecord;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@EqualsAndHashCode
public class OffenderMilitaryRecordFilter implements Specification<OffenderMilitaryRecord> {

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final Optional<LocalDateTime> from = Optional.empty();

    @Builder.Default
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private final Optional<LocalDateTime> to = Optional.empty();

    @Override
    public Predicate toPredicate(final Root<OffenderMilitaryRecord> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        from.ifPresent(localDateTime -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("startDate"), Timestamp.valueOf(localDateTime))));
        from.ifPresent(localDateTime -> predicateBuilder.add(cb.lessThan(root.get("endDate"), Timestamp.valueOf(localDateTime))));

        to.ifPresent(localDateTime -> predicateBuilder.add(cb.greaterThan(root.get("startDate"), Timestamp.valueOf(localDateTime))));
        to.ifPresent(localDateTime -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("endDate"), Timestamp.valueOf(localDateTime))));

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
