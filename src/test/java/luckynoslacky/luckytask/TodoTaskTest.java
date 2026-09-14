package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyparser.DurationPeriod;

/**
 * Tests the ToDo task subclass and the common task state behavior it inherits.
 */
class TodoTaskTest {

    /** Verifies the initial status and storage/display form of a ToDo. */
    @Test
    void todoTask_newTask_isNotDone() {
        TodoTask task = new TodoTask("read book");

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[T][ ] read book", task.toString());
        assertEquals(List.of("T", "0", "read book", "", ""),
                task.getCsvStorageFields());
    }

    /** Verifies null and blank descriptions use the exact validation message. */
    @Test
    void todoTask_nullOrBlankDescription_throwsExactException() {
        for (String description : new String[] {null, "", "   "}) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> new TodoTask(description));

            assertEquals("Task description cannot be empty.", exception.getMessage());
        }
    }

    /** Verifies that marking an incomplete ToDo sets it to done. */
    @Test
    void markAsDone_incompleteTask_isDone() {
        TodoTask task = new TodoTask("read book");

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
        assertEquals("[T][X] read book", task.toString());
    }

    /** Verifies that unmarking a done ToDo clears its status. */
    @Test
    void unmarkAsUndone_doneTask_isNotDone() {
        TodoTask task = new TodoTask("read book");
        task.markAsDone();

        task.unmarkAsUndone();

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[T][ ] read book", task.toString());
    }

    /** Verifies that snoozing a ToDo is rejected. */
    @Test
    void snoozeBy_todoTask_throwsIllegalArgumentException() {
        TodoTask task = new TodoTask("read book");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                task.snoozeBy(new DurationPeriod(Period.ZERO, Duration.ofHours(1))));

        assertEquals("ToDos cannot be snoozed.", exception.getMessage());
    }

    /** Verifies that replacing a ToDo ending time is rejected. */
    @Test
    void reschedule_todoTask_throwsIllegalArgumentException() {
        TodoTask task = new TodoTask("read book");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                task.reschedule(TaskTimes.makeDeadlineTimes(
                        LocalDateTime.of(2026, 8, 26, 14, 0))));

        assertEquals("ToDos cannot be rescheduled.", exception.getMessage());
    }

    /** Verifies that requesting a ToDo ending time is rejected. */
    @Test
    void getEndTime_todoTask_throwsIllegalArgumentException() {
        TodoTask task = new TodoTask("read book");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, task::getEndTime);

        assertEquals("Task has no ending time.", exception.getMessage());
    }
}
