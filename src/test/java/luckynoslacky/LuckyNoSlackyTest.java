package luckynoslacky;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckyparser.DateTimeParser;
import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests the public chatbot facade used by the graphical interface.
 */
class LuckyNoSlackyTest {
    @TempDir
    Path temporaryDirectory;

    /** Verifies that invalid GUI input is returned as a chatbot reply. */
    @Test
    void getResponse_unknownCommand_returnsUnknownCommandMessage() {
        LuckyNoSlacky chatbot = createChatbot();

        assertEquals(
                LuckyNoMessages.unknownCommandMessage(),
                chatbot.getResponse("unknown command").message());
        assertFalse(chatbot.getResponse("unknown command").shouldExit());
        assertEquals(
                LuckyNoSlacky.ResponseTone.WARNING,
                chatbot.getResponse("unknown command").tone());
    }

    /** Verifies that the bye command returns an exit signal to the GUI. */
    @Test
    void getResponse_byeCommand_returnsGoodbyeAndExitStatus() {
        LuckyNoSlacky chatbot = createChatbot();

        LuckyNoSlacky.ChatResponse response = chatbot.getResponse("bye");

        assertEquals(LuckyNoMessages.goodbye(), response.message());
        assertTrue(response.shouldExit());
        assertEquals(LuckyNoSlacky.ResponseTone.NEUTRAL, response.tone());
    }

    /** Verifies that a successful task command receives a success tone. */
    @Test
    void getResponse_taskCommand_returnsSuccessTone() {
        LuckyNoSlacky chatbot = createChatbot();

        LuckyNoSlacky.ChatResponse response = chatbot.getResponse(
                "todo update specification");

        assertEquals(LuckyNoSlacky.ResponseTone.SUCCESS, response.tone());
    }

    /** Verifies that a task-list command receives an informational tone. */
    @Test
    void getResponse_listCommand_returnsInformationTone() {
        LuckyNoSlacky chatbot = createChatbot();

        LuckyNoSlacky.ChatResponse response = chatbot.getResponse("list");

        assertEquals(LuckyNoSlacky.ResponseTone.INFORMATION, response.tone());
    }

    /** Verifies that the chatbot rejects a missing date/time parser. */
    @Test
    void construct_nullDateTimeParser_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoSlacky(null));
    }

    /** Creates a chatbot that persists only within the test's temporary directory. */
    private LuckyNoSlacky createChatbot() {
        return new LuckyNoSlacky(
                new DateTimeParser(),
                new CsvSaver(temporaryDirectory.resolve("luckyNoSlacky.csv")));
    }
}
