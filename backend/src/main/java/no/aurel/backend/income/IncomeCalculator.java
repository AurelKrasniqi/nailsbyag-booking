package no.aurel.backend.income;

import no.aurel.backend.income.dto.MonthIncome;

// Pure money logic: no Spring, no database. Everything is whole øre (long), never float/double.
public final class IncomeCalculator {

    private IncomeCalculator() {
    }

    /**
     * Splits one month's takings into VAT and actual earnings.
     *
     * @param grossNok what customers paid in the month, in whole kroner (prices include VAT)
     */
    public static MonthIncome forMonth(
            int year, int month, long appointments, long grossNok, boolean vatRegistered, int vatRatePercent) {
        long grossOre = grossNok * 100;
        long vatOre = vatRegistered ? vatIncludedIn(grossOre, vatRatePercent) : 0;
        return new MonthIncome(year, month, appointments, grossOre, vatOre, grossOre - vatOre);
    }

    /**
     * The VAT contained in a price that already includes VAT.
     * Price 1000 with 25 % VAT is 800 + 200 VAT, so the VAT is price x 25 / 125, not price x 25 / 100.
     * Rounded to the nearest øre (half up).
     */
    static long vatIncludedIn(long grossOre, int vatRatePercent) {
        long divisor = 100L + vatRatePercent;
        return (grossOre * vatRatePercent + divisor / 2) / divisor;
    }
}
