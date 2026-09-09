package luckynoslacky.luckycommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TodoTask;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests the execution behavior of the command classes used by LuckyNoSlacky.
 */
class LuckyNoCommandTest {
    private static final LocalDateTime SEARCH_DATE =
            LocalDateTime.of(2026, 8, 26, 0, 0);

    @TempDir
    Path tempDir;

    /** Verifies that task commands add tasks and return their reply. */
    @Test
    void execute_taskCommandWithTask_addsTaskAndReturnsReply() {
        TaskMaster taskMaster = createTaskMaster();
        TodoTask task = new TodoTask("read book");
        LuckyNoTaskCommand command = new LuckyNoTaskCommand(task, taskMaster);

        assertEquals(LuckyNoMessages.addedTaskMessage(task, 1), command.execute());
        assertEquals(1, taskMaster.getTaskCount());
        assertFalse(command.shouldExit());
    }

    /** Verifies that a mark command marks an incomplete task. */
    @Test
    void execute_markCommandWithUndoneTask_marksTaskDoneAndReturnsReply() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(1, true, taskMaster);

        assertEquals(
                LuckyNoMessages.markedTaskMessage("[T][X] read book"),
                command.execute());
        assertEquals("Nah, all these things you need to do:\n1.[T][X] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that an unmark command clears a task's done status. */
    @Test
    void execute_unmarkCommandWithDoneTask_marksTaskUndoneAndReturnsReply() {
        TaskMaster taskMaster = createTaskMaster();
        TodoTask task = new TodoTask("read book");
        task.markAsDone();
        taskMaster.addTask(task);
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(1, false, taskMaster);

        assertEquals(
                LuckyNoMessages.unmarkedTaskMessage("[T][ ] read book"),
                command.execute());
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that a delete command removes an existing task. */
    @Test
    void execute_deleteCommandWithExistingTask_deletesTaskAndReturnsReply() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoDeleteCommand command = new LuckyNoDeleteCommand(1, taskMaster);

        assertEquals(
                LuckyNoMessages.deletedTaskMessage("[T][ ] read book", 0),
                command.execute());
        assertEquals(0, taskMaster.getTaskCount());
    }

    /** Verifies that a list command returns the task-list response. */
    @Test
    void execute_listCommandWithExistingTasks_returnsTaskListMessage() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoListCommand command = new LuckyNoListCommand(taskMaster);

        assertEquals(
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()),
                command.execute());
    }

    /** Verifies that a find command returns matching dated tasks. */
    @Test
    void execute_findCommandWithMatchingDate_returnsMatchingTasks() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        LuckyNoFindCommand command = new LuckyNoFindCommand(SEARCH_DATE, taskMaster);

        assertEquals(
                LuckyNoMessages.listTasksMessage(taskMaster.findTasks(SEARCH_DATE)),
                command.execute());
    }

    /** Verifies that a snooze command extends a deadline and returns its reply. */
    @Test
    void execute_snoozeCommandWithDuration_updatesDeadlineAndReturnsReply() {
        TaskMaster taskMaster = createTaskMaster();
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));
        LuckyNoSnoozeCommand command = new LuckyNoSnoozeCommand(
                1, Duration.ofHours(2), taskMaster);

        String reply = command.execute();

        assertEquals(LuckyNoMessages.snoozedTaskMessage(task), reply);
        assertEquals(LocalDateTime.of(2026, 8, 26, 14, 0), task.getByTime());
    }

    /** Verifies that a reschedule command replaces both event times. */
    @Test
    void execute_reschedCommandWithEventTimes_updatesEventAndReturnsReply() {
        TaskMaster taskMaster = createTaskMaster();
        EventTask task = new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 12, 0),
                LocalDateTime.of(2026, 8, 26, 13, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));
        LocalDateTime newStart = LocalDateTime.of(2026, 8, 27, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 8, 27, 11, 0);
        LuckyNoReschedCommand command = new LuckyNoReschedCommand(
                1, newStart, newEnd, taskMaster);

        String reply = command.execute();

        assertEquals(LuckyNoMessages.rescheduledTaskMessage(task), reply);
        assertEquals(newStart, task.getStartTime());
        assertEquals(newEnd, task.getEndTime());
    }

    /** Verifies that a bye command returns goodbye and requests exit. */
    @Test
    void execute_byeCommandWithoutArguments_returnsGoodbyeAndRequestsExit() {
        LuckyNoByeCommand command = new LuckyNoByeCommand();

        assertEquals(LuckyNoMessages.goodbye(), command.execute());
        assertTrue(command.shouldExit());
    }

    /** Verifies that task commands reject a missing task master. */
    @Test
    void construct_taskCommandWithoutTaskMaster_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoTaskCommand(new TodoTask("read book"), null));
    }

    /** Verifies that mark commands reject a missing task master. */
    @Test
    void construct_markCommandWithoutTaskMaster_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoMarkCommand(1, true, null));
    }

    /** Verifies that delete commands reject a missing task master. */
    @Test
    void construct_deleteCommandWithoutTaskMaster_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoDeleteCommand(1, null));
    }

    /** Verifies that list commands reject a missing task master. */
    @Test
    void construct_listCommandWithoutTaskMaster_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoListCommand(null));
    }

    /** Verifies that find commands reject a missing task master. */
    @Test
    void construct_findCommandWithoutTaskMaster_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoFindCommand(SEARCH_DATE, null));
    }

    /** Creates a task master backed by a temporary CSV file. */
    private TaskMaster createTaskMaster() {
        return new TaskMaster(
                100,
                new CsvSaver(tempDir.resolve("tasks.csv")));
    }
}
