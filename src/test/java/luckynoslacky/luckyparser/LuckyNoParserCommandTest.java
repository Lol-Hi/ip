package luckynoslacky.luckyparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckycommand.LuckyNoCommand;
import luckynoslacky.luckycommand.LuckyNoDeleteCommand;
import luckynoslacky.luckycommand.LuckyNoFindCommand;
import luckynoslacky.luckycommand.LuckyNoMarkCommand;
import luckynoslacky.luckycommand.LuckyNoTaskCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.TodoTask;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests parsing and validation of basic command input. */
class LuckyNoParserCommandTest extends LuckyNoParserTestSupport {
    /** Verifies parsing a ToDo command. */
    @Test
    void parseCommand_todoInput_returnsTaskCommand() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("todo borrow book", 0));

        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new TodoTask("borrow book"), 1),
                command.execute());
    }

    /** Verifies parsing a deadline command. */
    @Test
    void parseCommand_deadlineInput_returnsDeadlineTaskCommand() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("deadline return book /by 2026-10-15 14:15", 0));

        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new DeadlineTask(
                                "return book",
                                LocalDateTime.of(2026, 10, 15, 14, 15)),
                        1),
                command.execute());
    }

    /** Verifies parsing an event command. */
    @Test
    void parseCommand_eventInput_returnsEventTaskCommand() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event project meeting /from 2026-08-06 14:00"
                        + " /to 2026-08-06 16:00", 0));

        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new EventTask(
                                "project meeting",
                                LocalDateTime.of(2026, 8, 6, 14, 0),
                                LocalDateTime.of(2026, 8, 6, 16, 0)),
                        1),
                command.execute());
    }

    /** Verifies that mark and unmark commands request explicit statuses. */
    @Test
    void parseCommand_markAndUnmarkInput_returnsExplicitStatuses() throws LuckyNoInputException {
        taskMaster.addTask(new TodoTask("read book"));
        LuckyNoMarkCommand mark = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("mark 1", 1));
        LuckyNoMarkCommand unmark = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("unmark 1", 1));

        assertEquals(
                LuckyNoMessages.markedTaskMessage("[T][X] read book"),
                mark.execute());
        assertEquals(
                LuckyNoMessages.unmarkedTaskMessage("[T][ ] read book"),
                unmark.execute());
    }

    /** Verifies parsing with an omitted explicit task count. */
    @Test
    void parseCommand_taskMasterCountOmitted_usesCurrentCount()
            throws LuckyNoInputException {
        taskMaster.addTask(new TodoTask("read book"));

        LuckyNoMarkCommand command = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("mark 1"));

        assertEquals(
                LuckyNoMessages.markedTaskMessage("[T][X] read book"),
                command.execute());
    }

    /** Verifies parsing a delete command. */
    @Test
    void parseCommand_deleteInput_returnsDeleteCommand() throws LuckyNoInputException {
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));
        taskMaster.addTask(new TodoTask("buy bread"));
        LuckyNoDeleteCommand command = assertInstanceOf(LuckyNoDeleteCommand.class,
                scanner.parseCommand("delete 3", 3));

        assertEquals(
                LuckyNoMessages.deletedTaskMessage("[T][ ] buy bread", 2),
                command.execute());
        assertEquals(2, taskMaster.getTaskCount());
    }

    /** Verifies parsing list and bye commands. */
    @Test
    void parseCommand_listAndByeInput_returnsCommands() throws LuckyNoInputException {
        assertEquals(
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()),
                scanner.parseCommand("list", 0).execute());
        LuckyNoCommand bye = scanner.parseCommand("bye", 0);
        assertEquals(LuckyNoMessages.goodbye(), bye.execute());
        assertTrue(bye.shouldExit());
    }

    /** Verifies that text before the find tag is ignored. */
    @Test
    void parseCommand_findWithDescriptionAndDate_returnsMatchingTasks()
            throws LuckyNoInputException {
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 9, 2, 23, 59)));
        LuckyNoFindCommand command = assertInstanceOf(LuckyNoFindCommand.class,
                scanner.parseCommand("find book /on next Wednesday", 0));

        assertEquals(
                LuckyNoMessages.listTasksMessage(taskMaster.findTasks(
                        "book", LocalDateTime.of(2026, 9, 2, 0, 0))),
                command.execute());
    }

    /** Verifies find commands that contain only a description. */
    @Test
    void parseCommand_findWithDescriptionOnly_returnsMatchingTasks()
            throws LuckyNoInputException {
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("buy bread"));

        LuckyNoFindCommand command = assertInstanceOf(LuckyNoFindCommand.class,
                scanner.parseCommand("find book", 0));

        assertEquals(
                LuckyNoMessages.listTasksMessage(taskMaster.findTasks("book")),
                command.execute());
    }

    /** Verifies known command tokens match case-insensitively. */
    @Test
    void fromCommandToken_knownAndUnknownTokens_returnsMatchOrEmptyOptional() {
        assertEquals(LuckyNoParser.CommandName.DELETE,
                LuckyNoParser.CommandName.fromCommandToken("DeLeTe").orElseThrow());
        assertTrue(LuckyNoParser.CommandName.fromCommandToken("dance").isEmpty());
    }

    /** Verifies command parsing tolerates case and surrounding whitespace. */
    @Test
    void parseCommand_caseInsensitiveAndWhitespace_returnsTaskCommand()
            throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("  ToDo   read book  ", 0));

        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new TodoTask("read book"), 1),
                command.execute());
    }

    /** Verifies timed commands accept trailing commentary and flexible spacing. */
    @Test
    void parseCommand_timedInputWithTrailingCommentary_returnsCommands()
            throws LuckyNoInputException {
        assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand(
                        "  deadline   report /by 26 Aug 2026 12pm please  ", 0));
        assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand(
                        "event   meeting /from 26 Aug 2026 2pm /to 3pm please", 0));
        assertInstanceOf(LuckyNoFindCommand.class,
                scanner.parseCommand("find report /on 26 Aug 2026 later", 0));
    }

    /** Verifies empty and unknown commands are rejected. */
    @Test
    void parseCommand_emptyOrUnknownInput_throwsInputException() {
        assertInputError("Eh you mute issit?? Just say what you want lah!", "   ", 0);
        assertInputError(
                LuckyNoMessages.unknownCommandMessage(),
                "dance", 0);
    }

    /** Verifies malformed ToDo, deadline, and event commands are rejected. */
    @Test
    void parseCommand_malformedTodoDeadlineOrEvent_throwsInputException() {
        assertInputError("You don't tell me what to do how I know what to do???", "todo", 0);
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.DEADLINE),
                "deadline return book", 0);
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.EVENT),
                "event meeting /from 2pm", 0);
    }

    /** Verifies ordinary slashes remain valid in task text and date values. */
    @Test
    void parseCommand_nonMarkerSlashes_remainsValid() throws LuckyNoInputException {
        assertEquals(
                LuckyNoMessages.addedTaskMessage(new TodoTask("read/book"), 1),
                scanner.parseCommand("todo read/book", 0).execute());
        assertEquals(
                LuckyNoMessages.addedTaskMessage(new TodoTask("read / book"), 2),
                scanner.parseCommand("todo read / book", 1).execute());
        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new DeadlineTask(
                                "slash date", LocalDateTime.of(2026, 8, 26, 23, 59)),
                        3),
                scanner.parseCommand(
                        "deadline slash date /by 2026/08/26", 2).execute());
    }

    /** Verifies unsupported marker-like slashes use command format errors. */
    @Test
    void parseCommand_markerLikeSlashes_throwsCommandFormatErrors() {
        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.TODO),
                "todo read /book", 0);
        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.DEADLINE),
                "deadline report /by 2026/08/26 /extra", 0);
        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND),
                "find book /extra", 0);
        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.EVENT),
                "event meeting /from 2026/08/26 /to 2026/08/27 /extra", 0);
    }

    /** Verifies missing deadline and event time sections are rejected. */
    @Test
    void parseCommand_missingDeadlineOrEventTime_throwsInputException() {
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.DEADLINE),
                "deadline return book /by", 0);
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.EVENT),
                "event meeting /from Mon 2pm /to", 0);
    }

    /** Verifies empty and reversed task sections are rejected. */
    @Test
    void parseCommand_emptyOrReversedTaskSections_throwsInputException() {
        String deadlineFormat = LuckyNoMessages.invalidFormatMessage(
                LuckyNoParser.CommandName.DEADLINE);
        String eventFormat = LuckyNoMessages.invalidFormatMessage(
                LuckyNoParser.CommandName.EVENT);

        assertInputError(deadlineFormat, "deadline /by 2pm", 0);
        assertInputError(eventFormat, "event /from 1pm /to 2pm", 0);
        assertInputError(eventFormat, "event meeting /to 2pm /from 1pm", 0);
    }

    /** Verifies malformed find commands are rejected. */
    @Test
    void parseCommand_malformedFindInput_throwsInputException() {
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND),
                "find", 0);
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND),
                "find /on", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find ignored /on definitely-not-a-date", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find /on 32 Aug 2026", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find /on 2026-13-01", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find /on 25:99", 0);
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND),
                "find book /on", 0);
        assertInputError(LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND),
                "find book /on tomorrow /on Friday", 0);
    }
}
