package luckynoslacky.luckyui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests command-line input and output handled by LuckyNoCli.
 */
class LuckyNoCliTest {
    private static final String DIVIDER =
            "  ____________________________________________________________\n";

    private InputStream originalInput;
    private PrintStream originalOutput;
    private ByteArrayOutputStream capturedOutput;

    @BeforeEach
    void setUp() {
        originalInput = System.in;
        originalOutput = System.out;
        capturedOutput = new ByteArrayOutputStream();

        System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalInput);
        System.setOut(originalOutput);
    }

    @Test
    void readCommand_inputLinesUntilExhausted_returnsEachCommand() {
        LuckyNoCli cli = createCliWithInput("todo read book\nbye\n");

        assertTrue(cli.hasNextLine());
        assertEquals("todo read book", cli.readCommand());
        assertTrue(cli.hasNextLine());
        assertEquals("bye", cli.readCommand());
        assertFalse(cli.hasNextLine());
    }

    @Test
    void showReply_userOutput_usesDividerAndIndentation() {
        LuckyNoCli cli = createCliWithInput("");

        cli.showReply("first line\nsecond line");

        assertEquals(
                expectedReply("first line\nsecond line"),
                capturedOutput.toString(StandardCharsets.UTF_8));
    }

    @Test
    void showGreeting_noInput_displaysBannerAndGreeting() {
        LuckyNoCli cli = createCliWithInput("");

        cli.showGreeting();

        assertEquals(
                DIVIDER + LuckyNoMessages.banner() + "\n"
                        + expectedReply(LuckyNoMessages.greeting()),
                capturedOutput.toString(StandardCharsets.UTF_8));
    }

    @Test
    void showGoodbyeAndStorageErrors_configuredMessages_displaysReplies() {
        LuckyNoCli cli = createCliWithInput("");

        cli.showGoodbye();
        cli.showLoadingError();
        cli.showSavingError();

        assertEquals(
                expectedReply(LuckyNoMessages.goodbye())
                        + expectedReply(LuckyNoMessages.loadErrorMessage())
                        + expectedReply(LuckyNoMessages.saveErrorMessage()),
                capturedOutput.toString(StandardCharsets.UTF_8));
    }

    @Test
    void echo_input_usesStandardReplyFormatting() {
        LuckyNoCli cli = createCliWithInput("");

        cli.echo("hello LuckyNoSlacky");

        assertEquals(
                expectedReply("hello LuckyNoSlacky"),
                capturedOutput.toString(StandardCharsets.UTF_8));
    }

    private LuckyNoCli createCliWithInput(String input) {
        System.setIn(new ByteArrayInputStream(
                input.getBytes(StandardCharsets.UTF_8)));
        return new LuckyNoCli();
    }

    private String expectedReply(String output) {
        String indentedOutput = output.replace("\n", "\n  ");
        return DIVIDER + "  " + indentedOutput + "\n" + DIVIDER;
    }
}
