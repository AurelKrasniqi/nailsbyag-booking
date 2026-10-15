package no.aurel.backend.servicegroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A box on the website: a title, an optional photo, and the treatments that belong to it
@Entity
@Table(name = "service_group")
@Getter
@Setter
@NoArgsConstructor
public class ServiceGroup {

    public static final int DEFAULT_IMAGE_FOCUS = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    // Small text above the title, e.g. "Manikyr" above "Nytt sett"
    @Column(length = 100)
    private String eyebrow;

    // Position on the website: lower comes first
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    // A photo that ships with the website, used until the admin uploads her own. Null = none.
    @Column(name = "default_image", length = 200)
    private String defaultImage;

    // Which part of a tall photo stays visible when it is cropped to the box: 0 = top, 100 = bottom
    @Column(name = "image_focus", nullable = false)
    private int imageFocus = DEFAULT_IMAGE_FOCUS;

    // Null = no uploaded photo. Otherwise when it was uploaded (changes the image address, so browsers refetch).
    @Column(name = "image_version")
    private Long imageVersion;
}
