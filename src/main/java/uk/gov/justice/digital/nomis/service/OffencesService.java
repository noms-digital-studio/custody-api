package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Offence;
import uk.gov.justice.digital.nomis.jpa.repository.OffencesRepository;
import uk.gov.justice.digital.nomis.service.transformer.OffenceTransformer;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OffencesService {

    private final OffencesRepository offencesRepository;
    private final OffenceTransformer offenceTransformer;

    @Autowired
    public OffencesService(final OffencesRepository offencesRepository, final OffenceTransformer offenceTransformer) {
        this.offencesRepository = offencesRepository;
        this.offenceTransformer = offenceTransformer;
    }

    public Page<Offence> getOffences(final Pageable pageable) {
        final var rawOffencesPage = offencesRepository.findAll(pageable);

        final var offences = rawOffencesPage.getContent().stream()
                .map(offenceTransformer::offenceOf)
                .collect(Collectors.toList());

        return new PageImpl<>(offences, pageable, rawOffencesPage.getTotalElements());
    }
}
