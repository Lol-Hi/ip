import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests the parsed command representations used by LuckyNoSlacky.
 */
class LuckyNoCommandTest {

    @Test
    void taskCommandStoresTaskAndUsesCreateTaskName() {
        TodoTask task = new TodoTask("read book");
        LuckyNoTaskCommand command = new LuckyNoTaskCommand(task);

        assertEquals("createTask", command.getCommandName());
        assertSame(task, command.getTask());
    }

    @Test
    void markCommandStoresTaskNumberAndDesiredStatus() {
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(2, true);

        assertEquals("toggleTask", command.getCommandName());
        assertEquals(2, command.getTaskNumber());
        assertEquals(true, command.shouldMarkDone());
    }

    @Test
    void unmarkCommandUsesSameCommandNameWithFalseStatus() {
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(2, false);

        assertEquals("toggleTask", command.getCommandName());
        assertEquals(false, command.shouldMarkDone());
    }

    @Test
    void deleteCommandStoresTaskNumberAndUsesDeleteTaskName() {
        LuckyNoDeleteCommand command = new LuckyNoDeleteCommand(3);

        assertEquals("deleteTask", command.getCommandName());
        assertEquals(3, command.getTaskNumber());
    }
}
