import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests user-visible storage error messages.
 */
class LuckyNoMessagesTest {
    @Test
    void loadErrorMessageMatchesConfiguredReply() {
        assertEquals(
                "Eh you so free ah, no tasks were loaded! "
                        + "If you think this is salah, check your task data file.",
                LuckyNoMessages.loadErrorMessage());
    }

    @Test
    void saveErrorMessageMatchesConfiguredReply() {
        assertEquals(
                "Honggan la your system abit rabs ah, I cannot save your task",
                LuckyNoMessages.saveErrorMessage());
    }
}
