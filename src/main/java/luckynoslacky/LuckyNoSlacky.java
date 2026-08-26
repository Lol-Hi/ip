package luckynoslacky;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import luckynoslacky.luckycommand.LuckyNoCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckyparser.DateTimeParser;
import luckynoslacky.luckyparser.LuckyNoParser;
import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoCli;

/**
 * Starts the LuckyNoSlacky chatbot.
 */
public class LuckyNoSlacky {
    private static final String FIXED_NOW_PROPERTY =
            "luckynoslacky.fixedNow";

    private final LuckyNoCli commandLineInterface;
    private final TaskMaster taskMaster;
    private final LuckyNoParser parser;
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
        commandLineInterface = new LuckyNoCli();

        CsvSaver csvSaver = new CsvSaver();
        taskMaster = new TaskMaster(csvSaver);

        boolean failedToLoad = false;
        try {
            taskMaster.loadTasksFromCsvStorageRecord(csvSaver.load());
        } catch (LuckyNoStorageException exception) {
            failedToLoad = true;
        }
        loadError = failedToLoad;

        parser = new LuckyNoParser(dateTimeParser, taskMaster);
    }

    /**
     * Reads and executes commands until input is exhausted or the user exits.
     *
     * @return true if the user explicitly requested to exit
     */
    private boolean chatLoop() {
        while (commandLineInterface.hasNextLine()) {
            String userInput = commandLineInterface.readCommand();
            try {
                LuckyNoCommand command = parser.parseCommand(userInput);
                commandLineInterface.showReply(command.execute());
                if (command.requestsExit()) {
                    return true;
                }
            } catch (LuckyNoInputException exception) {
                commandLineInterface.showReply(exception.getMessage());
            } catch (LuckyNoStorageException exception) {
                commandLineInterface.showSavingError();
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
        lucky.commandLineInterface.showGreeting();

        if (lucky.loadError) {
            lucky.commandLineInterface.showLoadingError();
            return;
        }

        if (!lucky.chatLoop()) {
            lucky.commandLineInterface.showGoodbye();
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
