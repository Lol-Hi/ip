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
     * Describes how a graphical interface should present a chatbot response.
     */
    public enum ResponseSeverity {
        /** A normal greeting, successful result, or farewell. */
        NORMAL,
        /** A user-input or storage problem that needs visual emphasis. */
        WARNING
    }

    /**
     * Contains a chatbot reply, its requested follow-up action, and its
     * presentation severity.
     *
     * @param message user-facing reply
     * @param shouldExit whether the interface should close
     * @param severity presentation severity for the reply
     */
    public record ChatResponse(
            String message,
            boolean shouldExit,
            ResponseSeverity severity) {
        /**
         * Creates a normal response for callers that do not need to specify a
         * presentation severity.
         *
         * @param message user-facing reply
         * @param shouldExit whether the interface should close
         */
        public ChatResponse(String message, boolean shouldExit) {
            this(message, shouldExit, ResponseSeverity.NORMAL);
        }
    }

    private final TaskMaster taskMaster;
    private final LuckyNoParser parser;
    private final boolean hasLoadError;

    /** Creates the chatbot using the system clock. */
    public LuckyNoSlacky() {
        this(new DateTimeParser());
    }

    /**
     * Creates the chatbot with a supplied date and time parser.
     *
     * @param dateTimeParser parser used to interpret date and time input
     * @throws IllegalArgumentException if {@code dateTimeParser} is null
     */
    LuckyNoSlacky(DateTimeParser dateTimeParser) {
        if (dateTimeParser == null) {
            throw new IllegalArgumentException("Date-time parser cannot be null.");
        }

        CsvSaver csvSaver = new CsvSaver();
        taskMaster = new TaskMaster(csvSaver);

        boolean hasLoadFailure = false;
        try {
            taskMaster.loadTasksFromCsvStorageRecord(csvSaver.load());
        } catch (LuckyNoStorageException exception) {
            hasLoadFailure = true;
        }
        hasLoadError = hasLoadFailure;

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
     * @param userInput command entered by the user
     * @return chatbot response and exit status
     */
    public ChatResponse getResponse(String userInput) {
        try {
            LuckyNoCommand command = parser.parseCommand(userInput);
            return new ChatResponse(
                    command.execute(),
                    command.shouldExit(),
                    ResponseSeverity.NORMAL);
        } catch (LuckyNoInputException exception) {
            return new ChatResponse(
                    exception.getMessage(),
                    false,
                    ResponseSeverity.WARNING);
        } catch (LuckyNoStorageException exception) {
            return new ChatResponse(
                    LuckyNoMessages.saveErrorMessage(),
                    false,
                    ResponseSeverity.WARNING);
        }
    }

    /**
     * Indicates whether loading the task data failed during startup.
     *
     * @return true if task data could not be loaded
     */
    public boolean hasLoadError() {
        return hasLoadError;
    }

    /**
     * Starts the chatbot application.
     *
     * @param args command-line arguments, which are not currently used
     */
    public static void main(String[] args) {
        LuckyNoSlacky chatbot = new LuckyNoSlacky(createDateTimeParser());
        LuckyNoCli commandLineInterface = new LuckyNoCli();
        commandLineInterface.showGreeting();

        if (chatbot.hasLoadError()) {
            commandLineInterface.showLoadingError();
            return;
        }

        if (!chatbot.chatLoop(commandLineInterface)) {
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
