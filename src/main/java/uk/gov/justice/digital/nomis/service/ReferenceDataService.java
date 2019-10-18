package uk.gov.justice.digital.nomis.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.AgencyInternalLocation;
import uk.gov.justice.digital.nomis.api.AgencyLocation;
import uk.gov.justice.digital.nomis.jpa.repository.AgencyInternalLocationsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.AgencyLocationsRepository;
import uk.gov.justice.digital.nomis.service.transformer.ReferenceDataTransformer;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReferenceDataService {

    private final AgencyLocationsRepository agencyLocationsRepository;
    private final AgencyInternalLocationsRepository agencyInternalLocationsRepository;
    private final ReferenceDataTransformer referenceDataTransformer;

    public ReferenceDataService(final AgencyLocationsRepository agencyLocationsRepository, final AgencyInternalLocationsRepository agencyInternalLocationsRepository, final ReferenceDataTransformer referenceDataTransformer) {
        this.agencyLocationsRepository = agencyLocationsRepository;
        this.agencyInternalLocationsRepository = agencyInternalLocationsRepository;
        this.referenceDataTransformer = referenceDataTransformer;
    }

    @Transactional
    public Page<AgencyLocation> getLocations(final Pageable pageable) {
        final var agencyLocations = agencyLocationsRepository.findAll(pageable);

        final var agencyLocationList = agencyLocations.getContent().stream().map(
                referenceDataTransformer::agencyLocationOf).collect(Collectors.toList());

        return new PageImpl<>(agencyLocationList, pageable, agencyLocations.getTotalElements());
    }

    @Transactional
    public Page<AgencyInternalLocation> getInternalLocations(final Pageable pageable) {
        final var agencyInternalLocations = agencyInternalLocationsRepository.findAll(pageable);

        final var agencyInternalLocationList = agencyInternalLocations.getContent().stream().map(
                referenceDataTransformer::agencyInternalLocationOf).collect(Collectors.toList());

        return new PageImpl<>(agencyInternalLocationList, pageable, agencyInternalLocations.getTotalElements());
    }

}
