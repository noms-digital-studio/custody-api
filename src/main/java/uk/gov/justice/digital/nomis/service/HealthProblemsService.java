package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.HealthProblem;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.repository.HealthProblemsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.HealthProblemsTransformer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HealthProblemsService {

    private final HealthProblemsTransformer healthProblemsTransformer;
    private final HealthProblemsRepository healthProblemsRepository;
    private final OffenderRepository offenderRepository;

    @Autowired
    public HealthProblemsService(final HealthProblemsTransformer healthProblemsTransformer, final HealthProblemsRepository healthProblemsRepository, final OffenderRepository offenderRepository) {
        this.healthProblemsTransformer = healthProblemsTransformer;
        this.healthProblemsRepository = healthProblemsRepository;
        this.offenderRepository = offenderRepository;
    }

    @Transactional
    public Page<HealthProblem> getHealthProblems(final Pageable pageable) {
        final var rawHealthProblemsPage = healthProblemsRepository.findAll(pageable);

        final var healthProblems = rawHealthProblemsPage.getContent().stream().map(
                healthProblemsTransformer::healthProblemOf
        ).collect(Collectors.toList());

        return new PageImpl<>(healthProblems, pageable, rawHealthProblemsPage.getTotalElements());
    }

    public Optional<List<HealthProblem>> healthProblemsForOffenderId(final Long offenderId) {

        final var maybeOffenderHealthProblems = offenderRepository.findById(offenderId)
                .map(offender ->
                        offender.getOffenderBookings().stream()
                                .map(OffenderBooking::getOffenderHealthProblems)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));

        return maybeOffenderHealthProblems.map(healthProblems -> healthProblems.stream().map(healthProblemsTransformer::healthProblemOf).collect(Collectors.toList()));
    }

    public Optional<List<HealthProblem>> healthProblemsForOffenderIdAndBookingId(final Long offenderId, final Long bookingId) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderHealthProblems()
                        .stream()
                        .map(healthProblemsTransformer::healthProblemOf)
                        .collect(Collectors.toList()));
    }

}
