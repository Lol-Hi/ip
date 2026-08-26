package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/**
 * Tests the event task subclass.
 */
class EventTaskTest {

    @Test
    void eventTaskIncludesStartAndEndTimesInOutput() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 6, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 6, 16, 0);
        EventTask task = new EventTask("project meeting", start, end);

        assertEquals("[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                task.toString());
        assertEquals(List.of("E", "0", "project meeting",
                        "2026-08-06 14:00", "2026-08-06 16:00"),
                task.getCSVStorageFields());
    }

    @Test
    void eventEndingBeforeStartingIsRejected() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 6, 16, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 6, 14, 0);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new EventTask("project meeting", start, end));
    }

}
