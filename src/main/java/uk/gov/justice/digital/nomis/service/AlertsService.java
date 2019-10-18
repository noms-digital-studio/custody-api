package uk.gov.justice.digital.nomis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.digital.nomis.api.Alert;
import uk.gov.justice.digital.nomis.jpa.entity.Offender;
import uk.gov.justice.digital.nomis.jpa.entity.OffenderAlert;
import uk.gov.justice.digital.nomis.jpa.filters.AlertsFilter;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderAlertsRepository;
import uk.gov.justice.digital.nomis.jpa.repository.OffenderRepository;
import uk.gov.justice.digital.nomis.service.transformer.AlertsTransformer;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AlertsService {

    private static final Comparator<OffenderAlert> BY_ALERT_DATE = Comparator
            .comparing(OffenderAlert::getOffenderBookId)
            .thenComparing(OffenderAlert::getAlertDate, Comparator.reverseOrder()) // Earliest first
            .thenComparing(OffenderAlert::getAlertSeq, Comparator.reverseOrder()) // ASC
            .reversed();

    private final OffenderRepository offenderRepository;
    private final AlertsTransformer alertsTransformer;
    private final OffenderAlertsRepository offenderAlertsRepository;

    @Autowired
    public AlertsService(final OffenderRepository offenderRepository, final AlertsTransformer alertsTransformer, final OffenderAlertsRepository offenderAlertsRepository) {
        this.offenderRepository = offenderRepository;
        this.offenderAlertsRepository = offenderAlertsRepository;
        this.alertsTransformer = alertsTransformer;
    }

    public Page<Alert> getAlerts(final Pageable pageable, final Optional<LocalDateTime> from, final Optional<LocalDateTime> to) {
        final var alertsFilter = AlertsFilter.builder()
                .from(from)
                .to(to)
                .build();

        final var rawOffenderAlertsPage = offenderAlertsRepository.findAll(alertsFilter, pageable);

        final var alerts = rawOffenderAlertsPage.getContent()
                .stream()
                .sorted(BY_ALERT_DATE)
                .map(alertsTransformer::alertOf)
                .collect(Collectors.toList());

        return new PageImpl<>(alerts, pageable, rawOffenderAlertsPage.getTotalElements());
    }

    public Optional<List<Alert>> getOffenderAlerts(final Long offenderId, final Optional<String> maybeAlertCode, final Optional<String> maybeAlertStatus, final Optional<String> maybeAlertType) {

        final var maybeOffender = offenderRepository.findById(offenderId);

        final var maybeOffenderBookings = maybeOffender.map(Offender::getOffenderBookings);

        return maybeOffenderBookings.map(bookings -> bookings
                .stream()
                .flatMap(booking -> booking.getOffenderAlerts().stream())
                .sorted(BY_ALERT_DATE)
                .filter(alert -> shouldInclude(alert, maybeAlertCode, maybeAlertStatus, maybeAlertType))
                .map(alertsTransformer::alertOf)
                .collect(Collectors.toList()));
    }

    private boolean shouldInclude(final OffenderAlert offenderAlert, final Optional<String> maybeAlertCode, final Optional<String> maybeAlertStatus, final Optional<String> maybeAlertType) {
        return maybeAlertCode.map(
                alertCode -> alertCode.equals(offenderAlert.getAlertCode())
        ).orElse(true)

                && maybeAlertStatus.map(
                alertStatus -> alertStatus.equals(offenderAlert.getAlertStatus())
        ).orElse(true)

                && maybeAlertType.map(
                alertType -> alertType.equals(offenderAlert.getAlertType())

        ).orElse(true);
    }

    public Optional<List<Alert>> offenderAlertsForOffenderIdAndBookingId(final Long offenderId, final Long bookingId, final Optional<String> maybeAlertCode, final Optional<String> maybeAlertStatus, final Optional<String> maybeAlertType) {
        final var maybeOffender = offenderRepository.findById(offenderId);

        return maybeOffender.flatMap(
                offender -> offender.getOffenderBookings()
                        .stream()
                        .filter(ob -> ob.getOffenderBookId().equals(bookingId))
                        .findFirst())
                .map(ob -> ob.getOffenderAlerts()
                        .stream()
                        .sorted(BY_ALERT_DATE)
                        .filter(alert -> shouldInclude(alert, maybeAlertCode, maybeAlertStatus, maybeAlertType))
                        .map(alertsTransformer::alertOf)
                        .collect(Collectors.toList()));
    }
}
