package no.aurel.backend.income.dto;

// One calendar month of income. All money is in øre (1/100 kr) so no decimals can creep in.
//   grossOre: what the customers paid (prices include VAT)
//   vatOre:   the VAT part of that, owed to the state (0 if the business is not VAT registered)
//   netOre:   what is actually earned: gross minus VAT
public record MonthIncome(int year, int month, long appointments, long grossOre, long vatOre, long netOre) {
}
