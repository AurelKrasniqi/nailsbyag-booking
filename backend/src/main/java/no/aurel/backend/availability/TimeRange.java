package no.aurel.backend.availability;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// A half-open time interval [start, end): 10:00-11:00 and 11:00-12:00 touch but do not overlap.
public record TimeRange(LocalTime start, LocalTime end) {

    public static final TimeRange WHOLE_DAY = new TimeRange(LocalTime.MIN, LocalTime.MAX);

    public TimeRange {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be after start: " + start + " - " + end);
        }
    }

    public boolean overlaps(TimeRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    // Joins ranges that overlap or touch, so 10-12 + 12-14 becomes 10-14 (a 3 hour treatment fits across the seam)
    public static List<TimeRange> merge(List<TimeRange> ranges) {
        List<TimeRange> sorted = ranges.stream().sorted(Comparator.comparing(TimeRange::start)).toList();
        List<TimeRange> merged = new ArrayList<>();
        for (TimeRange next : sorted) {
            if (merged.isEmpty()) {
                merged.add(next);
                continue;
            }
            TimeRange last = merged.getLast();
            if (next.start().isAfter(last.end())) {
                merged.add(next);
            } else if (next.end().isAfter(last.end())) {
                merged.set(merged.size() - 1, new TimeRange(last.start(), next.end()));
            }
        }
        return merged;
    }
}
