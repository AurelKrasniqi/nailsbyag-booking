package no.aurel.backend.addon;

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
@Table(name = "add_on")
@Getter
@Setter
@NoArgsConstructor
public class AddOn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // What the price is per, e.g. "pr. negl". Null for a one-off extra.
    @Column(name = "unit_label", length = 30)
    private String unitLabel;

    // Which treatments this extra can be added to. A null subcategory means the whole category.
    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 100)
    private String subcategory;

    // Price and duration for ONE unit (one nail, or the extra as a whole)
    @Column(name = "price_nok", nullable = false)
    private int priceNok;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    // 10 = chosen per nail, 1 = yes or no
    @Column(name = "max_quantity", nullable = false)
    private int maxQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
