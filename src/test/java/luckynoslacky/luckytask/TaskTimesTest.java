package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests the immutable timing value object used by timed tasks. */
class TaskTimesTest {

    /** Verifies that no-time values contain neither a start nor an end time. */
    @Test
    void none_noTaskTimes_returnsEmptyTimingInformation() {
        TaskTimes times = TaskTimes.none();

        assertNull(times.getStartTime());
        assertNull(times.getEndTime());
    }

    /** Verifies that deadline values contain only their end time. */
    @Test
    void makeDeadlineTimes_endTime_returnsEndOnlyTimingInformation() {
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 26, 12, 0);

        TaskTimes times = TaskTimes.makeDeadlineTimes(endTime);

        assertNull(times.getStartTime());
        assertEquals(endTime, times.getEndTime());
    }

    /** Verifies that a null deadline end time is rejected. */
    @Test
    void makeDeadlineTimes_nullEndTime_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.makeDeadlineTimes(null));
    }

    /** Verifies that event values retain both event times. */
    @Test
    void makeEventTimes_validTimes_returnsBothEventTimes() {
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 26, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 26, 13, 0);

        TaskTimes times = TaskTimes.makeEventTimes(startTime, endTime);

        assertEquals(startTime, times.getStartTime());
        assertEquals(endTime, times.getEndTime());
    }

    /** Verifies that an event ending early is rejected. */
    @Test
    void makeEventTimes_endBeforeStart_throwsIllegalArgumentException() {
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 26, 13, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 26, 12, 0);

        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.makeEventTimes(startTime, endTime));
    }

    /** Verifies that an event with a missing time is rejected. */
    @Test
    void makeEventTimes_nullTime_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.makeEventTimes(
                        null, LocalDateTime.of(2026, 8, 26, 12, 0)));
    }

    /** Verifies that deadline-shaped timing passes deadline verification. */
    @Test
    void verifyDeadlineTimes_deadlineShape_acceptsTimingInformation() {
        TaskTimes times = TaskTimes.makeDeadlineTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0));

        TaskTimes.verifyDeadlineTimes(times);
    }

    /** Verifies that event-shaped timing is rejected as a deadline. */
    @Test
    void verifyDeadlineTimes_eventShape_throwsIllegalArgumentException() {
        TaskTimes times = TaskTimes.makeEventTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0),
                LocalDateTime.of(2026, 8, 26, 13, 0));

        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.verifyDeadlineTimes(times));
    }

    /** Verifies that event-shaped timing passes event verification. */
    @Test
    void verifyEventTimes_eventShape_acceptsTimingInformation() {
        TaskTimes times = TaskTimes.makeEventTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0),
                LocalDateTime.of(2026, 8, 26, 13, 0));

        TaskTimes.verifyEventTimes(times);
    }

    /** Verifies that deadline-shaped timing is rejected as an event. */
    @Test
    void verifyEventTimes_deadlineShape_throwsIllegalArgumentException() {
        TaskTimes times = TaskTimes.makeDeadlineTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0));

        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.verifyEventTimes(times));
    }

    /** Verifies that replacing a schedule end preserves its shape. */
    @Test
    void withEndTime_replacementEnd_preservesScheduleShape() {
        LocalDateTime startTime = LocalDateTime.of(2026, 8, 26, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 8, 26, 13, 0);
        LocalDateTime replacementEnd = LocalDateTime.of(2026, 8, 26, 14, 0);

        TaskTimes eventTimes = TaskTimes.makeEventTimes(startTime, endTime);
        TaskTimes deadlineTimes = TaskTimes.makeDeadlineTimes(endTime);

        assertEquals(
                TaskTimes.makeEventTimes(startTime, replacementEnd),
                eventTimes.withEndTime(replacementEnd));
        assertEquals(
                TaskTimes.makeDeadlineTimes(replacementEnd),
                deadlineTimes.withEndTime(replacementEnd));
    }

    /** Verifies that replacing the end of a ToDo is rejected. */
    @Test
    void withEndTime_noEndTime_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.none().withEndTime(
                        LocalDateTime.of(2026, 8, 26, 14, 0)));
    }

    /** Verifies that ToDo timing never matches a date. */
    @Test
    void occursOn_noTaskTimes_returnsFalse() {
        assertFalse(TaskTimes.none().occursOn(LocalDate.of(2026, 8, 26)));
    }

    /** Verifies that a deadline matches only its end date. */
    @Test
    void occursOn_deadlineDate_matchesOnlyEndDate() {
        TaskTimes times = TaskTimes.makeDeadlineTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0));

        assertTrue(times.occursOn(LocalDate.of(2026, 8, 26)));
        assertFalse(times.occursOn(LocalDate.of(2026, 8, 27)));
    }

    /** Verifies that an event matches every date in its inclusive range. */
    @Test
    void occursOn_eventDate_matchesInclusiveDateRange() {
        TaskTimes times = TaskTimes.makeEventTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0),
                LocalDateTime.of(2026, 8, 28, 13, 0));

        assertTrue(times.occursOn(LocalDate.of(2026, 8, 26)));
        assertTrue(times.occursOn(LocalDate.of(2026, 8, 27)));
        assertTrue(times.occursOn(LocalDate.of(2026, 8, 28)));
        assertFalse(times.occursOn(LocalDate.of(2026, 8, 29)));
    }

    /** Verifies that date matching rejects a null query date. */
    @Test
    void occursOn_nullDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                TaskTimes.none().occursOn(null));
    }
}
