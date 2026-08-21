import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests parsing and validation of user input.
 */
class LuckyNoScannerTest {
    private final LuckyNoScanner scanner = new LuckyNoScanner();

    @Test
    void parsesTodoCommandIntoTaskCommand() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("todo borrow book", 0));

        assertEquals("[T][ ] borrow book", command.getTask().toString());
    }

    @Test
    void parsesDeadlineCommandIntoDeadlineTask() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("deadline return book /by Sunday", 0));

        assertEquals("[D][ ] return book (by: Sunday)", command.getTask().toString());
    }

    @Test
    void parsesEventCommandIntoEventTask() throws LuckyNoInputException {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event project meeting /from Mon 2pm /to 4pm", 0));

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                command.getTask().toString());
    }

    @Test
    void parsesMarkAndUnmarkAsExplicitStatuses() throws LuckyNoInputException {
        LuckyNoMarkCommand mark = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("mark 1", 1));
        LuckyNoMarkCommand unmark = assertInstanceOf(LuckyNoMarkCommand.class,
                scanner.parseCommand("unmark 1", 1));

        assertEquals("toggleTask", mark.getCommandName());
        assertEquals(true, mark.shouldMarkDone());
        assertEquals("toggleTask", unmark.getCommandName());
        assertEquals(false, unmark.shouldMarkDone());
    }

    @Test
    void parsesListAndByeCommands() throws LuckyNoInputException {
        assertEquals("list", scanner.parseCommand("list", 0).getCommandName());
        assertEquals("bye", scanner.parseCommand("bye", 0).getCommandName());
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
        assertInputError("What talking you? I only understand todo, deadline, event, list, mark, unmark, or bye, ok?",
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
    void rejectsInvalidTaskNumbers() {
        assertInputError("Eh which task you talking about har? Can say clearly anot.", "mark", 1);
        assertInputError("You siao ah how to spin this task from thin air?", "mark 2", 1);
        assertInputError("Eh which task you talking about har? Can say clearly anot.",
                "unmark 1 extra", 1);
        assertInputError("You siao ah how to spin this task from thin air?", "unmark -1", 1);
        assertInputError("You siao ah how to spin this task from thin air?", "mark one", 1);
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
