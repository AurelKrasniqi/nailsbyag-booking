package no.aurel.backend.income.dto;

// Booked appointments that have not taken place yet. Shown separately and NOT counted as income.
public record UpcomingIncome(long appointments, long grossOre) {
}
