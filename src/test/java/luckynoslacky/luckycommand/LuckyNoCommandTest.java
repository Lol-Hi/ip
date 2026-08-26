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

    @Test
    void taskCommandExecutesTaskCreation() {
        TaskMaster taskMaster = createTaskMaster();
        TodoTask task = new TodoTask("read book");
        LuckyNoTaskCommand command = new LuckyNoTaskCommand(task, taskMaster);

        assertEquals(LuckyNoMessages.addedTaskMessage(task, 1), command.execute());
        assertEquals(1, taskMaster.getTaskCount());
        assertFalse(command.requestsExit());
    }

    @Test
    void markCommandExecutesDoneStatusChange() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(1, true, taskMaster);

        assertEquals(
                LuckyNoMessages.markedTaskMessage("[T][X] read book"),
                command.execute());
        assertEquals("Nah all these stuff you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    @Test
    void unmarkCommandExecutesNotDoneStatusChange() {
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

    @Test
    void deleteCommandExecutesTaskDeletion() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoDeleteCommand command = new LuckyNoDeleteCommand(1, taskMaster);

        assertEquals(
                LuckyNoMessages.deletedTaskMessage("[T][ ] read book", 0),
                command.execute());
        assertEquals(0, taskMaster.getTaskCount());
    }

    @Test
    void listCommandExecutesTaskListing() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoListCommand command = new LuckyNoListCommand(taskMaster);

        assertEquals(taskMaster.listTasks(), command.execute());
    }

    @Test
    void findCommandExecutesDateSearch() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        LuckyNoFindCommand command = new LuckyNoFindCommand(SEARCH_DATE, taskMaster);

        assertEquals(taskMaster.searchTasks(SEARCH_DATE), command.execute());
    }

    @Test
    void byeCommandReturnsGoodbyeAndRequestsExit() {
        LuckyNoByeCommand command = new LuckyNoByeCommand();

        assertEquals(LuckyNoMessages.goodbye(), command.execute());
        assertTrue(command.requestsExit());
    }

    private TaskMaster createTaskMaster() {
        return new TaskMaster(
                100,
                new CSVSaver(tempDir.resolve("tasks.csv")));
    }
}
