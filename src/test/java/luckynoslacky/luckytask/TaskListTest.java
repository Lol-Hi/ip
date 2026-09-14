package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests indexed task-list results and their display representation.
 */
class TaskListTest {

    /** Verifies that tasks are stored and numbered sequentially. */
    @Test
    void addTask_multipleTasks_assignsSequentialNumbers() {
        TaskList taskList = new TaskList();
        Task firstTask = new TodoTask("first task");
        Task secondTask = new TodoTask("second task");

        taskList.addTask(firstTask);
        taskList.addTask(secondTask);

        assertEquals(2, taskList.size());
        assertEquals(firstTask, taskList.getTask(1));
        assertEquals(secondTask, taskList.getTask(2));
    }

    /** Verifies that removing a task renumbers the remaining tasks. */
    @Test
    void removeTask_middleTask_renumbersRemainingTasks() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("first task"));
        taskList.addTask(new TodoTask("second task"));
        taskList.addTask(new TodoTask("third task"));

        assertEquals("[T][ ] second task", taskList.removeTask(2).toString());
        assertEquals("1.[T][ ] first task\n2.[T][ ] third task",
                taskList.toDisplayString());
    }

    /** Verifies that loaded tasks replace the existing canonical collection. */
    @Test
    void replaceTasks_loadedTasks_replacesExistingTasks() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("old task"));

        taskList.replaceTasks(List.of(
                new TodoTask("first loaded task"),
                new TodoTask("second loaded task")));

        assertEquals(2, taskList.size());
        assertEquals("1.[T][ ] first loaded task\n2.[T][ ] second loaded task",
                taskList.toDisplayString());
    }

    /** Verifies that display formatting preserves original task numbers. */
    @Test
    void toDisplayString_filteredTasks_preservesOriginalNumbers() {
        TaskList taskList = new TaskList();
        TodoTask readBook = new TodoTask("read book");
        TodoTask buyBread = new TodoTask("buy bread");
        taskList.addTask(new TodoTask("first task"));
        taskList.addTask(readBook);
        taskList.addTask(new TodoTask("second task"));
        taskList.addTask(buyBread);

        TaskList matchingTasks = taskList.createView(
                null, task -> task == readBook || task == buyBread);

        assertEquals("2.[T][ ] read book\n4.[T][ ] buy bread",
                matchingTasks.toDisplayString());
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new TaskList().addTask(null));

        assertEquals("Task cannot be null.", exception.getMessage());
    }

    /** Verifies that an invalid task number is rejected by the task list. */
    @Test
    void getTask_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("read book"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> taskList.getTask(0));
        assertEquals("Invalid task number.", exception.getMessage());
        exception = assertThrows(
                IllegalArgumentException.class, () -> taskList.getTask(2));
        assertEquals("Invalid task number.", exception.getMessage());
    }

    /** Verifies that replacing with a null list is rejected safely. */
    @Test
    void replaceTasks_nullList_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new TaskList().replaceTasks(null));

        assertEquals("Tasks cannot be null.", exception.getMessage());
    }

    /** Verifies that replacing with a null task preserves existing tasks. */
    @Test
    void replaceTasks_nullTask_preservesExistingTasks() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("existing task"));
        List<Task> loadedTasks = new ArrayList<>();
        loadedTasks.add(new TodoTask("new task"));
        loadedTasks.add(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                taskList.replaceTasks(loadedTasks));
        assertEquals("Task cannot be null.", exception.getMessage());
        assertEquals("1.[T][ ] existing task", taskList.toDisplayString());
    }

    /** Verifies that non-positive task numbers are rejected by IndexedTask. */
    @Test
    void constructIndexedTask_nonPositiveNumber_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new TaskList.IndexedTask(0, new TodoTask("read book")));

        assertEquals("Task number must be positive.", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () ->
                new TaskList.IndexedTask(1, null));

        assertEquals("Task cannot be null.", exception.getMessage());
    }

    /** Verifies that a task-list view preserves original task numbers. */
    @Test
    void createView_matchingPredicate_preservesOriginalTaskNumbers() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("read book"));
        taskList.addTask(new TodoTask("buy bread"));
        taskList.addTask(new TodoTask("return book"));

        TaskList result = taskList.createView(
                null, task -> task.matchesDescription("book"));

        assertEquals("1.[T][ ] read book\n3.[T][ ] return book",
                result.toDisplayString());
    }

    /** Verifies that a null view matcher is rejected. */
    @Test
    void createView_nullMatcher_throwsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new TaskList().createView(null, null));

        assertEquals("Matcher cannot be null.", exception.getMessage());
    }

    /** Verifies inserting tasks at each valid position preserves task order. */
    @Test
    void insertTask_validPositions_renumbersTasksSequentially() {
        TaskList taskList = new TaskList();
        Task firstTask = new TodoTask("first task");
        Task middleTask = new TodoTask("middle task");
        Task lastTask = new TodoTask("last task");

        taskList.addTask(firstTask);
        taskList.addTask(lastTask);
        taskList.insertTask(2, middleTask);
        taskList.insertTask(1, new TodoTask("new first task"));
        taskList.insertTask(5, new TodoTask("new last task"));

        assertEquals(5, taskList.size());
        assertEquals("1.[T][ ] new first task\n"
                        + "2.[T][ ] first task\n"
                        + "3.[T][ ] middle task\n"
                        + "4.[T][ ] last task\n"
                        + "5.[T][ ] new last task",
                taskList.toDisplayString());
        assertEquals(lastTask, taskList.getTask(4));
    }

    /** Verifies invalid insertion positions use the exact validation message. */
    @Test
    void insertTask_invalidPosition_throwsExactException() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("existing task"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                taskList.insertTask(0, new TodoTask("invalid task")));
        assertEquals("Invalid task number.", exception.getMessage());
        exception = assertThrows(IllegalArgumentException.class, () ->
                taskList.insertTask(3, new TodoTask("invalid task")));
        assertEquals("Invalid task number.", exception.getMessage());
        assertEquals("1.[T][ ] existing task", taskList.toDisplayString());
    }

    /** Verifies a view retains its indexed entries after the source list changes. */
    @Test
    void createView_sourceListMutation_preservesExistingView() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("first task"));
        taskList.addTask(new TodoTask("second task"));

        TaskList view = taskList.createView(null, task -> true);
        taskList.removeTask(1);

        assertEquals("1.[T][ ] first task\n2.[T][ ] second task",
                view.toDisplayString());
        assertEquals("1.[T][ ] second task", taskList.toDisplayString());
    }
}
