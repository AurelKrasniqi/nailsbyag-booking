package no.aurel.backend.schedule.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import no.aurel.backend.schedule.OpeningHours;

public record OpeningHoursResponse(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {

    public static OpeningHoursResponse from(OpeningHours entity) {
        return new OpeningHoursResponse(
                entity.getId(), entity.getDayOfWeek(), entity.getStartTime(), entity.getEndTime());
    }
}
