package no.aurel.backend.schedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleExceptionRepository extends JpaRepository<ScheduleException, Long> {

    List<ScheduleException> findByEmployeeIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(
            Long employeeId, LocalDate from);

    List<ScheduleException> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);
}
