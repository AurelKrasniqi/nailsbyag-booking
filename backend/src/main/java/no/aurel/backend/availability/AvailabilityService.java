package no.aurel.backend.availability;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.appointment.Appointment;
import no.aurel.backend.appointment.AppointmentRepository;
import no.aurel.backend.availability.dto.DayAvailabilityResponse;
import no.aurel.backend.employee.EmployeeRepository;
import no.aurel.backend.salonservice.SalonService;
import no.aurel.backend.salonservice.SalonServiceRepository;
import no.aurel.backend.schedule.OpeningHours;
import no.aurel.backend.schedule.OpeningHoursRepository;
import no.aurel.backend.schedule.ScheduleException;
import no.aurel.backend.schedule.ScheduleExceptionRepository;
import no.aurel.backend.schedule.ScheduleExceptionType;

// Glue between the database and the pure AvailabilityCalculator: loads the data once, then asks the calculator per day.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityService {

    // Customers can start a treatment on the hour or half hour
    static final int SLOT_STEP_MINUTES = 30;
    private static final int MAX_DAYS = 62;

    private final EmployeeRepository employeeRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final ScheduleExceptionRepository exceptionRepository;
    private final AppointmentRepository appointmentRepository;

    public List<DayAvailabilityResponse> findAvailability(
            Long employeeId, List<Long> serviceIds, LocalDate from, LocalDate to) {
        if (to.isBefore(from) || from.plusDays(MAX_DAYS).isBefore(to)) {
            throw badRequest("'to' must be on or after 'from', at most " + MAX_DAYS + " days apart");
        }
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee " + employeeId + " not found");
        }
        int duration = totalDurationMinutes(serviceIds);

        List<OpeningHours> weeklyHours = openingHoursRepository.findByEmployeeId(employeeId);
        Map<LocalDate, List<ScheduleException>> exceptionsByDate = exceptionRepository
                .findByEmployeeIdAndDateBetween(employeeId, from, to).stream()
                .collect(Collectors.groupingBy(ScheduleException::getDate));
        Map<LocalDate, List<Appointment>> appointmentsByDate = appointmentRepository
                .findActiveOverlapping(employeeId, from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .collect(Collectors.groupingBy(a -> a.getStartTime().toLocalDate()));

        LocalDateTime now = LocalDateTime.now();
        List<DayAvailabilityResponse> days = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<ScheduleException> exceptions = exceptionsByDate.getOrDefault(date, List.of());

            List<TimeRange> open = new ArrayList<>();
            for (OpeningHours hours : weeklyHours) {
                if (hours.getDayOfWeek() == date.getDayOfWeek()) {
                    open.add(new TimeRange(hours.getStartTime(), hours.getEndTime()));
                }
            }
            exceptions.stream()
                    .filter(e -> e.getType() == ScheduleExceptionType.EXTRA_OPEN)
                    .forEach(e -> open.add(new TimeRange(e.getStartTime(), e.getEndTime())));

            // BLOCKED without times means the whole day
            List<TimeRange> blocked = exceptions.stream()
                    .filter(e -> e.getType() == ScheduleExceptionType.BLOCKED)
                    .map(e -> e.getStartTime() == null
                            ? TimeRange.WHOLE_DAY
                            : new TimeRange(e.getStartTime(), e.getEndTime()))
                    .toList();

            List<TimeRange> booked = appointmentsByDate.getOrDefault(date, List.of()).stream()
                    .map(AvailabilityService::toTimeRange)
                    .toList();

            days.add(new DayAvailabilityResponse(date, AvailabilityCalculator.availableStartTimes(
                    date, duration, SLOT_STEP_MINUTES, open, blocked, booked, now)));
        }
        return days;
    }

    // The cart is booked as one continuous appointment, so the length is the sum of the chosen treatments
    private int totalDurationMinutes(List<Long> serviceIds) {
        Set<Long> distinctIds = new HashSet<>(serviceIds);
        List<SalonService> services = salonServiceRepository.findAllById(distinctIds);
        if (distinctIds.isEmpty() || services.size() != distinctIds.size()) {
            throw badRequest("serviceIds must be a non-empty list of existing services");
        }
        if (services.stream().anyMatch(s -> !s.isActive())) {
            throw badRequest("One or more services are not bookable");
        }
        return services.stream().mapToInt(SalonService::getDurationMinutes).sum();
    }

    // An appointment is stored with full date-times; the calculator works with times within one day
    private static TimeRange toTimeRange(Appointment appointment) {
        LocalTime end = appointment.getEndTime().toLocalDate().isAfter(appointment.getStartTime().toLocalDate())
                ? LocalTime.MAX
                : appointment.getEndTime().toLocalTime();
        return new TimeRange(appointment.getStartTime().toLocalTime(), end);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
