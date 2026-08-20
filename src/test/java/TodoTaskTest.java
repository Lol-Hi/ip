import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the ToDo task subclass and the common task state behavior it inherits.
 */
class TodoTaskTest {

    @Test
    void newTodoTaskIsNotDone() {
        TodoTask task = new TodoTask("read book");

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[T][ ] read book", task.toString());
    }

    @Test
    void todoTaskCanBeMarkedAsDone() {
        TodoTask task = new TodoTask("read book");

        task.markAsDone();

        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());
        assertEquals("[T][X] read book", task.toString());
    }

    @Test
    void todoTaskCanBeUnmarkedAsUndone() {
        TodoTask task = new TodoTask("read book");
        task.markAsDone();

        task.unmarkAsUndone();

        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
        assertEquals("[T][ ] read book", task.toString());
    }
}
