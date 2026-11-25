package no.aurel.backend.accounting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.aurel.backend.accounting.dto.AccountingLine;
import no.aurel.backend.accounting.dto.AccountingStatement;
import no.aurel.backend.appointment.PaymentMethod;

class AccountingStatementBuilderTest {

    private static final LocalDate FROM = LocalDate.of(2026, 10, 1);
    private static final LocalDate TO = LocalDate.of(2026, 11, 30);

    private static AccountingLine line(int month, int day, PaymentMethod method, long kroner) {
        return new AccountingLine(LocalDateTime.of(2026, month, day, 10, 0), "Behandling", method, kroner * 100);
    }

    private static AccountingStatement build(boolean vat, boolean newestFirst, AccountingLine... lines) {
        return AccountingStatementBuilder.build(FROM, TO, vat, 25, List.of(lines), newestFirst, 299, 100);
    }

    @Test
    void linesAreSortedOldestFirstByDefault() {
        AccountingStatement statement = build(false, false,
                line(11, 20, PaymentMethod.AT_SALON, 500),
                line(10, 5, PaymentMethod.AT_SALON, 300));

        assertThat(statement.lines()).extracting(l -> l.start().getDayOfMonth()).containsExactly(5, 20);
    }

    @Test
    void linesCanBeSortedNewestFirst() {
        AccountingStatement statement = build(false, true,
                line(10, 5, PaymentMethod.AT_SALON, 300),
                line(11, 20, PaymentMethod.AT_SALON, 500));

        assertThat(statement.lines()).extracting(l -> l.start().getDayOfMonth()).containsExactly(20, 5);
    }

    @Test
    void totalsAreTheSumOfTheMonths() {
        AccountingStatement statement = build(true, false,
                line(10, 5, PaymentMethod.AT_SALON, 1000),
                line(11, 1, PaymentMethod.VIPPS, 1000),
                line(11, 2, PaymentMethod.VIPPS, 250));

        assertThat(statement.months()).hasSize(2);
        assertThat(statement.appointments()).isEqualTo(3);
        assertThat(statement.grossOre()).isEqualTo(225_000);
        // 25 % VAT is included in the price: 1000 kr is 800 + 200
        assertThat(statement.vatOre()).isEqualTo(45_000);
        assertThat(statement.netOre()).isEqualTo(180_000);
        assertThat(statement.vatOre() + statement.netOre()).isEqualTo(statement.grossOre());
    }

    @Test
    void noVatWhenTheBusinessIsNotVatRegistered() {
        AccountingStatement statement = build(false, false, line(10, 5, PaymentMethod.AT_SALON, 1000));

        assertThat(statement.vatOre()).isZero();
        assertThat(statement.netOre()).isEqualTo(statement.grossOre());
    }

    @Test
    void vippsAndSalonAreSummedSeparately() {
        AccountingStatement statement = build(false, false,
                line(10, 5, PaymentMethod.AT_SALON, 300),
                line(10, 6, PaymentMethod.VIPPS, 700));

        assertThat(statement.salonGrossOre()).isEqualTo(30_000);
        assertThat(statement.vippsGrossOre()).isEqualTo(70_000);
    }

    @Test
    void vippsFeeIsPercentagePlusFixedAmountPerPayment() {
        // 1000 kr: 2,99 % = 29,90 kr, plus 1 kr = 30,90 kr
        assertThat(AccountingStatementBuilder.vippsFee(100_000, 299, 100)).isEqualTo(3_090);
        // 0,5 øre rounds up: 150 øre * 2,99 % = 4,485 -> 4 øre + 100
        assertThat(AccountingStatementBuilder.vippsFee(150, 299, 100)).isEqualTo(104);
        assertThat(AccountingStatementBuilder.vippsFee(50_100, 299, 100)).isEqualTo(1_598);
    }

    @Test
    void feeIsChargedPerVippsPaymentAndNotOnSalonPayments() {
        AccountingStatement statement = build(false, false,
                line(10, 5, PaymentMethod.VIPPS, 1000),
                line(10, 6, PaymentMethod.VIPPS, 1000),
                line(10, 7, PaymentMethod.AT_SALON, 5000));

        // Two payments of 30,90 kr: the 1 kr is charged each time
        assertThat(statement.vippsFeeOre()).isEqualTo(6_180);
        // The fee is a cost: income and VAT are untouched
        assertThat(statement.netOre()).isEqualTo(700_000);
    }

    @Test
    void noVippsPaymentsMeansNoFee() {
        assertThat(build(false, false, line(10, 5, PaymentMethod.AT_SALON, 1000)).vippsFeeOre()).isZero();
    }

    @Test
    void anEmptyPeriodGivesZeroEverywhere() {
        AccountingStatement statement = build(true, false);

        assertThat(statement.lines()).isEmpty();
        assertThat(statement.months()).isEmpty();
        assertThat(statement.grossOre()).isZero();
    }
}
