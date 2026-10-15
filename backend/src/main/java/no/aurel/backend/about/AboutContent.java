package no.aurel.backend.about;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "about_content")
@Getter
@Setter
@NoArgsConstructor
public class AboutContent {

    // Single-row table: the migration creates the row with this id
    public static final long ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String heading;

    // Paragraphs separated by a blank line
    @Column(nullable = false, columnDefinition = "text")
    private String body;

    // Null = default photo. Otherwise when the photo was uploaded (changes the image address, so browsers refetch).
    @Column(name = "image_version")
    private Long imageVersion;
}
