package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.RehabDecision;
import uk.gov.justice.digital.nomis.api.RehabProvider;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderRehabDecision;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderRehabProvider;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRehabProviderRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.RehabDecisionTransformer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RehabDecisionService {

    private final RehabDecisionTransformer rehabDecisionTransformer;
    private final OffenderRepository offenderRepository;
    private final OffenderRehabProviderRepository offenderRehabProviderRepository;
    private final Comparator<? super OffenderRehabDecision> BY_ACTIVE_REHAB_DECISION =
            Comparator.comparing(OffenderRehabDecision::getActiveFlag)
                    .thenComparing(OffenderRehabDecision::getOffenderRehabDecisionId)
                    .reversed();

    private final Comparator<? super OffenderRehabProvider> BY_ACTIVE_REHAB_PROVIDER =
            Comparator.comparing(OffenderRehabProvider::getActiveFlag)
                    .thenComparing(OffenderRehabProvider::getOffenderRehabProviderId)
                    .reversed();

    @Autowired
    public RehabDecisionService(RehabDecisionTransformer rehabDecisionTransformer, OffenderRepository offenderRepository, OffenderRehabProviderRepository offenderRehabProviderRepository) {
        this.rehabDecisionTransformer = rehabDecisionTransformer;
        this.offenderRepository = offenderRepository;
        this.offenderRehabProviderRepository = offenderRehabProviderRepository;
    }

    public Optional<List<RehabDecision>> rehabDecisionsForOffenderId(Long offenderId) {

        final Optional<List<RehabDecision>> maybeRehabDecisions = offenderRepository.findById(offenderId)
                .map(offender ->
                        offender.getOffenderBookings()
                                .stream()
                                .flatMap(offenderBooking -> offenderBooking.getOffenderRehabDecisions().stream())
                                .sorted(BY_ACTIVE_REHAB_DECISION)
                                .map(rehabDecisionTransformer::rehabDecisionOf)
                                .collect(Collectors.toList()));

        return maybeRehabDecisions.map(
                rehabDecisions -> rehabDecisions.stream()
                        .map(rehabDecision -> rehabDecision.toBuilder()
                                .providers(rehabProvidersOf(rehabDecision))
                                .build())
                        .collect(Collectors.toList())
        );
    }

    private List<RehabProvider> rehabProvidersOf(RehabDecision rehabDecision) {
        return Optional.ofNullable(rehabDecision).map(
                rh -> offenderRehabProviderRepository.findByOffenderRehabDecisionId(rh.getOffenderRehabDecisionId())
                        .stream()
                        .sorted(BY_ACTIVE_REHAB_PROVIDER)
                        .map(rehabDecisionTransformer::rehabProviderOf)
                        .collect(Collectors.toList())
        ).orElse(Collections.emptyList());

    }


}
