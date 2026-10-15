package no.aurel.backend.addon;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddOnRepository extends JpaRepository<AddOn, Long> {

    List<AddOn> findByActiveTrueOrderBySortOrder();

    // Appointments keep a line for every extra they used, so such an extra can only be hidden, not deleted
    @Query(value = "SELECT COUNT(*) > 0 FROM appointment_add_on WHERE add_on_id = :id", nativeQuery = true)
    boolean usedInAppointment(@Param("id") Long id);

    @Query("SELECT COALESCE(MAX(a.sortOrder), 0) FROM AddOn a")
    int maxSortOrder();
}
