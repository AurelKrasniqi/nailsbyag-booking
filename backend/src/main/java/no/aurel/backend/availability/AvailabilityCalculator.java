package no.aurel.backend.availability;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Pure logic: no Spring, no database, no clock. Everything it needs is passed in, so it is trivial to unit test.
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    /**
     * Which start times can a customer pick on one day for a booking of the given length?
     *
     * @param open    when the employee works (weekly hours + extra opening)
     * @param blocked when she is unavailable (time off); overrides open
     * @param booked  existing appointments
     * @param now     start times that are not after this moment are removed
     */
    public static List<LocalTime> availableStartTimes(
            LocalDate date,
            int durationMinutes,
            int stepMinutes,
            List<TimeRange> open,
            List<TimeRange> blocked,
            List<TimeRange> booked,
            LocalDateTime now) {

        List<TimeRange> taken = new ArrayList<>(blocked);
        taken.addAll(booked);

        List<LocalTime> result = new ArrayList<>();

        for (TimeRange workingBlock : TimeRange.merge(open)) {
            int blockStart = minutesOfDay(workingBlock.start());
            int blockEnd = minutesOfDay(workingBlock.end());

            // First grid point (:00, :30, ...) at or after the start of the working block
            int firstCandidate = ((blockStart + stepMinutes - 1) / stepMinutes) * stepMinutes;

            // Walk the grid; stop when the treatment would run past the end of the block
            for (int minute = firstCandidate; minute + durationMinutes <= blockEnd; minute += stepMinutes) {
                TimeRange candidate = new TimeRange(toTime(minute), toTime(minute + durationMinutes));

                boolean collides = taken.stream().anyMatch(candidate::overlaps);
                boolean inThePast = !date.atTime(candidate.start()).isAfter(now);

                if (!collides && !inThePast) {
                    result.add(candidate.start());
                }
            }
        }
        return result;
    }

    private static int minutesOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static LocalTime toTime(int minutesOfDay) {
        return LocalTime.of(minutesOfDay / 60, minutesOfDay % 60);
    }
}
