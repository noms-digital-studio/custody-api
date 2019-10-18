package uk.gov.justice.digital.nomis.jpa.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderCaseNote;

import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Builder
@EqualsAndHashCode
public class OffenderCaseNotesFilter implements Specification<OffenderCaseNote> {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private final LocalDateTime from = null;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private final LocalDateTime to = null;

    @Builder.Default
    private final Optional<Set<String>> types = Optional.empty();

    @Builder.Default
    private final Optional<Set<String>> subTypes = Optional.empty();

    private final Long bookingId;

    @Override
    public Predicate toPredicate(final Root<OffenderCaseNote> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        var tsFrom = Timestamp.valueOf(from);
        var tsTo = Timestamp.valueOf(to);

        if (tsFrom.after(tsTo)) {
            final var tsTemp = tsFrom;
            tsFrom = tsTo;
            tsTo = tsTemp;
        }

        final Root caseNotesTable = root;
        final var offenderBookId = caseNotesTable.get("offenderBookId");
        final var contactDate = caseNotesTable.get("contactDate");
        final var caseNoteType = caseNotesTable.get("caseNoteType");
        final var caseNoteSubType = caseNotesTable.get("caseNoteSubType");

        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        predicateBuilder.add(cb.equal(offenderBookId, bookingId))
                .add(cb.greaterThanOrEqualTo(contactDate, tsFrom))
                .add(cb.lessThanOrEqualTo(contactDate, tsTo))
        ;

        types.ifPresent(filter -> predicateBuilder.add(valueInList(cb, caseNoteType, filter)));
        subTypes.ifPresent(filter -> predicateBuilder.add(valueInList(cb, caseNoteSubType, filter)));

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate valueInList(final CriteriaBuilder cb, final Path eventType, final Set<String> list) {
        final var inTypes = cb.in(eventType);
        list.stream().map(String::toUpperCase).forEach(inTypes::value);
        return inTypes;
    }

}
