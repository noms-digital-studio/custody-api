package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderExternalMovement;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@EqualsAndHashCode
public class MovementsFilter implements Specification<OffenderExternalMovement> {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private final Optional<LocalDateTime> from = Optional.empty();
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private final Optional<LocalDateTime> to = Optional.empty();
    @Builder.Default
    private final Optional<Long> bookingId = Optional.empty();

    @Override
    public Predicate toPredicate(final Root<OffenderExternalMovement> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        from.ifPresent(localDateTime -> predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("movementTime"), Timestamp.valueOf(localDateTime))));

        to.ifPresent(localDateTime -> predicateBuilder.add(cb.lessThanOrEqualTo(root.get("movementTime"), Timestamp.valueOf(localDateTime))));

        bookingId.ifPresent(bookingId -> predicateBuilder.add(cb.equal(root.get("id").get("offenderBooking").get("offenderBookId"), bookingId)));

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
