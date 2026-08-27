package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckystorage.CsvSaver;

/**
 * Tests the task storage and listing behavior of TaskMaster.
 */
class TaskMasterTest {
    private static final LocalDateTime DEADLINE =
            LocalDateTime.of(2026, 12, 6, 23, 59);
    private static final LocalDateTime EVENT_START =
            LocalDateTime.of(2026, 8, 6, 14, 0);
    private static final LocalDateTime EVENT_END =
            LocalDateTime.of(2026, 8, 6, 16, 0);

    @TempDir
    Path temporaryDirectory;

    /** Verifies the response for an empty task list. */
    @Test
    void listTasks_emptyTaskList_returnsEmptyTaskListMessage() {
        TaskMaster taskMaster = createTaskMaster();

        assertEquals("Chill lah bro got nothing yet lah!", taskMaster.listTasks());
    }

    /** Verifies that a valid task is added and listed. */
    @Test
    void addTask_validTask_includesTaskInList() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));

        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    /** Verifies that listing preserves task insertion order. */
    @Test
    void listTasks_multipleTasks_preservesOrder() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][ ] return book",
                taskMaster.listTasks());
    }

    /** Verifies that all supported task types are formatted in a list. */
    @Test
    void listTasks_differentTaskTypes_formatsAllTypes() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("borrow book"));
        taskMaster.addTask(new DeadlineTask("return book", DEADLINE));
        taskMaster.addTask(new EventTask("project meeting", EVENT_START, EVENT_END));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] borrow book\n"
                        + "2.[D][ ] return book (by: Sun Dec 06 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                taskMaster.listTasks());
    }

    /** Verifies that search returns deadlines and events on a date. */
    @Test
    void searchTasks_matchingDate_returnsDeadlinesAndEvents() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 25, 14, 0),
                LocalDateTime.of(2026, 8, 27, 16, 0)));

        assertEquals("Nah, all these things you need to do on: Aug 26 2026\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Tue Aug 25 2026, 2.00pm"
                        + " to: Thu Aug 27 2026, 4.00pm)",
                taskMaster.searchTasks(LocalDateTime.of(2026, 8, 26, 0, 0)));
    }

    /** Verifies that search excludes ToDos and dates without matches. */
    @Test
    void searchTasks_noMatchingDateOrTodoOnly_returnsNoMatchMessage() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));

        assertEquals("Wah, you very free hor, got nothing to do sia!",
                taskMaster.searchTasks(LocalDateTime.of(2026, 8, 25, 0, 0)));
        assertEquals("Wah, you very free hor, got nothing to do sia!",
                taskMaster.searchTasks(LocalDateTime.of(2026, 8, 27, 0, 0)));
    }

    /** Verifies that marking a valid task persists its done status. */
    @Test
    void searchTasks_descriptionQuery_matchesDescriptionsCaseInsensitively() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        taskMaster.addTask(new TodoTask("buy bread"));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)",
                taskMaster.searchTasks("BOOK"));
    }

    @Test
    void searchTasks_descriptionAndDateQueries_returnsIntersection() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        taskMaster.addTask(new EventTask(
                "book meeting",
                LocalDateTime.of(2026, 8, 27, 14, 0),
                LocalDateTime.of(2026, 8, 27, 16, 0)));

        assertEquals("Nah, all these things you need to do on: Aug 26 2026\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)",
                taskMaster.searchTasks(
                        "book",
                        LocalDateTime.of(2026, 8, 26, 0, 0)));
    }

    @Test
    void markTaskDone_validTask_marksTaskAsDone() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));
        taskMaster.markTaskDone(2);

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][X] return book",
                taskMaster.listTasks());
    }

    /** Verifies that unmarking a done task clears its status. */
    @Test
    void unmarkTaskUndone_doneTask_marksTaskAsUndone() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.markTaskDone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    /** Verifies that marking an already done task is idempotent. */
    @Test
    void markTaskDone_alreadyDoneTask_remainsDone() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.markTaskDone(1);
        taskMaster.markTaskDone(1);

        assertEquals("Nah, all these things you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    /** Verifies that unmarking an incomplete task is idempotent. */
    @Test
    void unmarkTaskUndone_alreadyUndoneTask_remainsUndone() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.unmarkTaskUndone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    /** Verifies that deletion removes and renumbers later tasks. */
    @Test
    void deleteTask_middleTask_removesAndRenumbersTasks() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new DeadlineTask("second", DEADLINE));
        taskMaster.addTask(new EventTask("third", EVENT_START, EVENT_END));

        assertEquals("[D][ ] second (by: Sun Dec 06 2026, 11.59pm)",
                taskMaster.deleteTask(2));
        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] first\n"
                        + "2.[E][ ] third (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                taskMaster.listTasks());
    }

    /** Verifies that an invalid deletion leaves the list unchanged. */
    @Test
    void deleteTask_invalidTaskNumber_preservesTaskList() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.deleteTask(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    /** Verifies that deletion frees capacity for a later task. */
    @Test
    void deleteTask_fullList_freesCapacity() {
        TaskMaster taskMaster = createTaskMaster(2);
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        taskMaster.deleteTask(1);
        taskMaster.addTask(new TodoTask("third"));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] second\n"
                        + "2.[T][ ] third",
                taskMaster.listTasks());
    }

    /** Verifies that deletion from an empty list is rejected. */
    @Test
    void deleteTask_emptyList_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.deleteTask(1));
    }

    /** Verifies that invalid numbers are rejected when marking tasks. */
    @Test
    void markTaskDone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
    }

    /** Verifies that invalid numbers are rejected when unmarking tasks. */
    @Test
    void unmarkTaskUndone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
    }

    /** Verifies invalid status changes do not alter another task's state. */
    @Test
    void markOrUnmark_invalidTaskNumber_preservesTaskState() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());

        taskMaster.markTaskDone(1);
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    /** Verifies that the configured task capacity is enforced. */
    @Test
    void addTask_customCapacity_throwsWhenFull() {
        TaskMaster taskMaster = createTaskMaster(2);

        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        assertThrows(IllegalStateException.class,
                () -> taskMaster.addTask(new TodoTask("third")));
    }

    /** Verifies that non-positive capacities are rejected. */
    @Test
    void taskMaster_nonPositiveCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(0));

        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(-1));
    }

    /** Verifies that a failed save rolls back an added task. */
    @Test
    void addTask_saveFailure_removesTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.addTask(new TodoTask("read book")));
        assertEquals(0, taskMaster.getTaskCount());
    }

    /** Verifies that a failed save rolls back a status change. */
    @Test
    void markTaskDone_saveFailure_restoresPreviousStatus() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.markTaskDone(1));
        assertFalse(task.isDone());
    }

    /** Verifies that a failed save restores a deleted task. */
    @Test
    void deleteTask_saveFailure_restoresDeletedTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.deleteTask(1));
        assertEquals(1, taskMaster.getTaskCount());
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    /** Verifies that null or null-containing loaded lists are rejected. */
    @Test
    void loadTasksFromCsvStorageRecord_nullOrNullTaskList_throwsStorageException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.loadTasksFromCsvStorageRecord(null));
        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.loadTasksFromCsvStorageRecord(
                        java.util.Arrays.asList(new TodoTask("read book"), null)));
    }

    /** Verifies that searching with a null date is rejected. */
    @Test
    void searchTasks_nullDate_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.searchTasks(null, null));
    }

    /** Creates a task master with the default test capacity. */
    private TaskMaster createTaskMaster() {
        return createTaskMaster(100);
    }

    /** Creates a task master with a temporary backing file and capacity. */
    private TaskMaster createTaskMaster(int maxTasks) {
        return new TaskMaster(
                maxTasks,
                new CsvSaver(temporaryDirectory.resolve("tasks.csv")));
    }

    /** Creates a saver that fails every save attempt. */
    private static class FailingSaver extends CsvSaver {
        FailingSaver() {
            super(Path.of("unused.csv"));
        }

        /** Always throws to simulate a persistence failure. */
        @Override
        public void save(TaskMaster taskMaster) {
            throw new LuckyNoStorageException("simulated save failure");
        }
    }
}
