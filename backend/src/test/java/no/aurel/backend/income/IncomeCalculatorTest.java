package no.aurel.backend.income;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.aurel.backend.income.dto.MonthIncome;

class IncomeCalculatorTest {

    @Test
    void vatIsTakenOutOfAPriceThatAlreadyIncludesIt() {
        // 1000 kr incl. 25 % VAT = 800 kr + 200 kr VAT. VAT is 20 % of the price, not 25 %.
        MonthIncome month = IncomeCalculator.forMonth(2026, 10, 1, 1000, true, 25);

        assertThat(month.grossOre()).isEqualTo(100_000);
        assertThat(month.vatOre()).isEqualTo(20_000);
        assertThat(month.netOre()).isEqualTo(80_000);
    }

    @Test
    void netPlusVatAlwaysEqualsGross() {
        for (int gross : new int[] {1, 333, 750, 1150, 2700, 12_345}) {
            MonthIncome month = IncomeCalculator.forMonth(2026, 1, 1, gross, true, 25);

            assertThat(month.netOre() + month.vatOre()).isEqualTo(month.grossOre());
        }
    }

    @Test
    void notBeingVatRegisteredMeansNothingIsDeducted() {
        MonthIncome month = IncomeCalculator.forMonth(2026, 10, 3, 2500, false, 25);

        assertThat(month.vatOre()).isZero();
        assertThat(month.netOre()).isEqualTo(250_000);
    }

    @Test
    void vatIsRoundedToTheNearestOre() {
        // 100 kr at 12 %: 10000 x 12 / 112 = 1071.43 øre, rounded down
        assertThat(IncomeCalculator.vatIncludedIn(10_000, 12)).isEqualTo(1_071);
        // 10 kr at 12 %: 1000 x 12 / 112 = 107.14 øre
        assertThat(IncomeCalculator.vatIncludedIn(1_000, 12)).isEqualTo(107);
        // 15 % on 115 kr is exactly 15 kr
        assertThat(IncomeCalculator.vatIncludedIn(11_500, 15)).isEqualTo(1_500);
        // halves round up: 1 x 25 / 125 is exact, but 5 øre at 10 % = 0.4545 -> 0
        assertThat(IncomeCalculator.vatIncludedIn(5, 10)).isZero();
        assertThat(IncomeCalculator.vatIncludedIn(6, 10)).isEqualTo(1);
    }

    @Test
    void zeroPercentRateMeansNoVat() {
        MonthIncome month = IncomeCalculator.forMonth(2026, 10, 1, 1000, true, 0);

        assertThat(month.vatOre()).isZero();
        assertThat(month.netOre()).isEqualTo(100_000);
    }

    @Test
    void anEmptyMonthIsAllZeros() {
        MonthIncome month = IncomeCalculator.forMonth(2026, 10, 0, 0, true, 25);

        assertThat(month.grossOre()).isZero();
        assertThat(month.vatOre()).isZero();
        assertThat(month.netOre()).isZero();
    }

    @Test
    void keepsTheMonthAndCount() {
        MonthIncome month = IncomeCalculator.forMonth(2026, 10, 7, 5000, true, 25);

        assertThat(month.year()).isEqualTo(2026);
        assertThat(month.month()).isEqualTo(10);
        assertThat(month.appointments()).isEqualTo(7);
    }
}
