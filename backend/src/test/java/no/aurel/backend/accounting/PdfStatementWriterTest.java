package no.aurel.backend.accounting;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import no.aurel.backend.accounting.dto.AccountingLine;
import no.aurel.backend.accounting.dto.AccountingStatement;
import no.aurel.backend.appointment.PaymentMethod;

class PdfStatementWriterTest {

    private final PdfStatementWriter writer =
            new PdfStatementWriter("The Beauty Room by AG", "BYCI BEAUTYROOM", "935418658", "Kleivene 131, 3041 Drammen");

    @Test
    void producesAPdfFile() {
        AccountingStatement statement = AccountingStatementBuilder.build(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 30), true, 25,
                List.of(
                        new AccountingLine(LocalDateTime.of(2026, 10, 5, 10, 0), "Vippeløft + Farge på bryn", PaymentMethod.VIPPS, 95_000),
                        new AccountingLine(LocalDateTime.of(2026, 11, 7, 12, 30), "Pedikyr", PaymentMethod.AT_SALON, 60_000)),
                false, 299, 100);

        byte[] pdf = writer.write(statement);

        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(1000);
    }

    @Test
    void anEmptyPeriodStillGivesAPdf() {
        AccountingStatement statement = AccountingStatementBuilder.build(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), false, 25, List.of(), false, 299, 100);

        assertThat(writer.write(statement)).isNotEmpty();
    }

    @Test
    void moneyIsFormattedWithSpacesAndComma() {
        assertThat(PdfStatementWriter.kr(123_456)).isEqualTo("1 234,56 kr");
        assertThat(PdfStatementWriter.kr(0)).isEqualTo("0,00 kr");
        assertThat(PdfStatementWriter.kr(100_000_000)).isEqualTo("1 000 000,00 kr");
    }
}
