import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the state and display behavior of Task.
 */
class TaskTest {

    @Test
    void newTaskIsNotDone() {
        Task task = new Task("read book");

        assertEquals(" ", task.getStatusIcon());
        assertEquals("[ ] read book", task.toString());
    }

    @Test
    void taskCanBeMarkedAsDone() {
        Task task = new Task("read book");

        task.markAsDone();

        assertEquals("X", task.getStatusIcon());
        assertEquals("[X] read book", task.toString());
    }

    @Test
    void taskCanBeUnmarkedAsUndone() {
        Task task = new Task("read book");
        task.markAsDone();

        task.unmarkAsUndone();

        assertEquals(" ", task.getStatusIcon());
        assertEquals("[ ] read book", task.toString());
    }
}
