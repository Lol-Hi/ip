package luckynoslacky;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckyparser.DateTimeParser;
import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskList;
import luckynoslacky.luckyui.LuckyNoCli;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests the public chatbot facade used by the graphical interface.
 */
class LuckyNoSlackyTest {
    private InputStream originalInput;
    private PrintStream originalOutput;
    private ByteArrayOutputStream capturedOutput;

    /** Redirects standard input and output before each CLI integration test. */
    @BeforeEach
    void setUp() {
        originalInput = System.in;
        originalOutput = System.out;
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(
                capturedOutput, true, StandardCharsets.UTF_8));
    }

    /** Restores standard input and output after each CLI integration test. */
    @AfterEach
    void tearDown() {
        System.setIn(originalInput);
        System.setOut(originalOutput);
    }

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

    /** Verifies that the chatbot rejects a missing date/time parser. */
    @Test
    void construct_nullDateTimeParser_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoSlacky(null));
    }

    /** Verifies that the chatbot rejects a missing CSV saver dependency. */
    @Test
    void construct_nullCsvSaver_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LuckyNoSlacky(new DateTimeParser(), null));
    }

    /** Verifies that a load failure leaves the chatbot in degraded mode. */
    @Test
    void construct_loadFailure_setsLoadErrorState() {
        LuckyNoSlacky chatbot = new LuckyNoSlacky(
                new DateTimeParser(), new LoadFailingSaver());

        assertTrue(chatbot.hasLoadError());
        assertEquals(
                LuckyNoMessages.emptyTaskListMessage(),
                chatbot.getResponse("list").message());
    }

    /** Verifies that the CLI continues and saves after a load failure. */
    @Test
    void run_loadFailure_processesCommandsAndSavesTasks() {
        System.setIn(new java.io.ByteArrayInputStream(
                "list\ntodo buy milk\nbye\n".getBytes(StandardCharsets.UTF_8)));
        LoadFailingSaver saver = new LoadFailingSaver();
        LuckyNoSlacky chatbot = new LuckyNoSlacky(
                new DateTimeParser(), saver);

        LuckyNoSlacky.run(chatbot, new LuckyNoCli());

        String output = capturedOutput.toString(StandardCharsets.UTF_8);
        int loadErrorIndex = output.indexOf(LuckyNoMessages.loadErrorMessage());
        int emptyListIndex = output.indexOf(
                LuckyNoMessages.emptyTaskListMessage());
        assertTrue(loadErrorIndex >= 0);
        assertTrue(emptyListIndex > loadErrorIndex);
        assertEquals(
                loadErrorIndex,
                output.lastIndexOf(LuckyNoMessages.loadErrorMessage()));
        assertTrue(output.contains(LuckyNoMessages.goodbye()));
        assertTrue(saver.wasSaveCalled());
    }

    /** Simulates a storage source that cannot load but can save new tasks. */
    private static final class LoadFailingSaver extends CsvSaver {
        private boolean saveCalled;

        @Override
        public List<Task> load() {
            throw new LuckyNoStorageException("Simulated load failure.");
        }

        @Override
        public void save(TaskList taskList) {
            saveCalled = true;
        }

        /** Returns whether a task mutation attempted to save. */
        boolean wasSaveCalled() {
            return saveCalled;
        }
    }
}
