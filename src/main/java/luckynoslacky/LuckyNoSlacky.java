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
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Starts the LuckyNoSlacky chatbot.
 */
public class LuckyNoSlacky {
    private static final String FIXED_NOW_PROPERTY =
            "luckynoslacky.fixedNow";

    /**
     * Contains a chatbot reply and the action requested after displaying it.
     *
     * @param message user-facing reply
     * @param shouldExit whether the interface should close
     */
    public record ChatResponse(String message, boolean shouldExit) {
    }

    private final TaskMaster taskMaster;
    private final LuckyNoParser parser;
    private final boolean loadError;

    /** Creates the chatbot using the system clock. */
    public LuckyNoSlacky() {
        this(new DateTimeParser());
    }

    /**
     * Creates the chatbot with a supplied date and time parser.
     *
     * @param dateTimeParser parser used to interpret date and time input
     */
    LuckyNoSlacky(DateTimeParser dateTimeParser) {
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
    private boolean chatLoop(LuckyNoCli commandLineInterface) {
        while (commandLineInterface.hasNextLine()) {
            String userInput = commandLineInterface.readCommand();
            try {
                LuckyNoCommand command = parser.parseCommand(userInput);
                commandLineInterface.showReply(command.execute());
                if (command.shouldExit()) {
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
     * Processes one command from a graphical user interface.
     *
     * @param input command entered by the user
     * @return chatbot response and exit status
     */
    public ChatResponse getResponse(String input) {
        try {
            LuckyNoCommand command = parser.parseCommand(input);
            return new ChatResponse(
                    command.execute(),
                    command.shouldExit());
        } catch (LuckyNoInputException exception) {
            return new ChatResponse(exception.getMessage(), false);
        } catch (LuckyNoStorageException exception) {
            return new ChatResponse(
                    LuckyNoMessages.saveErrorMessage(),
                    false);
        }
    }

    /**
     * Indicates whether loading the task data failed during startup.
     *
     * @return true if task data could not be loaded
     */
    public boolean hasLoadError() {
        return loadError;
    }

    /**
     * Starts the chatbot application.
     *
     * @param args command-line arguments, which are not currently used
     */
    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky(createDateTimeParser());
        LuckyNoCli commandLineInterface = new LuckyNoCli();
        commandLineInterface.showGreeting();

        if (lucky.loadError) {
            commandLineInterface.showLoadingError();
            return;
        }

        if (!lucky.chatLoop(commandLineInterface)) {
            commandLineInterface.showGoodbye();
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
