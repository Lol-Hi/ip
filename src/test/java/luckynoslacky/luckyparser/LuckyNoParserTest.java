package luckynoslacky.luckyparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckycommand.LuckyNoCommand;
import luckynoslacky.luckycommand.LuckyNoDeleteCommand;
import luckynoslacky.luckycommand.LuckyNoFindCommand;
import luckynoslacky.luckycommand.LuckyNoMarkCommand;
import luckynoslacky.luckycommand.LuckyNoReschedCommand;
import luckynoslacky.luckycommand.LuckyNoSnoozeCommand;
import luckynoslacky.luckycommand.LuckyNoTaskCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TodoTask;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests parsing and validation of user input.
 */
class LuckyNoParserTest {
    private static final Clock TEST_CLOCK = Clock.fixed(
            Instant.parse("2026-08-25T10:00:00Z"), ZoneId.of("UTC"));
    @TempDir
    Path tempDir;

    private DateTimeParser parser;
    private TaskMaster taskMaster;
    private LuckyNoParser scanner;

    /** Creates deterministic parser and task-list dependencies for each test. */
    @BeforeEach
    void setUp() {
        parser = new DateTimeParser(TEST_CLOCK);
        taskMaster = new TaskMaster(
                100,
                new CsvSaver(tempDir.resolve("tasks.csv")));
        scanner = new LuckyNoParser(parser, taskMaster);
    }

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

