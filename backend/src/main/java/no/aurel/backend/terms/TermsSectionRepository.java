package no.aurel.backend.terms;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsSectionRepository extends JpaRepository<TermsSection, Long> {

    List<TermsSection> findAllByOrderBySortOrder();
}
