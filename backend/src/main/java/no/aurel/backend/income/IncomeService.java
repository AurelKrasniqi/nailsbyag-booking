package no.aurel.backend.income;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.income.dto.IncomeReport;
import no.aurel.backend.income.dto.MonthIncome;
import no.aurel.backend.settings.SettingsService;
import no.aurel.backend.settings.dto.SettingsResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncomeService {

    private final IncomeRepository repository;
    private final SettingsService settingsService;
    private final Clock clock;

    public IncomeReport report() {
        SettingsResponse settings = settingsService.get();
        LocalDateTime now = LocalDateTime.now(clock);

        // VAT is worked out per month and then added up, so a year is always exactly the sum of its months
        List<MonthIncome> months = repository.findMonthlyGross(now).stream()
                .map(row -> IncomeCalculator.forMonth(
                        row.year(), row.month(), row.appointments(), row.grossNok(),
                        settings.vatRegistered(), settings.vatRatePercent()))
                .toList();

        return new IncomeReport(settings.vatRegistered(), settings.vatRatePercent(), months, repository.findUpcoming(now));
    }
}
