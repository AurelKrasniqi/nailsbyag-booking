package no.aurel.backend.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import no.aurel.backend.schedule.ScheduleExceptionType;

// startTime/endTime are both omitted for a whole-day BLOCKED exception
public record ScheduleExceptionRequest(
        @NotNull LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        @NotNull ScheduleExceptionType type
) {
}
