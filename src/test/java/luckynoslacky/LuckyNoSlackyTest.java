package luckynoslacky;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests the public chatbot facade used by the graphical interface.
 */
class LuckyNoSlackyTest {
    /** Verifies that invalid GUI input is returned as a chatbot reply. */
    @Test
    void getResponse_unknownCommand_returnsUnknownCommandMessage() {
        LuckyNoSlacky chatbot = new LuckyNoSlacky();

        assertEquals(
                LuckyNoMessages.unknownCommandMessage(),
                chatbot.getResponse("unknown command"));
    }
}
