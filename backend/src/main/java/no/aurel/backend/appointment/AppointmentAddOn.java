package no.aurel.backend.appointment;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One line of extras on an appointment. Name, price and duration are copied from the add-on at booking time,
// and are totals for the line (unit x quantity).
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentAddOn {

    @Column(name = "add_on_id", nullable = false)
    private Long addOnId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "price_nok", nullable = false)
    private int priceNok;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
}
