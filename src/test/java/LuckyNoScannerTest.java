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
    void parsesTodoCommandIntoTaskCommand() throws LuckyNoInputError {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("todo borrow book", 0));

        assertEquals("[T][ ] borrow book", command.getTask().toString());
    }

    @Test
    void parsesDeadlineCommandIntoDeadlineTask() throws LuckyNoInputError {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("deadline return book /by Sunday", 0));

        assertEquals("[D][ ] return book (by: Sunday)", command.getTask().toString());
    }

    @Test
    void parsesEventCommandIntoEventTask() throws LuckyNoInputError {
        LuckyNoTaskCommand command = assertInstanceOf(LuckyNoTaskCommand.class,
                scanner.parseCommand("event project meeting /from Mon 2pm /to 4pm", 0));

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                command.getTask().toString());
    }

    @Test
    void parsesMarkAndUnmarkAsExplicitStatuses() throws LuckyNoInputError {
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
    void parsesListAndByeCommands() throws LuckyNoInputError {
        assertEquals("list", scanner.parseCommand("list", 0).getCommandName());
        assertEquals("bye", scanner.parseCommand("bye", 0).getCommandName());
    }

    @Test
    void rejectsEmptyAndUnknownCommands() {
        assertInputError("Please enter a command.", "   ", 0);
        assertInputError("I don't recognize that command. Please use todo, deadline, event, list, mark, unmark, or bye.",
                "dance", 0);
    }

    @Test
    void rejectsMalformedTaskCommands() {
        assertInputError("Please provide a task description.", "todo", 0);
        assertInputError("Please use: deadline <description> /by <date/time>.",
                "deadline return book", 0);
        assertInputError("Please use: event <description> /from <start> /to <end>.",
                "event meeting /from 2pm", 0);
    }

    @Test
    void rejectsInvalidTaskNumbers() {
        assertInputError("Please provide a task number to mark.", "mark", 1);
        assertInputError("That task number is invalid.", "mark 2", 1);
        assertInputError("Please provide a task number to unmark.", "unmark 1 extra", 1);
    }

    @Test
    void rejectsArgumentsForListAndBye() {
        assertInputError("The list command does not take arguments.", "list now", 0);
        assertInputError("The bye command does not take arguments.", "bye now", 0);
    }

    private void assertInputError(String message, String input, int taskCount) {
        LuckyNoInputError error = assertThrows(LuckyNoInputError.class,
                () -> scanner.parseCommand(input, taskCount));
        assertEquals(message, error.getMessage());
    }
}
