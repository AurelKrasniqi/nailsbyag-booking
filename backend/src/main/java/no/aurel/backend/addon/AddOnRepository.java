package no.aurel.backend.addon;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AddOnRepository extends JpaRepository<AddOn, Long> {

    List<AddOn> findByActiveTrueOrderBySortOrder();
}
