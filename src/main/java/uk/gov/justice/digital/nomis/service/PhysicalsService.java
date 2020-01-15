package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Physicals;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.PhysicalsTransformer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PhysicalsService {

    private final PhysicalsTransformer physicalsTransformer;
    private final OffenderRepository offenderRepository;

    @Autowired
    public PhysicalsService(final PhysicalsTransformer physicalsTransformer, final OffenderRepository offenderRepository) {
        this.physicalsTransformer = physicalsTransformer;
        this.offenderRepository = offenderRepository;
    }


    public Optional<List<Physicals>> physicalsForOffenderId(final Long offenderId) {

        return offenderRepository.findById(offenderId)
                .map(offender ->
                        offender.getOffenderBookings().stream()
                                .map(physicalsTransformer::physicalsOf)
                                .collect(Collectors.toList()));

    }
}
