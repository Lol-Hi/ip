package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyparser.DurationPeriod;

/**
 * Tests the event task subclass.
 */
class EventTaskTest {

    /** Verifies that an event includes its formatted start and end times. */
    @Test
    void toString_eventTask_includesStartAndEndTimes() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 6, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 6, 16, 0);
        EventTask task = new EventTask("project meeting", start, end);

        assertEquals("[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                task.toString());
        assertEquals(List.of("E", "0", "project meeting",
                        "2026-08-06 14:00", "2026-08-06 16:00"),
                task.getCsvStorageFields());
    }

    /** Verifies that an event ending before it starts is rejected. */
    @Test
    void eventTask_endBeforeStart_throwsIllegalArgumentException() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 6, 16, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 6, 14, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new EventTask("project meeting", start, end));
    }

    /** Verifies that snoozing changes only the event end time. */
    @Test
    void snoozeBy_duration_updatesEndWithoutChangingStart() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 6, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 6, 16, 0);
        EventTask task = new EventTask("project meeting", start, end);

        task.snoozeBy(new DurationPeriod(
                java.time.Period.ZERO, java.time.Duration.ofHours(2)));

        assertEquals(start, task.getStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 6, 18, 0), task.getEndTime());
    }

    /** Verifies that event rescheduling updates both times together. */
    @Test
    void reschedule_validTimes_updatesStartAndEnd() {
        EventTask task = new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 6, 14, 0),
                LocalDateTime.of(2026, 8, 6, 16, 0));
        LocalDateTime newStart = LocalDateTime.of(2026, 8, 7, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 8, 7, 11, 0);

        task.reschedule(newStart, newEnd);

        assertEquals(newStart, task.getStartTime());
        assertEquals(newEnd, task.getEndTime());
    }

    /** Verifies that invalid event rescheduling leaves both times unchanged. */
    @Test
    void reschedule_endBeforeStart_preservesOriginalTimes() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 6, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 6, 16, 0);
        EventTask task = new EventTask("project meeting", start, end);

        assertThrows(IllegalArgumentException.class, () -> task.reschedule(
                LocalDateTime.of(2026, 8, 7, 16, 0),
                LocalDateTime.of(2026, 8, 7, 14, 0)));

        assertEquals(start, task.getStartTime());
        assertEquals(end, task.getEndTime());
    }

}
