package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests indexed task-list results and their display representation.
 */
class TaskListTest {

    /** Verifies that display formatting preserves original task numbers. */
    @Test
    void toDisplayString_indexedTasks_preservesOriginalNumbers() {
        TaskList taskList = new TaskList();
        taskList.addTask(2, new TodoTask("read book"));
        taskList.addTask(4, new TodoTask("buy bread"));

        assertEquals("2.[T][ ] read book\n4.[T][ ] buy bread",
                taskList.toDisplayString());
    }

    /** Verifies that a date-search result retains its search-date context. */
    @Test
    void getSearchDate_dateSearch_returnsSearchDate() {
        LocalDate searchDate = LocalDate.of(2026, 8, 26);
        TaskList taskList = new TaskList(searchDate);

        assertEquals(searchDate, taskList.getSearchDate().orElseThrow());
    }

    /** Verifies that an empty task list has no display lines. */
    @Test
    void toDisplayString_emptyTaskList_returnsEmptyString() {
        TaskList taskList = new TaskList();

        assertEquals("", taskList.toDisplayString());
    }

    /** Verifies that a null search date creates a non-date task list. */
    @Test
    void construct_nullSearchDate_createsNonDateTaskList() {
        TaskList taskList = new TaskList(null);

        assertTrue(taskList.getSearchDate().isEmpty());
    }

    /** Verifies that adding a null task is rejected. */
    @Test
    void addTask_nullTask_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TaskList().addTask(1, null));
    }
}
