package luckynoslacky;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                chatbot.getResponse("unknown command").message());
        assertFalse(chatbot.getResponse("unknown command").shouldExit());
    }

    /** Verifies that the bye command returns an exit signal to the GUI. */
    @Test
    void getResponse_byeCommand_returnsGoodbyeAndExitStatus() {
        LuckyNoSlacky chatbot = new LuckyNoSlacky();

        LuckyNoSlacky.ChatResponse response = chatbot.getResponse("bye");

        assertEquals(LuckyNoMessages.goodbye(), response.message());
        assertTrue(response.shouldExit());
    }
}
