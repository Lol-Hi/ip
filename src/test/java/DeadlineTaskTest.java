import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the deadline task subclass.
 */
class DeadlineTaskTest {

    @Test
    void deadlineTaskIncludesDeadlineInOutput() {
        DeadlineTask task = new DeadlineTask("return book", "Sunday");

        assertEquals("[D][ ] return book (by: Sunday)", task.toString());
    }

}
