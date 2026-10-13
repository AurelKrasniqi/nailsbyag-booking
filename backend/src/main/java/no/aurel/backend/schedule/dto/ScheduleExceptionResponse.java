package no.aurel.backend.schedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import no.aurel.backend.schedule.ScheduleException;
import no.aurel.backend.schedule.ScheduleExceptionType;

public record ScheduleExceptionResponse(
        Long id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        ScheduleExceptionType type
) {

    public static ScheduleExceptionResponse from(ScheduleException entity) {
        return new ScheduleExceptionResponse(
                entity.getId(), entity.getDate(), entity.getStartTime(), entity.getEndTime(), entity.getType());
    }
}
