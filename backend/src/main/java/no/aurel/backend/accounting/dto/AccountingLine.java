package no.aurel.backend.accounting.dto;

import java.time.LocalDateTime;

import no.aurel.backend.appointment.PaymentMethod;

// One appointment on the statement. grossOre is what the customer paid, including VAT, in øre.
public record AccountingLine(LocalDateTime start, String description, PaymentMethod paymentMethod, long grossOre) {
}
