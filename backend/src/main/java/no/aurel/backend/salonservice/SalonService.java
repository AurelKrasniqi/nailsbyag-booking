package no.aurel.backend.salonservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "salon_service")
@Getter
@Setter
@NoArgsConstructor
public class SalonService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "price_nok", nullable = false)
    private int priceNok;

    // Groups options under one heading (e.g. "Nytt sett manikyr"); null means a standalone service
    @Column(length = 100)
    private String category;

    // Optional second level inside a category (e.g. "Nytt sett" under "Manikyr")
    @Column(length = 100)
    private String subcategory;

    @Column(nullable = false)
    private boolean active = true;
}
