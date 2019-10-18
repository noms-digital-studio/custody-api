package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCharge;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Set;

@Builder
@EqualsAndHashCode
public class ChargesFilter implements Specification<OffenderCharge> {

    private final Set<Long> bookingIds;

    @Override
    public Predicate toPredicate(final Root<OffenderCharge> table, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (!CollectionUtils.isEmpty(bookingIds)) {
            predicateBuilder.add(table.get("offenderBooking").get("offenderBookId").in(bookingIds));
        }
        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}

