package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

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
}
