package no.aurel.backend.accounting;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import no.aurel.backend.accounting.dto.AccountingLine;
import no.aurel.backend.accounting.dto.AccountingStatement;
import no.aurel.backend.appointment.PaymentMethod;
import no.aurel.backend.income.IncomeCalculator;
import no.aurel.backend.income.dto.MonthIncome;

// Pure logic: no Spring, no database. Sorts the lines and adds everything up.
public final class AccountingStatementBuilder {

    private AccountingStatementBuilder() {
    }

    public static AccountingStatement build(
            LocalDate from, LocalDate to, boolean vatRegistered, int vatRatePercent,
            List<AccountingLine> lines, boolean newestFirst, int feeBasisPoints, long feeFixedOre) {

        Comparator<AccountingLine> byDate = Comparator.comparing(AccountingLine::start);
        List<AccountingLine> sorted = lines.stream()
                .sorted(newestFirst ? byDate.reversed() : byDate)
                .toList();

        // Per calendar month: how many appointments and how much they brought in (always oldest month first)
        Map<YearMonth, long[]> perMonth = new TreeMap<>();
        for (AccountingLine line : sorted) {
            long[] sums = perMonth.computeIfAbsent(YearMonth.from(line.start()), k -> new long[2]);
            sums[0]++;
            sums[1] += line.grossOre();
        }

        List<MonthIncome> months = perMonth.entrySet().stream()
                .map(e -> monthIncome(e.getKey(), e.getValue()[0], e.getValue()[1], vatRegistered, vatRatePercent))
                .toList();

        long gross = months.stream().mapToLong(MonthIncome::grossOre).sum();
        long vat = months.stream().mapToLong(MonthIncome::vatOre).sum();
        long net = months.stream().mapToLong(MonthIncome::netOre).sum();
        long vipps = grossFor(sorted, PaymentMethod.VIPPS);
        long salon = grossFor(sorted, PaymentMethod.AT_SALON);
        long vippsFee = sorted.stream()
                .filter(l -> l.paymentMethod() == PaymentMethod.VIPPS)
                .mapToLong(l -> vippsFee(l.grossOre(), feeBasisPoints, feeFixedOre))
                .sum();

        return new AccountingStatement(from, to, vatRegistered, vatRatePercent, sorted, months,
                sorted.size(), gross, vat, net, vipps, salon, vippsFee, feeBasisPoints, feeFixedOre);
    }

    /**
     * The fee Vipps takes on ONE payment: a percentage of the amount plus a fixed amount per payment.
     * 1000 kr at 2,99 % + 1 kr = 29,90 + 1,00 = 30,90 kr. Per payment (not on the sum), because the fixed part is
     * charged every time. The percentage part is rounded to the nearest øre (half up).
     *
     * @param feeBasisPoints hundredths of a percent: 299 = 2,99 %
     */
    public static long vippsFee(long grossOre, int feeBasisPoints, long feeFixedOre) {
        return (grossOre * feeBasisPoints + 5_000) / 10_000 + feeFixedOre;
    }

    // Same VAT rule as the income page (IncomeCalculator), applied to one month's total
    private static MonthIncome monthIncome(
            YearMonth month, long appointments, long grossOre, boolean vatRegistered, int vatRatePercent) {
        long vatOre = vatRegistered ? IncomeCalculator.vatIncludedIn(grossOre, vatRatePercent) : 0;
        return new MonthIncome(month.getYear(), month.getMonthValue(), appointments, grossOre, vatOre, grossOre - vatOre);
    }

    private static long grossFor(List<AccountingLine> lines, PaymentMethod method) {
        return lines.stream().filter(l -> l.paymentMethod() == method).mapToLong(AccountingLine::grossOre).sum();
    }
}
