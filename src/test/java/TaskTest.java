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

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[T][ ] read book", task.toString());
    }

    @Test
    void taskCanBeMarkedAsDone() {
        Task task = new Task("read book");

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
        assertEquals("[T][X] read book", task.toString());
    }

    @Test
    void taskCanBeUnmarkedAsUndone() {
        Task task = new Task("read book");
        task.markAsDone();

        task.unmarkAsUndone();

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[T][ ] read book", task.toString());
    }

    @Test
    void deadlineTaskIncludesDeadlineInOutput() {
        Task task = new Task("return book", "Sunday");

        assertEquals("[D][ ] return book (by: Sunday)", task.toString());
    }

    @Test
    void eventTaskIncludesStartAndEndTimesInOutput() {
        Task task = new Task("project meeting", "Mon 2pm", "4pm");

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                task.toString());
    }

}
