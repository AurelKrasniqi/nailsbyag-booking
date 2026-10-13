package no.aurel.backend.income.dto;

import java.util.List;

// months has one entry per month that has income, oldest first. The screen picks a month, a year or everything
// and adds the entries up, so a year is always exactly the sum of its months.
public record IncomeReport(
        boolean vatRegistered,
        int vatRatePercent,
        List<MonthIncome> months,
        UpcomingIncome upcoming
) {
}
