package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;


/**
 * Tests the deadline task subclass.
 */
class DeadlineTaskTest {

    /** Verifies that a deadline retains its description and due time. */
    @Test
    void construct_deadlineTask_retainsDescriptionAndDeadline() {
        LocalDateTime deadline = LocalDateTime.of(2019, 10, 15, 14, 15);
        DeadlineTask task = new DeadlineTask("return book", deadline);

        assertEquals("return book", task.getDescription());
        assertEquals(deadline, task.getEndTime());
        assertEquals(TaskTimes.makeDeadlineTimes(deadline), task.getTaskTimes());
    }

    /** Verifies that snoozing extends the deadline by the requested amount. */
    @Test
    void snoozeBy_duration_updatesDeadline() {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 14, 0));

        task.snoozeBy(new DurationPeriod(
                java.time.Period.ZERO, java.time.Duration.ofHours(2)));

        assertEquals(LocalDateTime.of(2026, 8, 26, 16, 0), task.getByTime());
    }

    /** Verifies a null snooze amount uses the exact validation message. */
    @Test
    void snoozeBy_nullAmount_throwsExactException() {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 14, 0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> task.snoozeBy(null));

        assertEquals("Snooze amount cannot be null.", exception.getMessage());
    }

    /** Verifies that rescheduling replaces the deadline. */
    @Test
    void reschedule_newDeadlineTimes_replacesDeadline() {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 14, 0));
        LocalDateTime newDeadline = LocalDateTime.of(2026, 8, 27, 10, 0);

        task.reschedule(TaskTimes.makeDeadlineTimes(newDeadline));

        assertEquals(newDeadline, task.getByTime());
    }

    /** Verifies an invalid deadline replacement uses the exact message. */
    @Test
    void reschedule_nullTimes_throwsExactException() {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 14, 0));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> task.reschedule(null));

        assertEquals("Invalid deadline times.", exception.getMessage());
    }

    /** Verifies that a deadline rejects timing information with a start time. */
    @Test
    void construct_eventTimes_throwsIllegalArgumentException() {
        TaskTimes eventTimes = TaskTimes.makeEventTimes(
                LocalDateTime.of(2026, 8, 26, 12, 0),
                LocalDateTime.of(2026, 8, 26, 13, 0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new DeadlineTask("return book", eventTimes));

        assertEquals("Invalid deadline times.", exception.getMessage());
    }

}
