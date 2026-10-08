package no.aurel.backend.salonservice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {

    List<SalonService> findByActiveTrue();
}
