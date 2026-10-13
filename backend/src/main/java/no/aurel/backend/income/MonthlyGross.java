package no.aurel.backend.income;

// One row of the income query: how many appointments took place in a month and what they cost in total (whole kroner).
// Wrapper types because the database query builds these objects directly.
public record MonthlyGross(Integer year, Integer month, Long grossNok, Long appointments) {
}
