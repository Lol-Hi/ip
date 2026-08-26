package luckynoslacky;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import luckynoslacky.luckyparser.DateTimeParser;
import luckynoslacky.luckyparser.LuckyNoParser;
import luckynoslacky.luckystorage.CSVSaver;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoCLI;
import luckynoslacky.luckycommand.LuckyNoCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckyexception.LuckyNoStorageException;

/**
 * Starts the LuckyNoSlacky chatbot.
 */

public class LuckyNoSlacky {
    private static final String FIXED_NOW_PROPERTY =
            "luckynoslacky.fixedNow";

    private final LuckyNoCLI cliLucky;
    private final TaskMaster tmLucky;
    private final LuckyNoParser parserLucky;
    private final boolean loadError;

    /** Creates the chatbot using the system clock. */
    LuckyNoSlacky() {
        this(new DateTimeParser());
    }

    /**
     * Creates the chatbot with a supplied date and time parser.
     *
     * @param dateTimeParser parser used to interpret date and time input
     */
    LuckyNoSlacky(DateTimeParser dateTimeParser) {
        cliLucky = new LuckyNoCLI();

        CSVSaver csvSaver = new CSVSaver();
        tmLucky = new TaskMaster(csvSaver);

        boolean failedToLoad = false;
        try {
            tmLucky.loadTasksFromCSVStorageRecord(csvSaver.load());
        } catch (LuckyNoStorageException exception) {
            failedToLoad = true;
        }
        loadError = failedToLoad;

        parserLucky = new LuckyNoParser(dateTimeParser, tmLucky);
    }

    /**
     * Reads and executes commands until input is exhausted or the user exits.
     *
     * @return true if the user explicitly requested to exit
     */
    private boolean chatLoop() {
        while (cliLucky.hasNextLine()) {
            String userInput = cliLucky.readCommand();
            try {
                LuckyNoCommand command = parserLucky.parseCommand(userInput);
                cliLucky.showReply(command.execute());
                if (command.requestsExit()) {
                    return true;
                }
            } catch (LuckyNoInputException exception) {
                cliLucky.showReply(exception.getMessage());
            } catch (LuckyNoStorageException exception) {
                cliLucky.showSavingError();
            }
        }
        return false;
    }

    /**
     * Starts the chatbot application.
     *
     * @param args command-line arguments, which are not currently used
     */
    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky(createDateTimeParser());
        lucky.cliLucky.showGreeting();

        if (lucky.loadError) {
            lucky.cliLucky.showLoadingError();
            return;
        }

        if (!lucky.chatLoop()) {
            lucky.cliLucky.showGoodbye();
        }
    }

    /**
     * Creates the parser used by the application entry point.
     *
     * <p>The optional fixed-now property is used by deterministic UI tests.
     * Normal launches omit it and continue to use the system clock.</p>
     *
     * @return parser configured for the current run
     */
    private static DateTimeParser createDateTimeParser() {
        String fixedNow = System.getProperty(FIXED_NOW_PROPERTY);
        if (fixedNow == null || fixedNow.isBlank()) {
            return new DateTimeParser();
        }

        try {
            Instant instant = Instant.parse(fixedNow);
            return new DateTimeParser(
                    Clock.fixed(instant, ZoneOffset.UTC));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Fixed application time must be an ISO-8601 instant.",
                    exception);
        }
    }
}
