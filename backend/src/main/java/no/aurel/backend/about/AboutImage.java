package no.aurel.backend.about;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "about_image")
@Getter
@Setter
@NoArgsConstructor
public class AboutImage {

    @Id
    private Long id = AboutContent.ID;

    @Column(name = "content_type", nullable = false, length = 30)
    private String contentType;

    @Column(nullable = false)
    private byte[] data;
}
