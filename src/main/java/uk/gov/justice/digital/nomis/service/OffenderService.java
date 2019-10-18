package uk.gov.justice.digital.nomis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Offender;
import uk.gov.justice.digital.nomis.api.OffenderActiveBooking;
import uk.gov.justice.digital.nomis.jpa.filters.OffenderBookingFilter;
import uk.gov.justice.digital.nomis.jpa.repository.AgencyLocationsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenderActiveBookingTransformer;
import uk.gov.justice.digital.nomis.service.transformer.OffenderTransformer;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class OffenderService {

    private final OffenderRepository offenderRepository;
    private final AgencyLocationsRepository agencyLocationRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final OffenderTransformer offenderTransformer;
    private final OffenderActiveBookingTransformer offenderActiveBookingTransformer;

    @Autowired
    public OffenderService(final OffenderRepository offenderRepository, final AgencyLocationsRepository agencyLocationRepository, final OffenderBookingRepository offenderBookingRepository, final OffenderTransformer offenderTransformer, final OffenderActiveBookingTransformer offenderActiveBookingTransformer) {
        this.offenderRepository = offenderRepository;
        this.agencyLocationRepository = agencyLocationRepository;
        this.offenderBookingRepository = offenderBookingRepository;
        this.offenderTransformer = offenderTransformer;
        this.offenderActiveBookingTransformer = offenderActiveBookingTransformer;
    }

    @Transactional
    public Page<Offender> getOffenders(final Pageable pageable) {
        final var rootOffendersRawPage = offenderRepository.findAllRootOffenders(pageable);

        final var rootOffenders = rootOffendersRawPage.getContent();

        final var offenderList = rootOffenders
                .stream()
                .map(offenderTransformer::offenderOf)
                .collect(Collectors.toList());

        return new PageImpl<>(offenderList, pageable, rootOffendersRawPage.getTotalElements());
    }

    @Transactional
    public Optional<Offender> getOffenderByOffenderId(final Long offenderId) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.map(offenderTransformer::offenderOf);
    }

    @Transactional
    public Optional<Offender> getOffenderByNomsId(final String nomsId) {
        final var maybeOffender = offenderRepository.findByNomsId(nomsId);

        return maybeOffender.map(offenderTransformer::offenderOf);
    }

    public Page<OffenderActiveBooking> getOffendersByPrison(final String agencyLocationId, final Pageable pageable) {
        agencyLocationRepository.getByAgyLocId(agencyLocationId).orElseThrow(() -> new EntityNotFoundException(String.format("Agency location %s not found", agencyLocationId)));

        final var offenderBookingFilter = OffenderBookingFilter.builder()
                .agencyLocationId(agencyLocationId)
                .activeFlag("Y")
                .build();

        final var rootOffendersRawPage = offenderBookingRepository.findAll(offenderBookingFilter, pageable);

        final var rootOffenders = rootOffendersRawPage.getContent();

        final var offenderList = rootOffenders
                .stream()
                .map(offenderActiveBookingTransformer::offenderOf)
                .collect(Collectors.toList());

        return new PageImpl<>(offenderList, pageable, rootOffendersRawPage.getTotalElements());
    }
}
