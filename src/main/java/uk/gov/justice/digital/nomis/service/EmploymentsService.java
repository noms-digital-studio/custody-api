package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Employment;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.EmploymentsTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EmploymentsService {

    private static final Comparator<Employment> BY_EMPLOYMENT_SEQUENCE =
                    Comparator.comparing(Employment::getModifiedDateTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                    .thenComparing(Employment::getCreatedDateTime)
                    .thenComparing(Employment::getEmploymentSequence)
                    .reversed();

    private final EmploymentsTransformer employmentsTransformer;
    private final OffenderRepository offenderRepository;

    @Autowired
    public EmploymentsService(final EmploymentsTransformer employmentsTransformer, final OffenderRepository offenderRepository) {
        this.employmentsTransformer = employmentsTransformer;
        this.offenderRepository = offenderRepository;
    }

    public Optional<List<Employment>> employmentsForOffenderId(final Long offenderId) {
        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings().stream()
                        .flatMap(offenderBooking -> offenderBooking.getOffenderEmployments().stream()
                                .map(employmentsTransformer::employmentOf))
                                .sorted(BY_EMPLOYMENT_SEQUENCE)
                        .collect(Collectors.toList()));
    }

}
