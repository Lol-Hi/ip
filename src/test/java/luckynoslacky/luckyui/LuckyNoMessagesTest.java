package luckynoslacky.luckyui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyparser.LuckyNoParser;
import luckynoslacky.luckytask.TaskList;
import luckynoslacky.luckytask.TodoTask;

/**
 * Tests user-visible storage error messages.
 */
class LuckyNoMessagesTest {
    /** Verifies formats are selected correctly for dated commands. */
    @Test
    void invalidFormatMessage_knownCommandAndFormat_returnsFormattedMessage() {
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: deadline "
                        + "<description> /by <date/time>.",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.DEADLINE));
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: event "
                        + "<description> /from <start date/time> /to <end date/time>.",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.EVENT));
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: find [<description>] [/on <date>]",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND));
    }

    /** Verifies that multiple valid formats are included in the error message. */
    @Test
    void invalidFormatMessage_multipleValidFormats_returnsAllFormats() {
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: find <description> "
                        + "or /on <date>",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND,
                        "<description>",
                        "/on <date>"));
    }

    /** Verifies the dedicated decimal calendar duration message. */
    @Test
    void decimalCalendarDurationMessage_noArguments_returnsConfiguredMessage() {
        assertEquals(
                "Paiseh bro... i cannot settle decimal values for years and months yet...",
                LuckyNoMessages.decimalCalendarDurationMessage());
    }

    /** Verifies that the banner retains its multiline layout. */
    @Test
    void banner_multipleLines_preservesExpectedLayout() {
        assertEquals(
                "     .--\"\"\"\"\"--.\n"
                        + "   /  /^\\   /^\\  \\\n"
                        + "  |  .---------.  |\n"
                        + "  |  | | | | | |  |\n"
                        + "   \\ '---------' /\n"
                        + "     '-._____.-'\n"
                        + "    [NO SLACKING]\n"
                        + "  LuckyNoSlacky is here to help!",
                LuckyNoMessages.banner());
    }

    /** Verifies that task-list messages use the relevant search header. */
    @Test
    void listTasksMessage_dateSearch_usesDateHeaderAndTaskLines() {
        TaskList taskList = new TaskList(LocalDate.of(2026, 8, 26));
        taskList.addTask(2, new TodoTask("read book"));

        assertEquals(
                "Nah, all these things you need to do on: Aug 26 2026\n"
                        + "2.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskList));
    }

    /** Verifies that an empty task-list message is shared by empty searches. */
    @Test
    void listTasksMessage_emptySearch_usesEmptyTaskListMessage() {
        assertEquals(
                LuckyNoMessages.emptyTaskListMessage(),
                LuckyNoMessages.listTasksMessage(new TaskList()));
    }

    /** Verifies unsupported command formats trigger the internal assertion. */
    @Test
    void invalidFormatMessage_unsupportedOrNullCommand_throwsExpectedExceptions() {
        assertThrows(AssertionError.class, () ->
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.LIST));
        assertThrows(IllegalArgumentException.class, () ->
                LuckyNoMessages.invalidFormatMessage(null));
    }

    /** Verifies the configured loading error message. */
    @Test
    void loadErrorMessage_noArguments_returnsConfiguredMessage() {
        assertEquals(
                "Eh you so free ah, no tasks were loaded! "
                        + "If you think this is salah, check your task data file.",
                LuckyNoMessages.loadErrorMessage());
    }

    /** Verifies the configured saving error message. */
    @Test
    void saveErrorMessage_noArguments_returnsConfiguredMessage() {
        assertEquals(
                "Honggan la your system abit rabs ah, I cannot save your task",
                LuckyNoMessages.saveErrorMessage());
    }
}
