package no.aurel.backend.salonservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.aurel.backend.servicegroup.ServiceGroup;

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

    // The box on the website this treatment is listed in
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private ServiceGroup group;

    // Groups options under one heading (e.g. "Nytt sett manikyr"); null means a standalone service
    @Column(length = 100)
    private String category;

    // Optional second level inside a category (e.g. "Nytt sett" under "Manikyr")
    @Column(length = 100)
    private String subcategory;

    @Column(nullable = false)
    private boolean active = true;

    // Position among the treatments of the same box: lower comes first
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    // Small print under the treatment, e.g. "Tillegg kan forekomme"
    @Column(length = 200)
    private String note;

    // No fixed price (priceNok is then 0 and meaningless): shown as "contact us", cannot be booked online
    @Column(name = "price_on_request", nullable = false)
    private boolean priceOnRequest;
}
