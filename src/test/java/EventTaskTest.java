import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the event task subclass.
 */
class EventTaskTest {

    @Test
    void eventTaskIncludesStartAndEndTimesInOutput() {
        EventTask task = new EventTask("project meeting", "Mon 2pm", "4pm");

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                task.toString());
    }

}
