package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.OffenderIepLevel;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.IEPTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class IEPService {

    private static final Comparator<uk.gov.justice.digital.nomis.jpa.entity.OffenderIepLevel> BY_IEP_SEQ_REVERSED = Comparator
            .comparing(uk.gov.justice.digital.nomis.jpa.entity.OffenderIepLevel::getIepDate)
            .thenComparing(uk.gov.justice.digital.nomis.jpa.entity.OffenderIepLevel::getIepTime)
            .thenComparing(uk.gov.justice.digital.nomis.jpa.entity.OffenderIepLevel::getIepLevelSeq)
            .reversed();
    private final IEPTransformer iepTransformer;
    private final OffenderRepository offenderRepository;

    @Autowired
    public IEPService(final IEPTransformer iepTransformer, final OffenderRepository offenderRepository) {
        this.iepTransformer = iepTransformer;
        this.offenderRepository = offenderRepository;
    }


    public Optional<List<OffenderIepLevel>> iepsForOffenderId(final Long offenderId) {
        return offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings()
                        .stream()
                        .flatMap(offenderBooking -> offenderBooking.getOffenderIepLevels().stream())
                        .sorted(BY_IEP_SEQ_REVERSED)
                        .map(iepTransformer::offenderIepLevelOf)
                        .collect(Collectors.toList()));

    }

}
