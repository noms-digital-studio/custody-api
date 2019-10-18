package uk.gov.justice.digital.nomis.service.transformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.nomis.api.ImprisonmentStatus;
import uk.gov.justice.digital.nomis.api.OffenderImprisonmentStatus;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderImprisonStatus;
import uk.gov.justice.digital.nomis.jpa.repository.ImprisonmentStatusesRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ImprisonStatusTransformer {

    private final TypesTransformer typesTransformer;
    private final ImprisonmentStatusesRepository imprisonmentStatusesRepository;

    @Autowired
    public ImprisonStatusTransformer(final TypesTransformer typesTransformer, final ImprisonmentStatusesRepository imprisonmentStatusesRepository) {
        this.typesTransformer = typesTransformer;
        this.imprisonmentStatusesRepository = imprisonmentStatusesRepository;
    }

    public List<OffenderImprisonmentStatus> offenderImprisonStatusesOf(final List<uk.gov.justice.digital.nomis.jpa.entity.OffenderImprisonStatus> offenderImprisonStatuses) {
        return Optional.ofNullable(offenderImprisonStatuses)
                .map(oises -> oises
                        .stream()
                        .filter(ois -> !"N".equalsIgnoreCase(ois.getLatestStatus()))
                        .map(this::offenderImprisonStatusOf)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    public OffenderImprisonmentStatus offenderImprisonStatusOf(final uk.gov.justice.digital.nomis.jpa.entity.OffenderImprisonStatus ois) {
        return OffenderImprisonmentStatus.builder()
                .agencyLocationId(ois.getAgyLocId())
                .commentText(ois.getCommentText())
                .effectiveDateTime(typesTransformer.localDateTimeOf(ois.getEffectiveDate(), ois.getEffectiveTime()))
                .expiryDate(typesTransformer.localDateOf(ois.getExpiryDate()))
                .imprisonmentStatus(imprisonmentStatusOf(ois))
                .imprisonStatusSeq(ois.getImprisonStatusSeq())
                .latestStatus(typesTransformer.ynToBoolean(ois.getLatestStatus()))
                .bookingId(ois.getOffenderBookId())
                .build();
    }

    private ImprisonmentStatus imprisonmentStatusOf(final OffenderImprisonStatus ois) {
        return Optional.ofNullable(ois.getImprisonmentStatus() != null ?
                imprisonmentStatusesRepository.findByImprisonmentStatus(ois.getImprisonmentStatus())
                        .stream().findFirst().orElse(null) : null)
                .map(is -> ImprisonmentStatus.builder()
                        .bandCode(is.getBandCode())
                    .description(is.getDescription())
                    .imprisonmentStatus(is.getImprisonmentStatus())
                    .imprisonmentStatusId(is.getImprisonmentStatusId())
                    .imprisonmentStatusSeq(is.getImprisonmentStatusSeq())
                    .rankValue(is.getRankValue())
                .build())
                .orElse(null);
    }
}
