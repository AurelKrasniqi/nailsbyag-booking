package no.aurel.backend.availability.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// One calendar day. An empty startTimes list means the day is closed or fully booked.
public record DayAvailabilityResponse(LocalDate date, List<LocalTime> startTimes) {
}
