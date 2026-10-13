package no.aurel.backend.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Single-row table: there is only one business, so the row always has id 1
@Entity
@Table(name = "business_settings")
@Getter
@Setter
@NoArgsConstructor
public class BusinessSettings {

    public static final long ID = 1L;

    @Id
    private Long id;

    @Column(name = "vat_registered", nullable = false)
    private boolean vatRegistered;

    @Column(name = "vat_rate_percent", nullable = false)
    private int vatRatePercent;
}
