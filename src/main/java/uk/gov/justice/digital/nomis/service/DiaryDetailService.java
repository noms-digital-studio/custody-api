package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.DiaryDetail;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;
import uk.gov.justice.digital.nomis.service.transformer.DiaryDetailTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class DiaryDetailService {

    private static final String SCH = "SCH";
    private static final String ESCORT = "ESCORT";
    private  static final Comparator<DiaryDetail> BY_COURT_EVENT_DATE = Comparator.comparing(DiaryDetail::getMovementDateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    private final DiaryDetailTransformer diaryDetailTransformer;
    private final OffenderRepository offenderRepository;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public DiaryDetailService(DiaryDetailTransformer diaryDetailTransformer, OffenderRepository offenderRepository, ReferenceCodesRepository referenceCodesRepository) {
        this.diaryDetailTransformer = diaryDetailTransformer;
        this.offenderRepository = offenderRepository;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public Optional<List<DiaryDetail>> diaryDetailsForOffenderId(Long offenderId) {

        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.map(this::diaryDetailsOfOffender);
    }

    private List<DiaryDetail> diaryDetailsOfOffender(Offender offender) {
        return offender.getOffenderBookings()
                .stream()
                .flatMap(this::diaryDetailsOfBooking)
                .collect(Collectors.toList());
    }

    private Stream<DiaryDetail> diaryDetailsOfBooking(OffenderBooking offenderBooking) {

        final Stream<DiaryDetail> courtEventDiaryDetailStream = offenderBooking.getCourtEvents()
                .stream()
                .filter(courtEvent -> SCH.equals(courtEvent.getEventStatus()))
                .map(diaryDetailTransformer::diaryDetailOf);

        final Stream<DiaryDetail> offenderReleaseDetailsDiaryDetailStream =
                Optional.ofNullable(offenderBooking.getOffenderReleaseDetails())
                .filter(offenderReleaseDetails -> SCH.equals(offenderReleaseDetails.getEventStatus()))
                .map(diaryDetailTransformer::diaryDetailOf)
                .map(Stream::of).orElse(Stream.empty());

        final Stream<DiaryDetail> offenderIndSchedulesDiaryDetailStream = offenderBooking.getOffenderIndSchedules()
                .stream()
                .filter(offenderIndSchedule -> SCH.equals(offenderIndSchedule.getEventStatus()))
                .map(offenderIndSchedule ->
                        diaryDetailTransformer.diaryDetailOf(
                                offenderIndSchedule,
                                offenderIndSchedule.getEscortCode() != null ? referenceCodesRepository.findById(
                                        ReferenceCodePK.builder()
                                                .code(offenderIndSchedule.getEscortCode())
                                                .domain(ESCORT)
                                                .build()).orElse(null) : null));

        return Stream.of(
                courtEventDiaryDetailStream,
                offenderReleaseDetailsDiaryDetailStream,
                offenderIndSchedulesDiaryDetailStream
        )
                .flatMap(s -> s)
                .sorted(BY_COURT_EVENT_DATE);

    }

}
