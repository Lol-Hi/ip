package luckynoslacky;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import luckynoslacky.luckycommand.LuckyNoCommand;
import luckynoslacky.luckycommand.LuckyNoDeleteCommand;
import luckynoslacky.luckycommand.LuckyNoFindCommand;
import luckynoslacky.luckycommand.LuckyNoListCommand;
import luckynoslacky.luckycommand.LuckyNoMarkCommand;
import luckynoslacky.luckycommand.LuckyNoReschedCommand;
import luckynoslacky.luckycommand.LuckyNoSnoozeCommand;
import luckynoslacky.luckycommand.LuckyNoTaskCommand;
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

    /** Describes the visual tone associated with a chatbot response. */
    public enum ResponseTone {
        /** Standard chatbot replies such as greetings and farewells. */
        NEUTRAL,
        /** Successful task changes. */
        SUCCESS,
        /** Task lists, search results, and other informational replies. */
        INFORMATION,
        /** Recoverable command and input problems. */
        WARNING,
        /** Storage and other system-level failures. */
        SYSTEM_ERROR
    }

    /**
     * Contains a chatbot reply and the action requested after displaying it.
     *
     * @param message user-facing reply
     * @param shouldExit whether the interface should close
     * @param tone visual tone associated with the reply
     */
    public record ChatResponse(
            String message,
            boolean shouldExit,
            ResponseTone tone) {
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
        this(dateTimeParser, new CsvSaver());
    }

    /**
     * Creates the chatbot with supplied parsing and storage dependencies.
     *
     * @param dateTimeParser parser used to interpret date and time input
     * @param csvSaver storage used to load and save tasks
     * @throws IllegalArgumentException if either argument is null
     */
    LuckyNoSlacky(DateTimeParser dateTimeParser, CsvSaver csvSaver) {
        if (dateTimeParser == null) {
            throw new IllegalArgumentException("Date-time parser cannot be null.");
        }
        if (csvSaver == null) {
            throw new IllegalArgumentException("CSV saver cannot be null.");
        }

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
                    getResponseTone(command));
        } catch (LuckyNoInputException exception) {
            return new ChatResponse(
                    exception.getMessage(), false, ResponseTone.WARNING);
        } catch (LuckyNoStorageException exception) {
            return new ChatResponse(
                    LuckyNoMessages.saveErrorMessage(),
                    false,
                    ResponseTone.SYSTEM_ERROR);
        }
    }

    /**
     * Maps a successfully parsed command to the visual tone of its reply.
     *
     * @param command command that produced the reply
     * @return visual tone appropriate for the command response
     */
    private ResponseTone getResponseTone(LuckyNoCommand command) {
        if (command instanceof LuckyNoTaskCommand
                || command instanceof LuckyNoMarkCommand
                || command instanceof LuckyNoDeleteCommand
                || command instanceof LuckyNoSnoozeCommand
                || command instanceof LuckyNoReschedCommand) {
            return ResponseTone.SUCCESS;
        }

        if (command instanceof LuckyNoListCommand
                || command instanceof LuckyNoFindCommand) {
            return ResponseTone.INFORMATION;
        }

        return ResponseTone.NEUTRAL;
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
