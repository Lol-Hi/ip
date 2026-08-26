package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests the deadline task subclass.
 */
class DeadlineTaskTest {

    /** Verifies that a deadline includes its formatted due time. */
    @Test
    void toString_deadlineTask_includesDeadline() {
        LocalDateTime deadline = LocalDateTime.of(2019, 10, 15, 14, 15);
        DeadlineTask task = new DeadlineTask("return book", deadline);

        assertEquals("[D][ ] return book (by: Tue Oct 15 2019, 2.15pm)",
                task.toString());
        assertEquals(List.of("D", "0", "return book", "", "2019-10-15 14:15"),
                task.getCsvStorageFields());
    }

}
