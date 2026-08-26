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
import luckynoslacky.luckystorage.CSVSaver;

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

    @Test
    void listTasks_emptyTaskList_returnsEmptyTaskListMessage() {
        TaskMaster taskMaster = createTaskMaster();

        assertEquals("Chill lah bro got nothing yet lah!", taskMaster.listTasks());
    }

    @Test
    void addTask_validTask_includesTaskInList() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));

        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void listTasks_multipleTasks_preservesOrder() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][ ] return book",
                taskMaster.listTasks());
    }

    @Test
    void listTasks_differentTaskTypes_formatsAllTypes() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("borrow book"));
        taskMaster.addTask(new DeadlineTask("return book", DEADLINE));
        taskMaster.addTask(new EventTask("project meeting", EVENT_START, EVENT_END));

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] borrow book\n"
                        + "2.[D][ ] return book (by: Sun Dec 06 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                taskMaster.listTasks());
    }

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

        assertEquals("Nah, all these stuff you need to do on: Aug 26 2026\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Tue Aug 25 2026, 2.00pm"
                        + " to: Thu Aug 27 2026, 4.00pm)",
                taskMaster.searchTasks(LocalDateTime.of(2026, 8, 26, 0, 0)));
    }

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

    @Test
    void markTaskDone_validTask_marksTaskAsDone() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));
        taskMaster.markTaskDone(2);

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][X] return book",
                taskMaster.listTasks());
    }

    @Test
    void unmarkTaskUndone_doneTask_marksTaskAsUndone() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.markTaskDone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void markTaskDone_alreadyDoneTask_remainsDone() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.markTaskDone(1);
        taskMaster.markTaskDone(1);

        assertEquals("Nah all these stuff you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    @Test
    void unmarkTaskUndone_alreadyUndoneTask_remainsUndone() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.unmarkTaskUndone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void deleteTask_middleTask_removesAndRenumbersTasks() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new DeadlineTask("second", DEADLINE));
        taskMaster.addTask(new EventTask("third", EVENT_START, EVENT_END));

        assertEquals("[D][ ] second (by: Sun Dec 06 2026, 11.59pm)",
                taskMaster.deleteTask(2));
        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] first\n"
                        + "2.[E][ ] third (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                taskMaster.listTasks());
    }

    @Test
    void deleteTask_invalidTaskNumber_preservesTaskList() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.deleteTask(2));
        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void deleteTask_fullList_freesCapacity() {
        TaskMaster taskMaster = createTaskMaster(2);
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        taskMaster.deleteTask(1);
        taskMaster.addTask(new TodoTask("third"));

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] second\n"
                        + "2.[T][ ] third",
                taskMaster.listTasks());
    }

    @Test
    void deleteTask_emptyList_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.deleteTask(1));
    }

    @Test
    void markTaskDone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
    }

    @Test
    void unmarkTaskUndone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
    }

    @Test
    void markOrUnmark_invalidTaskNumber_preservesTaskState() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());

        taskMaster.markTaskDone(1);
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
        assertEquals("Nah all these stuff you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    @Test
    void addTask_customCapacity_throwsWhenFull() {
        TaskMaster taskMaster = createTaskMaster(2);

        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        assertThrows(IllegalStateException.class,
                () -> taskMaster.addTask(new TodoTask("third")));
    }

    @Test
    void TaskMaster_nonPositiveCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(0));

        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(-1));
    }

    @Test
    void addTask_saveFailure_removesTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.addTask(new TodoTask("read book")));
        assertEquals(0, taskMaster.getTaskCount());
    }

    @Test
    void markTaskDone_saveFailure_restoresPreviousStatus() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCSVStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.markTaskDone(1));
        assertFalse(task.isDone());
    }

    @Test
    void deleteTask_saveFailure_restoresDeletedTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCSVStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.deleteTask(1));
        assertEquals(1, taskMaster.getTaskCount());
        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void loadTasksFromCSVStorageRecord_nullOrNullTaskList_throwsStorageException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.loadTasksFromCSVStorageRecord(null));
        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.loadTasksFromCSVStorageRecord(
                        java.util.Arrays.asList(new TodoTask("read book"), null)));
    }

    @Test
    void searchTasks_nullDate_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.searchTasks(null));
    }

    private TaskMaster createTaskMaster() {
        return createTaskMaster(100);
    }

    private TaskMaster createTaskMaster(int maxTasks) {
        return new TaskMaster(
                maxTasks,
                new CSVSaver(temporaryDirectory.resolve("tasks.csv")));
    }

    private static class FailingSaver extends CSVSaver {
        FailingSaver() {
            super(Path.of("unused.csv"));
        }

        @Override
        public void save(TaskMaster taskMaster) {
            throw new LuckyNoStorageException("simulated save failure");
        }
    }
}
