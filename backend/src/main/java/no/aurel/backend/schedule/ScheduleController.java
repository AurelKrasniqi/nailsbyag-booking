package no.aurel.backend.schedule;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.aurel.backend.schedule.dto.OpeningHoursRequest;
import no.aurel.backend.schedule.dto.OpeningHoursResponse;
import no.aurel.backend.schedule.dto.ScheduleExceptionRequest;
import no.aurel.backend.schedule.dto.ScheduleExceptionResponse;

@RestController
@RequestMapping("/api/employees/{employeeId}")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService service;
    private final Clock clock;

    @GetMapping("/opening-hours")
    public List<OpeningHoursResponse> getOpeningHours(@PathVariable Long employeeId) {
        return service.getOpeningHours(employeeId);
    }

    @PutMapping("/opening-hours")
    public List<OpeningHoursResponse> replaceOpeningHours(
            @PathVariable Long employeeId, @RequestBody List<@Valid OpeningHoursRequest> requests) {
        return service.replaceOpeningHours(employeeId, requests);
    }

    // Only upcoming exceptions are interesting, so "from" defaults to today
    @GetMapping("/schedule-exceptions")
    public List<ScheduleExceptionResponse> getExceptions(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from) {
        return service.getExceptions(employeeId, from != null ? from : LocalDate.now(clock));
    }

    @PostMapping("/schedule-exceptions")
    public ResponseEntity<ScheduleExceptionResponse> createException(
            @PathVariable Long employeeId, @Valid @RequestBody ScheduleExceptionRequest request) {
        ScheduleExceptionResponse created = service.createException(employeeId, request);
        return ResponseEntity
                .created(URI.create("/api/employees/" + employeeId + "/schedule-exceptions/" + created.id()))
                .body(created);
    }

    @DeleteMapping("/schedule-exceptions/{exceptionId}")
    public ResponseEntity<Void> deleteException(@PathVariable Long employeeId, @PathVariable Long exceptionId) {
        service.deleteException(employeeId, exceptionId);
        return ResponseEntity.noContent().build();
    }
}
