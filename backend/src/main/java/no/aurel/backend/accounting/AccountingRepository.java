package no.aurel.backend.accounting;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import no.aurel.backend.appointment.Appointment;

// Read-only. Uses the same rule for "counts as income" as the income page: not cancelled, not waiting for payment,
// and already over. "to" is exclusive (the day after the last day of the period).
public interface AccountingRepository extends Repository<Appointment, Long> {

    @Query("""
            select distinct a from Appointment a
            left join fetch a.services
            where a.status in (no.aurel.backend.appointment.AppointmentStatus.BOOKED,
                               no.aurel.backend.appointment.AppointmentStatus.COMPLETED)
              and a.endTime <= :now
              and a.startTime >= :from
              and a.startTime < :to
            """)
    List<Appointment> findCountedBetween(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("now") LocalDateTime now);
}
