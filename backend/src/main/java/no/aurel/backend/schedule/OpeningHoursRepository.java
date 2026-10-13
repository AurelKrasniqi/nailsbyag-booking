package no.aurel.backend.schedule;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OpeningHoursRepository extends JpaRepository<OpeningHours, Long> {

    List<OpeningHours> findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
