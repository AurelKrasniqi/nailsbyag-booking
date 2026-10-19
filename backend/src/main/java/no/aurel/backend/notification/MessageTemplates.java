package no.aurel.backend.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Single-row table: there is only one salon, so the row always has id 1
@Entity
@Table(name = "message_templates")
@Getter
@Setter
@NoArgsConstructor
public class MessageTemplates {

    public static final long ID = 1L;

    @Id
    private Long id;

    @Column(name = "confirmation_text", nullable = false)
    private String confirmationText;

    @Column(name = "reminder_text", nullable = false)
    private String reminderText;
}
