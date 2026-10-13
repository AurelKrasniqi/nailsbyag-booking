package no.aurel.backend.availability;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import no.aurel.backend.addon.dto.AddOnSelection;
import no.aurel.backend.availability.dto.DayAvailabilityResponse;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;

    // Example: /api/availability?employeeId=2&serviceIds=1,9&addOns=3:4,6:1&from=2026-10-19&to=2026-11-01
    // addOns is optional: a list of "extra id:quantity" pairs
    @GetMapping
    public List<DayAvailabilityResponse> findAvailability(
            @RequestParam Long employeeId,
            @RequestParam List<Long> serviceIds,
            @RequestParam(required = false) List<String> addOns,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findAvailability(employeeId, serviceIds, parseAddOns(addOns), from, to);
    }

    private static List<AddOnSelection> parseAddOns(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        try {
            return raw.stream().map(pair -> {
                String[] parts = pair.split(":");
                if (parts.length != 2) {
                    throw new IllegalArgumentException(pair);
                }
                return new AddOnSelection(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
            }).toList();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "addOns must look like 3:4,6:1 (extra id:quantity)");
        }
    }
}
