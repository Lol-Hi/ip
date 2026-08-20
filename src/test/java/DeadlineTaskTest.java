import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the deadline task subclass and its command parser.
 */
class DeadlineTaskTest {

    @Test
    void deadlineTaskIncludesDeadlineInOutput() {
        DeadlineTask task = new DeadlineTask("return book", "Sunday");

        assertEquals("[D][ ] return book (by: Sunday)", task.toString());
    }

    @Test
    void deadlineFactoryParsesCommandArguments() {
        DeadlineTask task = DeadlineTask.createDeadlineTask(
                "return book /by Sunday");

        assertEquals("[D][ ] return book (by: Sunday)", task.toString());
    }

    @Test
    void invalidDeadlineArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DeadlineTask.createDeadlineTask("return book"));
    }
}
