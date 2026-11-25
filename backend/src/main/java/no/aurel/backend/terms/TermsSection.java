package no.aurel.backend.terms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One block of the "Salgsvilkår" page: an optional heading and some text. The page is the sections in sort order.
@Entity
@Table(name = "terms_section")
@Getter
@Setter
@NoArgsConstructor
public class TermsSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Null = no heading (used for the introduction at the top)
    @Column(length = 100)
    private String heading;

    // Paragraphs separated by a blank line
    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
