package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the task completion status representations.
 */
class TaskStatusTest {

    /** Verifies that each status exposes the expected display icon. */
    @Test
    void getDisplayIcon_eachStatus_returnsExpectedIcon() {
        assertEquals(" ", Task.TaskStatus.NOT_DONE.getDisplayIcon());
        assertEquals("X", Task.TaskStatus.DONE.getDisplayIcon());
    }

    /** Verifies that each status exposes the expected CSV value. */
    @Test
    void getStorageValue_eachStatus_returnsExpectedValue() {
        assertEquals("0", Task.TaskStatus.NOT_DONE.getStorageValue());
        assertEquals("1", Task.TaskStatus.DONE.getStorageValue());
    }

    /** Verifies that valid CSV values are converted into statuses. */
    @Test
    void fromStorageValue_validValues_returnsExpectedStatus() {
        assertEquals(Task.TaskStatus.NOT_DONE,
                Task.TaskStatus.fromStorageValue("0"));
        assertEquals(Task.TaskStatus.DONE,
                Task.TaskStatus.fromStorageValue("1"));
    }

    /** Verifies that invalid CSV values are rejected. */
    @Test
    void fromStorageValue_invalidValue_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Task.TaskStatus.fromStorageValue("2"));

        assertEquals("Invalid completion status: 2", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () ->
                Task.TaskStatus.fromStorageValue(null));

        assertEquals("Completion status cannot be null.", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () ->
                Task.TaskStatus.fromStorageValue(""));

        assertEquals("Invalid completion status: ", exception.getMessage());
    }

    /** Verifies status transitions behave consistently for every task type. */
    @Test
    void taskStatus_allTaskTypes_supportIdempotentTransitions() {
        List<Task> tasks = List.of(
                new TodoTask("read book"),
                new DeadlineTask("return book", LocalDateTime.of(2026, 8, 26, 12, 0)),
                new EventTask(
                        "project meeting",
                        LocalDateTime.of(2026, 8, 26, 14, 0),
                        LocalDateTime.of(2026, 8, 26, 16, 0)));

        for (Task task : tasks) {
            assertFalse(task.isDone());
            task.markAsDone();
            task.markAsDone();
            assertTrue(task.isDone());
            task.unmarkAsUndone();
            task.unmarkAsUndone();
            assertFalse(task.isDone());
        }
    }
}
