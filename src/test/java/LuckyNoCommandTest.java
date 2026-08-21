import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests the parsed command representations used by LuckyNoSlacky.
 */
class LuckyNoCommandTest {

    @Test
    void taskCommandStoresTaskAndUsesCreateTaskType() {
        TodoTask task = new TodoTask("read book");
        LuckyNoTaskCommand command = new LuckyNoTaskCommand(task);

        assertEquals(LuckyNoCommand.CommandType.CREATE_TASK, command.getCommandType());
        assertSame(task, command.getTask());
    }

    @Test
    void markCommandStoresTaskNumberAndDesiredStatus() {
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(2, true);

        assertEquals(LuckyNoCommand.CommandType.TOGGLE_TASK, command.getCommandType());
        assertEquals(2, command.getTaskNumber());
        assertEquals(true, command.shouldMarkDone());
    }

    @Test
    void unmarkCommandUsesSameCommandTypeWithFalseStatus() {
        LuckyNoMarkCommand command = new LuckyNoMarkCommand(2, false);

        assertEquals(LuckyNoCommand.CommandType.TOGGLE_TASK, command.getCommandType());
        assertEquals(false, command.shouldMarkDone());
    }

    @Test
    void deleteCommandStoresTaskNumberAndUsesDeleteTaskType() {
        LuckyNoDeleteCommand command = new LuckyNoDeleteCommand(3);

        assertEquals(LuckyNoCommand.CommandType.DELETE_TASK, command.getCommandType());
        assertEquals(3, command.getTaskNumber());
    }
}
