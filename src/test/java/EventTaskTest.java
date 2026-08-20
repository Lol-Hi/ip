import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the event task subclass and its command parser.
 */
class EventTaskTest {

    @Test
    void eventTaskIncludesStartAndEndTimesInOutput() {
        EventTask task = new EventTask("project meeting", "Mon 2pm", "4pm");

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                task.toString());
    }

    @Test
    void eventFactoryParsesCommandArguments() {
        EventTask task = EventTask.createEventTask(
                "project meeting /from Mon 2pm /to 4pm");

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                task.toString());
    }

    @Test
    void invalidEventArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> EventTask.createEventTask("project meeting /from Mon 2pm"));
    }
}
