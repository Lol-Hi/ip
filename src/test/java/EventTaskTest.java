import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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
        assertEquals(List.of("E", "0", "project meeting", "Mon 2pm", "4pm"),
                task.getCSVStorageFields());
    }

}
