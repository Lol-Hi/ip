package luckynoslacky.luckycommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckyparser.DateTimeParser;
import luckynoslacky.luckystorage.CSVSaver;
import luckynoslacky.luckytask.*;
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
        assertFalse(command.requestsExit());
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
        assertEquals("Nah all these stuff you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
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
        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
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
    void execute_listCommandWithExistingTasks_returnsTaskList() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoListCommand command = new LuckyNoListCommand(taskMaster);

        assertEquals(taskMaster.listTasks(), command.execute());
    }

    /** Verifies that a find command returns matching dated tasks. */
    @Test
    void execute_findCommandWithMatchingDate_returnsMatchingTasks() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        LuckyNoFindCommand command = new LuckyNoFindCommand(SEARCH_DATE, taskMaster);

        assertEquals(taskMaster.searchTasks(SEARCH_DATE), command.execute());
    }

    /** Verifies that a bye command returns goodbye and requests exit. */
    @Test
    void execute_byeCommandWithoutArguments_returnsGoodbyeAndRequestsExit() {
        LuckyNoByeCommand command = new LuckyNoByeCommand();

        assertEquals(LuckyNoMessages.goodbye(), command.execute());
        assertTrue(command.requestsExit());
    }

    /** Creates a task master backed by a temporary CSV file. */
    private TaskMaster createTaskMaster() {
        return new TaskMaster(
                100,
                new CSVSaver(tempDir.resolve("tasks.csv")));
    }
}
