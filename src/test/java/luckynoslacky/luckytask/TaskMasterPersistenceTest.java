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
import luckynoslacky.luckyexception.LuckyNoTaskLimitException;
import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests persistence, capacity, and validation behavior provided by {@link TaskMaster}. */
class TaskMasterPersistenceTest {
    private static final LocalDateTime DEADLINE =
            LocalDateTime.of(2026, 12, 6, 23, 59);

    @TempDir
    Path temporaryDirectory;

    /** Verifies that task-type lookup exposes only the requested category. */
    @Test
    void getTaskType_existingTask_returnsTaskCategory() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.loadTasksFromCsvStorageRecord(List.of(
                new TodoTask("read book"), new DeadlineTask("return book", DEADLINE),
                new EventTask("project meeting",
                        LocalDateTime.of(2026, 8, 6, 14, 0),
                        LocalDateTime.of(2026, 8, 6, 16, 0))));

        assertEquals(Task.TaskType.TODO, taskMaster.getTaskType(1));
        assertEquals(Task.TaskType.DEADLINE, taskMaster.getTaskType(2));
        assertEquals(Task.TaskType.EVENT, taskMaster.getTaskType(3));
    }

    /** Verifies that the configured task capacity is enforced. */
    @Test
    void addTask_customCapacity_throwsWhenFull() {
        TaskMaster taskMaster = createTaskMaster(2);

        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        LuckyNoTaskLimitException exception = assertThrows(
                LuckyNoTaskLimitException.class, () ->
                taskMaster.addTask(new TodoTask("third")));
        assertEquals("The task list is full.", exception.getMessage());
        assertEquals(2, taskMaster.getTaskCount());
    }

    /** Verifies that non-positive capacities are rejected. */
    @Test
    void taskMaster_nonPositiveCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new TaskMaster(0));
        assertThrows(IllegalArgumentException.class, () -> new TaskMaster(-1));
    }

    /** Verifies that a failed save rolls back an added task. */
    @Test
    void addTask_saveFailure_removesTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.addTask(new TodoTask("read book")));
        assertEquals(0, taskMaster.getTaskCount());
    }

    /** Verifies that a failed save rolls back a status change. */
    @Test
    void markTaskDone_saveFailure_restoresPreviousStatus() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class, () -> taskMaster.markTaskDone(1));
        assertFalse(task.isDone());
    }

    /** Verifies that a failed save restores a deleted task. */
    @Test
    void deleteTask_saveFailure_restoresDeletedTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class, () -> taskMaster.deleteTask(1));
        assertEquals(1, taskMaster.getTaskCount());
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that null or null-containing loaded lists are rejected. */
    @Test
    void loadTasksFromCsvStorageRecord_nullOrNullTaskList_throwsStorageException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.loadTasksFromCsvStorageRecord(null));
        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.loadTasksFromCsvStorageRecord(
                        java.util.Arrays.asList(new TodoTask("read book"), null)));
    }

    /** Verifies that searching with a null date and description is rejected. */
    @Test
    void findTasks_nullDate_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class, () -> taskMaster.findTasks(null, null));
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

    /** A saver that fails every save attempt for rollback tests. */
    private static class FailingSaver extends CsvSaver {
        FailingSaver() {
            super(Path.of("unused.csv"));
        }

        /** Always throws to simulate a persistence failure. */
        @Override
        public void save(TaskList taskList) {
            throw new LuckyNoStorageException("simulated save failure");
        }
    }
}
