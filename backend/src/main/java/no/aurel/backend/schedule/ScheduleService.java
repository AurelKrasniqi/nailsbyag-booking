package no.aurel.backend.schedule;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.employee.Employee;
import no.aurel.backend.employee.EmployeeRepository;
import no.aurel.backend.schedule.dto.OpeningHoursRequest;
import no.aurel.backend.schedule.dto.OpeningHoursResponse;
import no.aurel.backend.schedule.dto.ScheduleExceptionRequest;
import no.aurel.backend.schedule.dto.ScheduleExceptionResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private static final Comparator<OpeningHours> BY_DAY_THEN_START =
            Comparator.comparing(OpeningHours::getDayOfWeek).thenComparing(OpeningHours::getStartTime);

    private final OpeningHoursRepository openingHoursRepository;
    private final ScheduleExceptionRepository exceptionRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<OpeningHoursResponse> getOpeningHours(Long employeeId) {
        getEmployeeOrThrow(employeeId);
        return openingHoursRepository.findByEmployeeId(employeeId).stream()
                .sorted(BY_DAY_THEN_START)
                .map(OpeningHoursResponse::from)
                .toList();
    }

    // The whole week is replaced in one go: the admin page sends the full weekly schedule when she saves
    public List<OpeningHoursResponse> replaceOpeningHours(Long employeeId, List<OpeningHoursRequest> requests) {
        Employee employee = getEmployeeOrThrow(employeeId);
        validateOpeningHours(requests);

        openingHoursRepository.deleteByEmployeeId(employeeId);
        List<OpeningHours> saved = openingHoursRepository.saveAll(requests.stream().map(r -> {
            OpeningHours entity = new OpeningHours();
            entity.setEmployee(employee);
            entity.setDayOfWeek(r.dayOfWeek());
            entity.setStartTime(r.startTime());
            entity.setEndTime(r.endTime());
            return entity;
        }).toList());

        return saved.stream()
                .sorted(BY_DAY_THEN_START)
                .map(OpeningHoursResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleExceptionResponse> getExceptions(Long employeeId, LocalDate from) {
        getEmployeeOrThrow(employeeId);
        return exceptionRepository
                .findByEmployeeIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(employeeId, from)
                .stream()
                .map(ScheduleExceptionResponse::from)
                .toList();
    }

    public ScheduleExceptionResponse createException(Long employeeId, ScheduleExceptionRequest request) {
        Employee employee = getEmployeeOrThrow(employeeId);
        validateException(request);

        ScheduleException entity = new ScheduleException();
        entity.setEmployee(employee);
        entity.setDate(request.date());
        entity.setStartTime(request.startTime());
        entity.setEndTime(request.endTime());
        entity.setType(request.type());
        return ScheduleExceptionResponse.from(exceptionRepository.save(entity));
    }

    public void deleteException(Long employeeId, Long exceptionId) {
        ScheduleException entity = exceptionRepository.findById(exceptionId)
                .filter(e -> e.getEmployee().getId().equals(employeeId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Exception " + exceptionId + " not found"));
        exceptionRepository.delete(entity);
    }

    private void validateOpeningHours(List<OpeningHoursRequest> requests) {
        for (OpeningHoursRequest r : requests) {
            if (!r.endTime().isAfter(r.startTime())) {
                throw badRequest("End time must be after start time on " + r.dayOfWeek());
            }
        }
        // Two intervals on the same weekday must not overlap
        for (int i = 0; i < requests.size(); i++) {
            for (int j = i + 1; j < requests.size(); j++) {
                OpeningHoursRequest a = requests.get(i);
                OpeningHoursRequest b = requests.get(j);
                boolean overlap = a.dayOfWeek() == b.dayOfWeek()
                        && a.startTime().isBefore(b.endTime())
                        && b.startTime().isBefore(a.endTime());
                if (overlap) {
                    throw badRequest("Opening hours overlap on " + a.dayOfWeek());
                }
            }
        }
    }

    private void validateException(ScheduleExceptionRequest request) {
        boolean hasStart = request.startTime() != null;
        boolean hasEnd = request.endTime() != null;
        if (hasStart != hasEnd) {
            throw badRequest("Give both startTime and endTime, or neither for a whole-day exception");
        }
        if (hasStart && !request.endTime().isAfter(request.startTime())) {
            throw badRequest("End time must be after start time");
        }
        if (request.type() == ScheduleExceptionType.EXTRA_OPEN && !hasStart) {
            throw badRequest("EXTRA_OPEN needs startTime and endTime");
        }
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee " + id + " not found"));
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
