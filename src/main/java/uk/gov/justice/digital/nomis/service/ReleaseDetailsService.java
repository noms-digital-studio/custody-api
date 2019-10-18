package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.ReleaseDetails;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderReleaseDetailsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.ReleaseDetailsTransformer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReleaseDetailsService {

    private final ReleaseDetailsTransformer releaseDetailsTransformer;
    private final OffenderReleaseDetailsRepository releaseDetailsRepository;
    private final OffenderRepository offenderRepository;

    @Autowired
    public ReleaseDetailsService(final ReleaseDetailsTransformer releaseDetailsTransformer, final OffenderReleaseDetailsRepository releaseDetailsRepository, final OffenderRepository offenderRepository) {
        this.releaseDetailsTransformer = releaseDetailsTransformer;
        this.releaseDetailsRepository = releaseDetailsRepository;
        this.offenderRepository = offenderRepository;
    }

    @Transactional
    public Page<ReleaseDetails> getReleaseDetails(final Pageable pageable) {
        final var rawReleaseDetailsPage = releaseDetailsRepository.findAll(pageable);

        final var releaseDetails = rawReleaseDetailsPage.getContent()
                .stream()
                .map(releaseDetailsTransformer::releaseDetailsOf)
                .collect(Collectors.toList());

        return new PageImpl<>(releaseDetails, pageable, rawReleaseDetailsPage.getTotalElements());
    }

    public Optional<List<ReleaseDetails>> releaseDetailsForOffenderId(final Long offenderId) {
        final var maybeReleaseDetails = offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings()
                                .stream()
                                .map(OffenderBooking::getOffenderReleaseDetails)
                        .filter(Objects::nonNull)
                                .collect(Collectors.toList()));

        return maybeReleaseDetails.map(releaseDetails -> releaseDetails
                        .stream()
                        .map(releaseDetailsTransformer::releaseDetailsOf)
                        .collect(Collectors.toList()));
    }

    public Optional<ReleaseDetails> releaseDetailsForOffenderIdAndBookingId(final Long offenderId, final Long bookingId) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .flatMap(ob -> Optional.ofNullable(ob.getOffenderReleaseDetails())
                        .map(releaseDetailsTransformer::releaseDetailsOf));
    }

}
