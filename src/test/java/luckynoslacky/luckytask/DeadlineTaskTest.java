package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyparser.DurationPeriod;

/**
 * Tests the deadline task subclass.
 */
class DeadlineTaskTest {

    /** Verifies that a deadline includes its formatted due time. */
    @Test
    void toString_deadlineTask_includesDeadline() {
        LocalDateTime deadline = LocalDateTime.of(2019, 10, 15, 14, 15);
        DeadlineTask task = new DeadlineTask("return book", deadline);

        assertEquals("[D][ ] return book (by: Tue Oct 15 2019, 2.15pm)",
                task.toString());
        assertEquals(List.of("D", "0", "return book", "", "2019-10-15 14:15"),
                task.getCsvStorageFields());
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

    /** Verifies that rescheduling replaces the deadline. */
    @Test
    void rescheduleTo_newTime_replacesDeadline() {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 14, 0));
        LocalDateTime newDeadline = LocalDateTime.of(2026, 8, 27, 10, 0);

        task.rescheduleTo(newDeadline);

        assertEquals(newDeadline, task.getByTime());
    }

}
