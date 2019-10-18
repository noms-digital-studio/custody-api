package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;

import javax.persistence.criteria.*;

@Builder
@EqualsAndHashCode
public class OffenderBookingFilter implements Specification<OffenderBooking> {

    private final String agencyLocationId;
    private final String activeFlag;


    @Override
    public Predicate toPredicate(final Root<OffenderBooking> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        predicateBuilder.add(cb.equal(root.get("activeFlag"), activeFlag));

        predicateBuilder.add(cb.equal(root.get("agyLocId"),agencyLocationId));

        final var predicates = predicateBuilder.build();

        if (Long.class != query.getResultType()) {
            root.fetch("offender", JoinType.INNER);
        }

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

}