    /** Verifies relative date phrases use the injected fixed date. */
    @Test
    void parseCommand_relativeDatePhrases_usesFixedCurrentDate() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 15, 0, 0),
                parser.parseStartDateTime("the 15th").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 30, 23, 59),
                parser.parseEndDateTime("30th").dateTime());
        assertEquals(LocalDateTime.of(2027, 6, 6, 0, 0),
                parser.parseStartDateTime("June 6th").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 31, 0, 0),
                parser.parseStartDateTime("Monday").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 24, 0, 0),
                parser.parseStartDateTime("this Monday").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 30, 0, 0),
                parser.parseStartDateTime("this Sunday").dateTime());
        assertEquals(LocalDateTime.of(2026, 9, 2, 0, 0),
                parser.parseStartDateTime("next Wednesday").dateTime());
        assertEquals(LocalDateTime.of(2026, 9, 7, 0, 0),
                parser.parseStartDateTime("next next Monday").dateTime());
        assertEquals(LocalDateTime.of(2026, 9, 9, 0, 0),
                parser.parseStartDateTime("the following Wednesday").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 26, 0, 0),
                parser.parseStartDateTime("this coming Wednesday").dateTime());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0),
                parser.parseStartDateTime("the coming Tuesday").dateTime());
        assertEquals(LocalDateTime.of(2026, 9, 15, 0, 0),
                parser.parseStartDateTime("next 15th").dateTime());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
                parser.parseStartDateTime("next next 15th").dateTime());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
                parser.parseStartDateTime("the following 15th").dateTime());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0),
                parser.parseStartDateTime("next month").dateTime());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0),
                parser.parseStartDateTime("next next month").dateTime());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0),
                parser.parseStartDateTime("the following month").dateTime());
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
                parser.parseStartDateTime("next year").dateTime());
        assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
                parser.parseStartDateTime("the following year").dateTime());
        assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
                parser.parseStartDateTime("next next year").dateTime());
    }

    /** Verifies time-only task arguments resolve to today or tomorrow. */
    @Test
    void parseCommand_timeOnlyValues_usesTodayOrTomorrow() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 14, 0),
                parser.parseStartDateTime("2pm").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 26, 9, 0),
                parser.parseStartDateTime("9am").dateTime());
    }

    /** Verifies all documented date/time formats are accepted. */
    @Test
    void parseCommand_documentedDateTimeFormats_returnsExpectedValues() throws Exception {
        LocalDate expectedDate = LocalDate.of(2030, 10, 15);
        for (String dateTimeText : new String[] {
            "2030-10-15", "2030/10/15", "15/10/2030", "15-10-2030",
            "15 Oct 2030", "15 October 2030", "Oct 15 2030",
            "October 15 2030", "Tue Oct 15 2030",
            "Tuesday, October 15 2030"}) {
            assertEquals(expectedDate.atStartOfDay(),
                parser.parseStartDateTime(dateTimeText).dateTime(), dateTimeText);
        }

        LocalDateTime expectedTime = LocalDateTime.of(2026, 8, 25, 14, 15, 30);
        for (String dateTimeText : new String[] {
            "14:15", "14:15:30", "2pm", "2 pm", "2:15pm",
            "2:15 pm", "2.15pm", "2.15 pm"}) {
            LocalDateTime expected = dateTimeText.contains("30")
                    ? expectedTime
                    : dateTimeText.contains(":15") || dateTimeText.contains(".15")
                    ? expectedTime.withSecond(0)
                    : expectedTime.withMinute(0).withSecond(0);
            assertEquals(expected,
                    parser.parseStartDateTime(dateTimeText).dateTime(), dateTimeText);
        }
    }

    /** Verifies date-only task arguments use start and end defaults. */
    @Test
    void parseCommand_dateOnlyValues_useStartAndEndDefaults() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseStartDateTime("2026-08-25").dateTime());
        assertEquals(LocalDateTime.of(2026, 8, 25, 23, 59),
                parser.parseEndDateTime("2026-08-25").dateTime());
    }

    /** Verifies invalid and past date/time arguments are rejected. */
    @Test
    void parseCommand_invalidOrPastDateTimes_throwsInputException() {
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "deadline report /by definitely-not-a-date", 0);
        assertInputError(LuckyNoMessages.timeTravelMessage(),
                "deadline report /by 2026-08-25 09:00", 0);
        assertInputError(LuckyNoMessages.timeTravelMessage(),
                "deadline report /by 25 Aug 2025", 0);
        assertInputError(LuckyNoMessages.timeTravelMessage(),
                "event meeting /from 2026-08-06 16:00 /to 2026-08-06 14:00", 0);
    }

    /** Verifies past event starts are allowed when the end is later. */
    @Test
    void parseCommand_pastEventStartWithFutureEnd_succeeds() throws Exception {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event past meeting /from 25 Aug 2025"
                        + " /to 26 Aug 2025", 0));

        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new EventTask(
                                "past meeting",
                                LocalDateTime.of(2025, 8, 25, 0, 0),
                                LocalDateTime.of(2025, 8, 26, 23, 59)),
                        1),
                command.execute());
    }

    /** Verifies event end times use the start date or roll to the next date. */
    @Test
    void parseCommand_timeOnlyEventEnd_usesStartDateOrNextDate() throws Exception {
        LuckyNoTaskCommand sameDay = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event afternoon meeting /from 25 Aug 2026 2pm"
                        + " /to 4pm", 0));
        LuckyNoTaskCommand overnight = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event overnight meeting /from 25 Aug 2026 11pm"
                        + " /to 1am", 0));

        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new EventTask(
                                "afternoon meeting",
                                LocalDateTime.of(2026, 8, 25, 14, 0),
                                LocalDateTime.of(2026, 8, 25, 16, 0)),
                        1),
                sameDay.execute());
        assertEquals(
                LuckyNoMessages.addedTaskMessage(
                        new EventTask(
                                "overnight meeting",
                                LocalDateTime.of(2026, 8, 25, 23, 0),
                                LocalDateTime.of(2026, 8, 26, 1, 0)),
                        2),
                overnight.execute());
    }

    /** Verifies that the default snooze extends a deadline by one hour. */
    @Test
    void parseCommand_defaultSnooze_extendsDeadlineByOneHour() throws Exception {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoSnoozeCommand command = assertInstanceOf(LuckyNoSnoozeCommand.class,
                scanner.parseCommand("snooze 1", 1));

        command.execute();

        assertEquals(LocalDateTime.of(2026, 8, 26, 13, 0), task.getByTime());
    }

    /** Verifies that a duration snooze extends only an event's end time. */
    @Test
    void parseCommand_durationSnooze_updatesOnlyEventEnd() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 26, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 26, 13, 0);
        EventTask task = new EventTask("project meeting", start, end);
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoSnoozeCommand command = assertInstanceOf(LuckyNoSnoozeCommand.class,
                scanner.parseCommand("snooze 1 /by 2 hours please", 1));

        command.execute();

        assertEquals(start, task.getStartTime());
        assertEquals(end.plusHours(2), task.getEndTime());
    }

    /** Verifies that an explicit snooze time replaces a deadline. */
    @Test
    void parseCommand_explicitSnoozeTime_replacesDeadline() throws Exception {
        DeadlineTask task = new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoSnoozeCommand command = assertInstanceOf(LuckyNoSnoozeCommand.class,
                scanner.parseCommand("snooze 1 /to 27 Aug 2026 5pm", 1));

        command.execute();

        assertEquals(LocalDateTime.of(2026, 8, 27, 17, 0), task.getByTime());
    }

    /** Verifies that an event reschedule uses the new start as the end reference. */
    @Test
    void parseCommand_eventRescheduleOvernight_usesNextEndDate() throws Exception {
        EventTask task = new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 12, 0),
                LocalDateTime.of(2026, 8, 26, 13, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoReschedCommand command = assertInstanceOf(LuckyNoReschedCommand.class,
                scanner.parseCommand(
                        "resched 1 /from 26 Aug 2026 11pm /to 1am", 1));

        command.execute();

        assertEquals(LocalDateTime.of(2026, 8, 26, 23, 0), task.getStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 27, 1, 0), task.getEndTime());
    }

    /** Verifies that partial event start rescheduling preserves the end. */
    @Test
    void parseCommand_eventFromOnly_preservesExistingEnd() throws Exception {
        LocalDateTime existingEnd = LocalDateTime.of(2026, 8, 26, 16, 0);
        EventTask task = new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                existingEnd);
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoReschedCommand command = assertInstanceOf(LuckyNoReschedCommand.class,
                scanner.parseCommand("resched 1 /from 26 Aug 2026 3pm", 1));

        command.execute();

        assertEquals(LocalDateTime.of(2026, 8, 26, 15, 0), task.getStartTime());
        assertEquals(existingEnd, task.getEndTime());
    }

    /** Verifies that partial event end rescheduling preserves the start. */
    @Test
    void parseCommand_eventToOnly_preservesExistingStart() throws Exception {
        LocalDateTime existingStart = LocalDateTime.of(2026, 8, 26, 14, 0);
        EventTask task = new EventTask(
                "project meeting",
                existingStart,
                LocalDateTime.of(2026, 8, 26, 16, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoReschedCommand command = assertInstanceOf(LuckyNoReschedCommand.class,
                scanner.parseCommand("resched 1 /to 5pm", 1));

        command.execute();

        assertEquals(existingStart, task.getStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 26, 17, 0), task.getEndTime());
    }

    /** Verifies reversed markers use the new start as the end reference. */
    @Test
    void parseCommand_reversedEventMarkers_useNewStartReference() throws Exception {
        EventTask task = new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0));
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(task));

        LuckyNoReschedCommand command = assertInstanceOf(LuckyNoReschedCommand.class,
                scanner.parseCommand(
                        "resched 1 /to 1am /from 26 Aug 2026 11pm", 1));

        command.execute();

        assertEquals(LocalDateTime.of(2026, 8, 26, 23, 0), task.getStartTime());
        assertEquals(LocalDateTime.of(2026, 8, 27, 1, 0), task.getEndTime());
    }

    /** Verifies duplicate and unknown event markers use the event format. */
    @Test
    void parseCommand_invalidEventMarkers_throwsEventFormat() {
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(
                new EventTask(
                        "project meeting",
                        LocalDateTime.of(2026, 8, 26, 14, 0),
                        LocalDateTime.of(2026, 8, 26, 16, 0))));
        String expectedMessage = LuckyNoMessages.invalidFormatMessage(
                LuckyNoParser.CommandName.RESCHED,
                LuckyNoMessages.reschedEventFormat());

        assertInputError(
                expectedMessage,
                "resched 1 /from 3pm /from 4pm", 1);
        assertInputError(
                expectedMessage,
                "resched 1 /when 5pm", 1);
        assertInputError(
                expectedMessage,
                "resched 1 /to 5pm /to 6pm", 1);
        assertInputError(
                expectedMessage,
                "resched 1 /from 3pm /to 5pm /extra", 1);
    }

    /** Verifies that ToDos receive dedicated snooze and reschedule errors. */
    @Test
    void parseCommand_timedCommandOnTodo_throwsDedicatedInputErrors() {
        taskMaster.loadTasksFromCsvStorageRecord(
                java.util.List.of(new TodoTask("read book")));

        assertInputError(
                LuckyNoMessages.cannotSnoozeOrRescheduleTodoMessage(
                        LuckyNoParser.CommandName.SNOOZE),
                "snooze 1", 1);
        assertInputError(
                LuckyNoMessages.cannotSnoozeOrRescheduleTodoMessage(
                        LuckyNoParser.CommandName.RESCHED),
                "resched 1 /to tomorrow", 1);
    }

    /** Verifies negative durations and malformed slash syntax are rejected. */
    @Test
    void parseCommand_invalidSnoozeArguments_throwsInputException() {
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(
                new DeadlineTask("return book", LocalDateTime.of(2026, 8, 26, 12, 0))));

        assertInputError(
                "Siao ah time where got negative one",
                "snooze 1 /by -2 hours", 1);
        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.SNOOZE,
                        LuckyNoMessages.snoozeByFormat(),
                        LuckyNoMessages.snoozeToFormat()),
                "snooze 1 /by 2 hours /to tomorrow", 1);
    }

    /** Verifies invalid rescheduling formats depend on the selected task type. */
    @Test
    void parseCommand_rescheduleMissingFormat_usesTaskTypeFormat() {
        taskMaster.loadTasksFromCsvStorageRecord(java.util.List.of(
                new DeadlineTask("return book", LocalDateTime.of(2026, 8, 26, 12, 0)),
                new EventTask(
                        "project meeting",
                        LocalDateTime.of(2026, 8, 26, 12, 0),
                        LocalDateTime.of(2026, 8, 26, 13, 0))));

        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.RESCHED,
                        LuckyNoMessages.reschedDeadlineFormat()),
                "resched 1 /from tomorrow /to tomorrow", 2);
        assertInputError(
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.RESCHED,
                        LuckyNoMessages.reschedEventFormat()),
                "resched 2", 2);
    }

    /** Verifies invalid, zero, and out-of-range task numbers are rejected. */
    @Test
    void parseCommand_invalidTaskNumbers_throwsInputException() {
        assertInputError("Eh which task you talking about har? Can say clearly anot.", "mark", 1);
        assertInputError("You siao ah how to spin this task from thin air?", "mark 2", 1);
        assertInputError("Eh which task you talking about har? Can say clearly anot.",
                "unmark 1 extra", 1);
        assertInputError("You siao ah how to spin this task from thin air?", "unmark -1", 1);
        assertInputError("You siao ah how to spin this task from thin air?", "mark one", 1);
        assertInputError("Eh which task you talking about har? Can say clearly anot.",
                "delete", 1);
        assertInputError("You siao ah how to spin this task from thin air?",
                "delete 2", 1);
        assertInputError("Eh which task you talking about har? Can say clearly anot.",
                "delete 1 extra", 1);
    }

    /** Verifies list and bye reject extra arguments. */
    @Test
    void parseCommand_extraArgumentsForListAndBye_throwsInputException() {
        assertInputError("Why you so losor! Leave the list command to do its own thing lah", "list now", 0);
        assertInputError("Why you so losor! Leave the bye command to do its own thing lah", "bye now", 0);
    }

    /** Asserts that parsing an input returns the expected user-facing error. */
    private void assertInputError(String message, String userInput, int taskCount) {
        LuckyNoInputException error = assertThrows(
                LuckyNoInputException.class, () -> scanner.parseCommand(userInput, taskCount));
        assertEquals(message, error.getMessage());
    }
}
