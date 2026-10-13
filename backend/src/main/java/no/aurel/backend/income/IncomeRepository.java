package no.aurel.backend.income;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import no.aurel.backend.appointment.Appointment;
import no.aurel.backend.income.dto.UpcomingIncome;

// Read-only questions about money. It reads appointments, but is kept apart from AppointmentRepository
// so the income feature owns its own queries.
public interface IncomeRepository extends Repository<Appointment, Long> {

    // Income = appointments that have taken place (they ended before "now") and were not cancelled.
    // Pending online payments are excluded because they are not confirmed.
    @Query("""
            select new no.aurel.backend.income.MonthlyGross(
                cast(extract(year from a.startTime) as integer),
                cast(extract(month from a.startTime) as integer),
                sum(a.totalPriceNok),
                count(a))
            from Appointment a
            where a.status in (no.aurel.backend.appointment.AppointmentStatus.BOOKED,
                               no.aurel.backend.appointment.AppointmentStatus.COMPLETED)
              and a.endTime <= :now
            group by extract(year from a.startTime), extract(month from a.startTime)
            order by extract(year from a.startTime), extract(month from a.startTime)
            """)
    List<MonthlyGross> findMonthlyGross(@Param("now") LocalDateTime now);

    @Query("""
            select new no.aurel.backend.income.dto.UpcomingIncome(count(a), coalesce(sum(a.totalPriceNok), 0L) * 100L)
            from Appointment a
            where a.status = no.aurel.backend.appointment.AppointmentStatus.BOOKED
              and a.endTime > :now
            """)
    UpcomingIncome findUpcoming(@Param("now") LocalDateTime now);
}
