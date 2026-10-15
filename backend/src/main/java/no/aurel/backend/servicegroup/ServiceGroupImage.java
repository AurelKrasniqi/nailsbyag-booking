package no.aurel.backend.servicegroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// The uploaded photo of a box. A separate table so listing the boxes never drags the image bytes along.
@Entity
@Table(name = "service_group_image")
@Getter
@Setter
@NoArgsConstructor
public class ServiceGroupImage {

    @Id
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "content_type", nullable = false, length = 30)
    private String contentType;

    @Column(nullable = false)
    private byte[] data;
}
