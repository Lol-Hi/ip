package luckynoslacky.luckyui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyparser.LuckyNoParser;

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

    /** Verifies unsupported command formats are rejected internally. */
    @Test
    void invalidFormatMessage_commandWithoutFormat_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.LIST));
        assertThrows(IllegalArgumentException.class,
                () -> LuckyNoMessages.invalidFormatMessage(null));
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
