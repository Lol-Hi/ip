import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

/**
 * Tests parsing and validation of user input.
 */
class LuckyNoScannerTest {
    private static final Clock TEST_CLOCK = Clock.fixed(
            Instant.parse("2026-08-25T10:00:00Z"), ZoneId.of("UTC"));
    private final DateTimeParser parser =
            new DateTimeParser(TEST_CLOCK);
    private final LuckyNoScanner scanner = new LuckyNoScanner(parser);

    @Test
    void parsesTodoCommandIntoTaskCommand() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("todo borrow book", 0));

        assertEquals("[T][ ] borrow book", command.getTask().toString());
    }

    @Test
    void parsesDeadlineCommandIntoDeadlineTask() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("deadline return book /by 2026-10-15 14:15", 0));

        assertEquals("[D][ ] return book (by: Thu Oct 15 2026, 2.15pm)",
                command.getTask().toString());
    }

    @Test
    void parsesEventCommandIntoEventTask() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event project meeting /from 2026-08-06 14:00"
                        + " /to 2026-08-06 16:00", 0));

        assertEquals("[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                command.getTask().toString());
    }

    @Test
    void parsesMarkAndUnmarkAsExplicitStatuses() throws LuckyNoInputException {
        LuckyNoMarkCommand mark = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("mark 1", 1));
        LuckyNoMarkCommand unmark = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("unmark 1", 1));

        assertEquals(LuckyNoCommand.CommandType.TOGGLE_TASK, mark.getCommandType());
        assertEquals(true, mark.shouldMarkDone());
        assertEquals(LuckyNoCommand.CommandType.TOGGLE_TASK, unmark.getCommandType());
        assertEquals(false, unmark.shouldMarkDone());
    }

    @Test
    void parsesDeleteCommandIntoDeleteCommand() throws LuckyNoInputException {
        LuckyNoDeleteCommand command = assertInstanceOf(LuckyNoDeleteCommand.class,
                scanner.parseCommand("delete 3", 3));

        assertEquals(LuckyNoCommand.CommandType.DELETE_TASK, command.getCommandType());
        assertEquals(3, command.getTaskNumber());
    }

    @Test
    void parsesListAndByeCommands() throws LuckyNoInputException {
        assertEquals(LuckyNoCommand.CommandType.LIST,
                scanner.parseCommand("list", 0).getCommandType());
        assertEquals(LuckyNoCommand.CommandType.BYE,
                scanner.parseCommand("bye", 0).getCommandType());
    }

    @Test
    void parsesFindCommandAndIgnoresTextBeforeOnTag()
            throws LuckyNoInputException {
        LuckyNoFindCommand command = assertInstanceOf(LuckyNoFindCommand.class,
                scanner.parseCommand("find anything /on next Wednesday", 0));

        assertEquals(LuckyNoCommand.CommandType.FIND, command.getCommandType());
        assertEquals(LocalDateTime.of(2026, 9, 2, 0, 0),
                command.getSearchDateTime());
    }

    @Test
    void commandNameLookupReturnsKnownCommandOrEmptyOptional() {
        assertEquals(LuckyNoScanner.CommandName.DELETE,
                LuckyNoScanner.CommandName.fromInput("DeLeTe").orElseThrow());
        assertTrue(LuckyNoScanner.CommandName.fromInput("dance").isEmpty());
    }

    @Test
    void acceptsCaseInsensitiveCommandsAndSurroundingWhitespace()
            throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("  ToDo   read book  ", 0));

        assertEquals("[T][ ] read book", command.getTask().toString());
    }

    @Test
    void rejectsEmptyAndUnknownCommands() {
        assertInputError("Eh you mute issit?? Just say what you want lah!", "   ", 0);
        assertInputError("What talking you? I only understand todo, deadline, event, list, mark, unmark, delete, find, or bye, ok?",
                "dance", 0);
    }

    @Test
    void rejectsMalformedTaskCommands() {
        assertInputError("You don't tell me what to do how I know what to do???", "todo", 0);
        assertInputError("Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: deadline <description> /by <date/time>.",
                "deadline return book", 0);
        assertInputError("Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: event <description> /from <start> /to <end>.",
                "event meeting /from 2pm", 0);
    }

    @Test
    void rejectsMissingDeadlineAndEventTimes() {
        assertInputError("Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: deadline <description> /by <date/time>.",
                "deadline return book /by", 0);
        assertInputError("Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: event <description> /from <start> /to <end>.",
                "event meeting /from Mon 2pm /to", 0);
    }

    @Test
    void rejectsMalformedFindCommands() {
        assertInputError("Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: find /on <date>",
                "find next Wednesday", 0);
        assertInputError("Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: find /on <date>",
                "find /on", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find ignored /on definitely-not-a-date", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find /on 32 Aug 2026", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find /on 2026-13-01", 0);
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "find /on 25:99", 0);
    }

    @Test
    void resolvesFlexibleDatePhrasesAgainstTheCurrentDate() throws Exception {
        assertEquals(LocalDateTime.of(2026, 9, 15, 0, 0),
                parser.parseStartDateTime("the 15th").value());
        assertEquals(LocalDateTime.of(2026, 8, 30, 23, 59),
                parser.parseEndDateTime("30th").value());
        assertEquals(LocalDateTime.of(2027, 6, 6, 0, 0),
                parser.parseStartDateTime("June 6th").value());
        assertEquals(LocalDateTime.of(2026, 8, 31, 0, 0),
                parser.parseStartDateTime("Monday").value());
        assertEquals(LocalDateTime.of(2026, 8, 24, 0, 0),
                parser.parseStartDateTime("this Monday").value());
        assertEquals(LocalDateTime.of(2026, 8, 30, 0, 0),
                parser.parseStartDateTime("this Sunday").value());
        assertEquals(LocalDateTime.of(2026, 9, 2, 0, 0),
                parser.parseStartDateTime("next Wednesday").value());
        assertEquals(LocalDateTime.of(2026, 9, 7, 0, 0),
                parser.parseStartDateTime("next next Monday").value());
        assertEquals(LocalDateTime.of(2026, 9, 9, 0, 0),
                parser.parseStartDateTime("the following Wednesday").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 0, 0),
                parser.parseStartDateTime("this coming Wednesday").value());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0),
                parser.parseStartDateTime("the coming Tuesday").value());
        assertEquals(LocalDateTime.of(2026, 9, 15, 0, 0),
                parser.parseStartDateTime("next 15th").value());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
                parser.parseStartDateTime("next next 15th").value());
        assertEquals(LocalDateTime.of(2026, 10, 15, 0, 0),
                parser.parseStartDateTime("the following 15th").value());
        assertEquals(LocalDateTime.of(2026, 9, 1, 0, 0),
                parser.parseStartDateTime("next month").value());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0),
                parser.parseStartDateTime("next next month").value());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0),
                parser.parseStartDateTime("the following month").value());
        assertEquals(LocalDateTime.of(2027, 1, 1, 0, 0),
                parser.parseStartDateTime("next year").value());
        assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
                parser.parseStartDateTime("the following year").value());
        assertEquals(LocalDateTime.of(2028, 1, 1, 0, 0),
                parser.parseStartDateTime("next next year").value());
    }

    @Test
    void resolvesTimeOnlyValuesToTodayOrTomorrow() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 14, 0),
                parser.parseStartDateTime("2pm").value());
        assertEquals(LocalDateTime.of(2026, 8, 26, 9, 0),
                parser.parseStartDateTime("9am").value());
    }

    @Test
    void acceptsDocumentedDateAndTimeFormats() throws Exception {
        LocalDate expectedDate = LocalDate.of(2030, 10, 15);
        for (String input : new String[] {
                "2030-10-15", "2030/10/15", "15/10/2030", "15-10-2030",
                "15 Oct 2030", "15 October 2030", "Oct 15 2030",
                "October 15 2030", "Tue Oct 15 2030",
                "Tuesday, October 15 2030"}) {
            assertEquals(expectedDate.atStartOfDay(),
                    parser.parseStartDateTime(input).value(), input);
        }

        LocalDateTime expectedTime = LocalDateTime.of(2026, 8, 25, 14, 15, 30);
        for (String input : new String[] {
                "14:15", "14:15:30", "2pm", "2 pm", "2:15pm",
                "2:15 pm", "2.15pm", "2.15 pm"}) {
            LocalDateTime expected = input.contains("30")
                    ? expectedTime
                    : input.contains(":15") || input.contains(".15")
                    ? expectedTime.withSecond(0)
                    : expectedTime.withMinute(0).withSecond(0);
            assertEquals(expected, parser.parseStartDateTime(input).value(), input);
        }
    }

    @Test
    void usesStartAndEndDefaultsForDateOnlyValues() throws Exception {
        assertEquals(LocalDateTime.of(2026, 8, 25, 0, 0),
                parser.parseStartDateTime("2026-08-25").value());
        assertEquals(LocalDateTime.of(2026, 8, 25, 23, 59),
                parser.parseEndDateTime("2026-08-25").value());
    }

    @Test
    void rejectsInvalidAndImpossibleDateTimes() {
        assertInputError(LuckyNoMessages.invalidDateTimeMessage(),
                "deadline report /by definitely-not-a-date", 0);
        assertInputError(LuckyNoMessages.timeTravelMessage(),
                "deadline report /by 2026-08-25 09:00", 0);
        assertInputError(LuckyNoMessages.timeTravelMessage(),
                "deadline report /by 25 Aug 2025", 0);
        assertInputError(LuckyNoMessages.timeTravelMessage(),
                "event meeting /from 2026-08-06 16:00 /to 2026-08-06 14:00", 0);
    }

    @Test
    void allowsPastEventStartWhenEventEndIsAfterIt() throws Exception {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event past meeting /from 25 Aug 2025"
                        + " /to 26 Aug 2025", 0));

        assertEquals("[E][ ] past meeting (from: Mon Aug 25 2025, 12.00am"
                        + " to: Tue Aug 26 2025, 11.59pm)",
                command.getTask().toString());
    }

    @Test
    void resolvesEventEndTimeRelativeToEventStart() throws Exception {
        LuckyNoTaskCommand sameDay = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event afternoon meeting /from 25 Aug 2026 2pm"
                        + " /to 4pm", 0));
        LuckyNoTaskCommand overnight = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event overnight meeting /from 25 Aug 2026 11pm"
                        + " /to 1am", 0));

        assertEquals("[E][ ] afternoon meeting (from: Tue Aug 25 2026, 2.00pm"
                        + " to: Tue Aug 25 2026, 4.00pm)",
                sameDay.getTask().toString());
        assertEquals("[E][ ] overnight meeting (from: Tue Aug 25 2026, 11.00pm"
                        + " to: Wed Aug 26 2026, 1.00am)",
                overnight.getTask().toString());
    }

    @Test
    void rejectsInvalidTaskNumbers() {
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

    @Test
    void rejectsArgumentsForListAndBye() {
        assertInputError("Why you so losor! Leave the list command to do its own thing lah", "list now", 0);
        assertInputError("Why you so losor! Leave the bye command to do its own thing lah", "bye now", 0);
    }

    private void assertInputError(String message, String input, int taskCount) {
        LuckyNoInputException error = assertThrows(LuckyNoInputException.class,
                () -> scanner.parseCommand(input, taskCount));
        assertEquals(message, error.getMessage());
    }
}
