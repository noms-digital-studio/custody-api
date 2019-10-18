package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Sentence;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderSentence;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderSentencesRepository;
import uk.gov.justice.digital.nomis.service.transformer.SentenceTransformer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SentencesService {

    private static final Comparator<? super OffenderSentence> BY_SENTENCE_PRIORITY = Comparator
            .comparing(OffenderSentence::getSentenceStatus)
            .thenComparing(OffenderSentence::getSentenceSeq);
    private final OffenderSentencesRepository offenderSentencesRepository;
    private final OffenderRepository offenderRepository;
    private final SentenceTransformer sentenceTransformer;

    @Autowired
    public SentencesService(final OffenderSentencesRepository offenderSentencesRepository, final OffenderRepository offenderRepository, final SentenceTransformer sentenceTransformer) {
        this.offenderSentencesRepository = offenderSentencesRepository;
        this.offenderRepository = offenderRepository;
        this.sentenceTransformer = sentenceTransformer;
    }

    @Transactional
    public Page<Sentence> getSentences(final Pageable pageable) {

        final var offenderSentences = offenderSentencesRepository.findAll(pageable);

        final var sentencesList = offenderSentences.getContent()
                .stream()
                .sorted(BY_SENTENCE_PRIORITY)
                .map(sentenceTransformer::sentenceOf)
                .collect(Collectors.toList());

        return new PageImpl<>(sentencesList, pageable, offenderSentences.getTotalElements());
    }

    @Transactional
    public Optional<List<Sentence>> sentencesForOffenderId(final Long offenderId) {
        final var maybeOffenderSentences = offenderRepository.findById(offenderId)
                .map(offender -> offender.getOffenderBookings().stream()
                        .map(OffenderBooking::getOffenderSentences).
                                flatMap(Collection::stream).
                                collect(Collectors.toList()));

        return maybeOffenderSentences.map(offenderSentences -> offenderSentences
                .stream()
                .sorted(BY_SENTENCE_PRIORITY)
                .map(sentenceTransformer::sentenceOf)
                .collect(Collectors.toList()));
    }

    @Transactional
    public Optional<List<Sentence>> sentencesForOffenderIdAndBookingId(final Long offenderId, final Long bookingId) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderSentences()
                        .stream()
                        .sorted(BY_SENTENCE_PRIORITY)
                        .map(sentenceTransformer::sentenceOf)
                        .collect(Collectors.toList()));
    }
}
