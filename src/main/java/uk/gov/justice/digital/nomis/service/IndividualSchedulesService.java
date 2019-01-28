package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.IndividualSchedule;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderBooking;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderIndSchedule;
import uk.gov.justice.digital.nomis.jpa.entity.ReferenceCodePK;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.jpa.repository.ReferenceCodesRepository;
import uk.gov.justice.digital.nomis.service.transformer.IndividualScheduleTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class IndividualSchedulesService {

    private static final String SCH = "SCH";
    private static final String ESCORT = "ESCORT";
    private  static final Comparator<OffenderIndSchedule> BY_RETURN_DATE = Comparator
            .comparing(OffenderIndSchedule::getReturnDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
    private final IndividualScheduleTransformer individualScheduleTransformer;
    private final OffenderRepository offenderRepository;
    private final ReferenceCodesRepository referenceCodesRepository;

    @Autowired
    public IndividualSchedulesService(IndividualScheduleTransformer individualScheduleTransformer, OffenderRepository offenderRepository, ReferenceCodesRepository referenceCodesRepository) {
        this.individualScheduleTransformer = individualScheduleTransformer;
        this.offenderRepository = offenderRepository;
        this.referenceCodesRepository = referenceCodesRepository;
    }

    public Optional<List<IndividualSchedule>> individualSchedulesForOffenderId(Long offenderId) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.map(this::individualSchedulesOfOffender);
    }

    public Optional<List<IndividualSchedule>> individualSchedulesForOffenderIdAndBookingId(Long offenderId, Long bookingId) {
        Optional<Offender> maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.map(o -> this.individualScheduleOfOffender(o, bookingId));
    }

    private List<IndividualSchedule> individualSchedulesOfOffender(Offender offender) {
        return offender.getOffenderBookings()
                .stream()
                .flatMap(this::individualSchedulesOfBooking)
                .collect(Collectors.toList());
    }

    private List<IndividualSchedule> individualScheduleOfOffender(Offender offender, Long bookingId) {
        return offender.getOffenderBookings()
                .stream()
                .filter(ob -> ob.getOffenderBookId() == bookingId)
                .flatMap(this::individualSchedulesOfBooking)
                .collect(Collectors.toList());
    }

    private Stream<IndividualSchedule> individualSchedulesOfBooking(OffenderBooking offenderBooking) {
        return offenderBooking.getOffenderIndSchedules()
                .stream()
                .filter(offenderIndSchedule -> SCH.equals(offenderIndSchedule.getEventStatus()))
                .sorted(BY_RETURN_DATE)
                .map(ois -> individualScheduleTransformer.individualScheduleOf(
                        ois,
                        ois.getEscortCode() != null ? referenceCodesRepository.findById(ReferenceCodePK.builder()
                                .code(ois.getEscortCode())
                                .domain(ESCORT)
                                .build()).orElse(null) : null));
    }
}
