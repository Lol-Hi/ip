package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(IllegalArgumentException.class, () ->
                Task.TaskStatus.fromStorageValue("2"));
    }
}
