package no.aurel.backend.appointment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Appointments that occupy the employee's time somewhere in [from, to): everything except cancelled ones
    @Query("""
            select a from Appointment a
            where a.employee.id = :employeeId
              and a.status <> no.aurel.backend.appointment.AppointmentStatus.CANCELLED
              and a.startTime < :to
              and a.endTime > :from
            """)
    List<Appointment> findActiveOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
