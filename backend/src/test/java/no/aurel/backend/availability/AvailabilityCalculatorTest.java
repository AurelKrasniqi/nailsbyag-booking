package no.aurel.backend.availability;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

class AvailabilityCalculatorTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 20);
    // "Now" is the day before, so nothing counts as being in the past unless a test says so
    private static final LocalDateTime LONG_BEFORE = DAY.minusDays(1).atStartOfDay();
    private static final int STEP = 30;

    private static TimeRange range(String start, String end) {
        return new TimeRange(LocalTime.parse(start), LocalTime.parse(end));
    }

    private static List<LocalTime> times(String... values) {
        return java.util.Arrays.stream(values).map(LocalTime::parse).toList();
    }

    private static List<LocalTime> calculate(
            int duration, List<TimeRange> open, List<TimeRange> blocked, List<TimeRange> booked, LocalDateTime now) {
        return AvailabilityCalculator.availableStartTimes(DAY, duration, STEP, open, blocked, booked, now);
    }

    @Test
    void emptyDayGivesEverySlotWhereTheTreatmentFits() {
        List<LocalTime> result = calculate(60, List.of(range("10:00", "12:00")), List.of(), List.of(), LONG_BEFORE);

        assertThat(result).isEqualTo(times("10:00", "10:30", "11:00"));
    }

    @Test
    void treatmentMustFinishBeforeClosing() {
        // 105 min starting 16:30 would end 18:15, after closing
        List<LocalTime> result = calculate(105, List.of(range("15:00", "18:00")), List.of(), List.of(), LONG_BEFORE);

        assertThat(result).isEqualTo(times("15:00", "15:30", "16:00"));
    }

    @Test
    void existingBookingRemovesOverlappingSlotsButNotTouchingOnes() {
        List<LocalTime> result = calculate(
                60, List.of(range("10:00", "14:00")), List.of(), List.of(range("11:00", "12:00")), LONG_BEFORE);

        // 10:30 would run 10:30-11:30 into the booking; 10:00 ends exactly when it starts; 12:00 starts exactly when it ends
        assertThat(result).isEqualTo(times("10:00", "12:00", "12:30", "13:00"));
    }

    @Test
    void slotsAreOnTheGridEvenWhenABookingEndsOffGrid() {
        List<LocalTime> result = calculate(
                60, List.of(range("10:00", "14:00")), List.of(), List.of(range("10:00", "12:15")), LONG_BEFORE);

        assertThat(result).isEqualTo(times("12:30", "13:00"));
    }

    @Test
    void wholeDayBlockRemovesEverything() {
        List<LocalTime> result = calculate(
                60, List.of(range("10:00", "18:00")), List.of(TimeRange.WHOLE_DAY), List.of(), LONG_BEFORE);

        assertThat(result).isEmpty();
    }

    @Test
    void partialBlockRemovesOnlyThatPartOfTheDay() {
        List<LocalTime> result = calculate(
                60, List.of(range("10:00", "14:00")), List.of(range("12:00", "14:00")), List.of(), LONG_BEFORE);

        assertThat(result).isEqualTo(times("10:00", "10:30", "11:00"));
    }

    @Test
    void noOpeningHoursGivesNoSlots() {
        assertThat(calculate(60, List.of(), List.of(), List.of(), LONG_BEFORE)).isEmpty();
    }

    @Test
    void lunchBreakSplitsTheDay() {
        List<LocalTime> result = calculate(
                120, List.of(range("10:00", "12:00"), range("13:00", "15:00")), List.of(), List.of(), LONG_BEFORE);

        assertThat(result).isEqualTo(times("10:00", "13:00"));
    }

    @Test
    void touchingOpeningRangesAreTreatedAsOne() {
        List<LocalTime> result = calculate(
                180, List.of(range("10:00", "12:00"), range("12:00", "14:00")), List.of(), List.of(), LONG_BEFORE);

        assertThat(result).isEqualTo(times("10:00", "10:30", "11:00"));
    }

    @Test
    void slotsThatHaveAlreadyStartedAreRemoved() {
        LocalDateTime now = DAY.atTime(11, 10);

        List<LocalTime> result = calculate(60, List.of(range("10:00", "13:00")), List.of(), List.of(), now);

        assertThat(result).isEqualTo(times("11:30", "12:00"));
    }

    @Test
    void openingTimeOffTheGridStartsAtNextGridPoint() {
        List<LocalTime> result = calculate(60, List.of(range("10:10", "12:00")), List.of(), List.of(), LONG_BEFORE);

        assertThat(result).isEqualTo(times("10:30", "11:00"));
    }
}
