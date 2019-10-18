package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.KeyValue;
import uk.gov.justice.digital.nomis.api.Offence;
import uk.gov.justice.digital.nomis.jpa.entity.HoCode;
import uk.gov.justice.digital.nomis.jpa.entity.OffenceIndicator;
import uk.gov.justice.digital.nomis.jpa.entity.Statute;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OffenceTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public OffenceTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public Offence offenceOf(final uk.gov.justice.digital.nomis.jpa.entity.Offence offence) {
        return Offence.builder()
                .active(typesTransformer.ynToBoolean(offence.getActiveFlag()))
                .defaultOffenceType(offence.getDefaultOffenceType())
                .description(offence.getDescription())
                .expiryDate(typesTransformer.localDateOf(offence.getExpiryDate()))
                .hoCode(hoCodeOf(offence.getHoCode()))
                .listSeq(offence.getListSeq())
                .maxGoodTimePerc(offence.getMaxGoodTimePerc())
                .maxSentenceLength(offence.getMaxSentenceLength())
                .offenceCode(offence.getOffenceCode())
                .offenceGroup(offence.getOffenceGroup())
                .offenseDegree(offence.getOffenseDegree())
                .oldStatute(statuteOf(offence.getOldStatute()))
                .repealedDate(typesTransformer.localDateOf(offence.getRepealedDate()))
                .sentenceUnitCode(offence.getSentenceUnitCode())
                .statute(statuteOf(offence.getStatute()))
                .offenceIndicators(offenceIndicatorsOf(offence.getOffenceIndicators()))
                .build();
    }

    private List<String> offenceIndicatorsOf(final List<OffenceIndicator> offenceIndicators) {
        return Optional.ofNullable(offenceIndicators)
                .map(ois -> ois.stream()
                        .map(OffenceIndicator::getIndicatorCode)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    private KeyValue statuteOf(final Statute statute) {
        return Optional.ofNullable(statute)
                .map(s -> KeyValue.builder()
                        .code(s.getStatuteCode())
                        .description(s.getDescription())
                        .build())
                .orElse(null);
    }

    private KeyValue hoCodeOf(final HoCode hoCode) {
        return Optional.ofNullable(hoCode)
                .map(hoc -> KeyValue.builder()
                        .code(hoc.getHoCode())
                        .description(hoc.getDescription())
                        .build())
                .orElse(null);
    }
}
