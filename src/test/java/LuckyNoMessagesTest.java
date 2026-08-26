import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests user-visible storage error messages.
 */
class LuckyNoMessagesTest {
    @Test
    void invalidFormatMessageUsesTheCommandNameAndItsFormat() {
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: deadline "
                        + "<description> /by <date/time>.",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.DEADLINE));
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: event "
                        + "<description> /from <start> /to <end>.",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.EVENT));
        assertEquals(
                "Eh HELLO you know how to type command one anot? \n"
                        + "Lai lai let me teach you: find /on <date>",
                LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.FIND));
    }

    @Test
    void invalidFormatMessageRejectsCommandsWithoutFormats() {
        assertThrows(IllegalArgumentException.class,
                () -> LuckyNoMessages.invalidFormatMessage(
                        LuckyNoParser.CommandName.LIST));
        assertThrows(IllegalArgumentException.class,
                () -> LuckyNoMessages.invalidFormatMessage(null));
    }

    @Test
    void loadErrorMessageMatchesConfiguredReply() {
        assertEquals(
                "Eh you so free ah, no tasks were loaded! "
                        + "If you think this is salah, check your task data file.",
                LuckyNoMessages.loadErrorMessage());
    }

    @Test
    void saveErrorMessageMatchesConfiguredReply() {
        assertEquals(
                "Honggan la your system abit rabs ah, I cannot save your task",
                LuckyNoMessages.saveErrorMessage());
    }
}
