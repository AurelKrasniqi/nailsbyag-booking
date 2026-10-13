package no.aurel.backend.availability;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.availability.dto.DayAvailabilityResponse;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;

    // Example: /api/availability?employeeId=2&serviceIds=1,9&from=2026-10-19&to=2026-11-01
    @GetMapping
    public List<DayAvailabilityResponse> findAvailability(
            @RequestParam Long employeeId,
            @RequestParam List<Long> serviceIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findAvailability(employeeId, serviceIds, from, to);
    }
}
