package no.aurel.backend.accounting;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import no.aurel.backend.accounting.dto.AccountingLine;
import no.aurel.backend.accounting.dto.AccountingStatement;
import no.aurel.backend.appointment.Appointment;
import no.aurel.backend.appointment.AppointmentAddOn;
import no.aurel.backend.salonservice.SalonService;
import no.aurel.backend.settings.SettingsService;
import no.aurel.backend.settings.dto.SettingsResponse;

@Service
@Transactional(readOnly = true)
public class AccountingService {

    // A full year plus a leap day. Keeps one request from asking for the whole history by accident.
    private static final int MAX_DAYS = 366;

    private final AccountingRepository repository;
    private final SettingsService settingsService;
    private final Clock clock;
    // What Vipps charges per payment. Set in application.properties because it comes from the agreement with Vipps.
    private final int vippsFeeBasisPoints;
    private final long vippsFeeFixedOre;

    public AccountingService(
            AccountingRepository repository,
            SettingsService settingsService,
            Clock clock,
            @Value("${app.accounting.vipps-fee-basis-points}") int vippsFeeBasisPoints,
            @Value("${app.accounting.vipps-fee-fixed-ore}") long vippsFeeFixedOre) {
        this.repository = repository;
        this.settingsService = settingsService;
        this.clock = clock;
        this.vippsFeeBasisPoints = vippsFeeBasisPoints;
        this.vippsFeeFixedOre = vippsFeeFixedOre;
    }

    // from and to are both included
    public AccountingStatement statement(LocalDate from, LocalDate to, boolean newestFirst) {
        if (to.isBefore(from)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sluttdato kan ikke være før startdato");
        }
        if (from.plusDays(MAX_DAYS).isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Velg en periode på høyst ett år");
        }

        SettingsResponse settings = settingsService.get();
        List<AccountingLine> lines = repository
                .findCountedBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), LocalDateTime.now(clock))
                .stream()
                .map(AccountingService::toLine)
                .toList();

        return AccountingStatementBuilder.build(
                from, to, settings.vatRegistered(), settings.vatRatePercent(), lines, newestFirst,
                vippsFeeBasisPoints, vippsFeeFixedOre);
    }

    // "Vippeløft, Brynslaminering + Farge på vipper ×1"
    private static AccountingLine toLine(Appointment a) {
        String treatments = a.getServices().stream()
                .sorted(Comparator.comparing(SalonService::getName))
                .map(SalonService::getName)
                .reduce((x, y) -> x + ", " + y)
                .orElse("Behandling");
        String extras = a.getAddOns().stream()
                .map(AccountingService::describeExtra)
                .reduce((x, y) -> x + ", " + y)
                .map(text -> " + " + text)
                .orElse("");
        return new AccountingLine(a.getStartTime(), treatments + extras, a.getPaymentMethod(), a.getTotalPriceNok() * 100L);
    }

    private static String describeExtra(AppointmentAddOn extra) {
        return extra.getQuantity() > 1 ? extra.getName() + " ×" + extra.getQuantity() : extra.getName();
    }
}
