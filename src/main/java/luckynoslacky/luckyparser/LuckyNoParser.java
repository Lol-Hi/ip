package luckynoslacky.luckyparser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import luckynoslacky.luckycommand.LuckyNoByeCommand;
import luckynoslacky.luckycommand.LuckyNoCommand;
import luckynoslacky.luckycommand.LuckyNoDeleteCommand;
import luckynoslacky.luckycommand.LuckyNoFindCommand;
import luckynoslacky.luckycommand.LuckyNoListCommand;
import luckynoslacky.luckycommand.LuckyNoMarkCommand;
import luckynoslacky.luckycommand.LuckyNoTaskCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TodoTask;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Parses user input into commands and reports invalid input consistently.
 */
public class LuckyNoParser {
    private final DateTimeParser dateTimeParser;
    private final TaskMaster taskMaster;

    /** Creates a parser using the current system clock and a default task master. */
    public LuckyNoParser() {
        this(new DateTimeParser(), new TaskMaster());
    }

    /**
     * Creates a parser using the supplied date/time parser.
     *
     * @param dateTimeParser parser used to resolve date/time arguments
     */
    public LuckyNoParser(DateTimeParser dateTimeParser) {
        this(dateTimeParser, new TaskMaster());
    }

    /**
     * Creates a parser using the system clock and supplied task master.
     *
     * @param taskMaster task master attached to parsed commands
     */
    public LuckyNoParser(TaskMaster taskMaster) {
        this(new DateTimeParser(), taskMaster);
    }

    /**
     * Creates a parser with explicit date/time and task-list dependencies.
     *
     * @param dateTimeParser parser used to resolve date/time arguments
     * @param taskMaster task master attached to parsed commands
     */
    public LuckyNoParser(DateTimeParser dateTimeParser, TaskMaster taskMaster) {
        if (dateTimeParser == null) {
            throw new IllegalArgumentException("Date-time parser cannot be null.");
        }
        if (taskMaster == null) {
            throw new IllegalArgumentException("Task master cannot be null.");
        }
        this.dateTimeParser = dateTimeParser;
        this.taskMaster = taskMaster;
    }
    /**
     * Represents a command name that can be entered by the user.
     */
    public enum CommandName {
        /** Exit command. */
        BYE("bye"),
        /** List command. */
        LIST("list"),
        /** ToDo creation command. */
        TODO("todo"),
        /** Deadline creation command. */
        DEADLINE("deadline"),
        /** Event creation command. */
        EVENT("event"),
        /** Mark-done command. */
        MARK("mark"),
        /** Mark-undone command. */
        UNMARK("unmark"),
        /** Task-deletion command. */
        DELETE("delete"),
        /** Date-search command. */
        FIND("find");

        private final String inputName;

        CommandName(String inputName) {
            this.inputName = inputName;
        }

        /**
         * Returns the command spelling accepted from the user.
         *
         * @return user-facing command name
         */
        public String getInputName() {
            return inputName;
        }

        /**
         * Finds the command represented by a user-provided token.
         *
         * @param commandToken command token
         * @return matching command, or an empty Optional if there is no match
         */
        public static Optional<CommandName> fromCommandToken(String commandToken) {
            return Arrays.stream(values())
                    .filter(commandName -> commandName.inputName.equalsIgnoreCase(commandToken))
                    .findFirst();
        }
    }

    /**
     * Parses one line of chatbot input.
     *
     * @param userInput raw user input
     * @param taskCount number of tasks currently stored
     * @return parsed command
     * @throws LuckyNoInputException if the input is invalid
     */
    public LuckyNoCommand parseCommand(String userInput, int taskCount)
            throws LuckyNoInputException {
        return parseCommand(userInput, taskCount, taskMaster);
    }

    /**
     * Parses input using the task master's current task count.
     *
     * @param userInput raw user input
     * @return parsed command
     * @throws LuckyNoInputException if the input is invalid
     */
    public LuckyNoCommand parseCommand(String userInput)
            throws LuckyNoInputException {
        return parseCommand(userInput, taskMaster.getTaskCount(), taskMaster);
    }

    /**
     * Parses a command with an explicit task count and command dependency.
     *
     * @param userInput raw user input
     * @param taskCount current number of tasks
     * @param commandTaskMaster task master attached to parsed commands
     * @return parsed command
     * @throws LuckyNoInputException if the input is invalid
     */
    private LuckyNoCommand parseCommand(
            String userInput,
            int taskCount,
            TaskMaster commandTaskMaster)
            throws LuckyNoInputException {
        ParsedInput parsedInput = parseInput(userInput);
        CommandName commandName = parseCommandName(parsedInput.commandToken());
        return createCommand(
                commandName,
                parsedInput.arguments(),
                taskCount,
                commandTaskMaster);
    }

    /**
     * Splits raw input into a command token and its remaining arguments.
     *
     * @param userInput raw user input
     * @return normalized command token and arguments
     * @throws LuckyNoInputException if the input is blank
     */
    private ParsedInput parseInput(String userInput) throws LuckyNoInputException {
        if (userInput == null || userInput.trim().isEmpty()) {
            throw new LuckyNoInputException(LuckyNoMessages.missingCommandMessage());
        }

        String[] commandParts = userInput.trim().split("\\s+", 2);
        String commandToken = commandParts[0].toLowerCase(Locale.ROOT);
        String arguments = commandParts.length == 2
                ? commandParts[1].trim()
                : "";
        return new ParsedInput(commandToken, arguments);
    }

    /**
     * Converts a command token into a supported command name.
     *
     * @param commandToken normalized command token
     * @return matching command name
     * @throws LuckyNoInputException if the token is not recognized
     */
    private static CommandName parseCommandName(String commandToken)
            throws LuckyNoInputException {
        return CommandName.fromCommandToken(commandToken)
                .orElseThrow(() -> new LuckyNoInputException(
                        LuckyNoMessages.unknownCommandMessage()));
    }

    /**
     * Builds the executable command represented by parsed input.
     *
     * @param commandName recognized command name
     * @param commandArguments command arguments
     * @param taskCount current number of tasks
     * @param commandTaskMaster task master attached to the command
     * @return executable command
     * @throws LuckyNoInputException if the command arguments are invalid
     */
    private LuckyNoCommand createCommand(
            CommandName commandName,
            String commandArguments,
            int taskCount,
            TaskMaster commandTaskMaster)
            throws LuckyNoInputException {

        switch (commandName) {
            case BYE:
                checkNoArguments(commandArguments, CommandName.BYE.getInputName());
                return new LuckyNoByeCommand();
            case LIST:
                checkNoArguments(commandArguments, CommandName.LIST.getInputName());
                return new LuckyNoListCommand(commandTaskMaster);
            case TODO:
                return new LuckyNoTaskCommand(
                        parseTodo(commandArguments), commandTaskMaster);
            case DEADLINE:
                return new LuckyNoTaskCommand(
                        parseDeadline(commandArguments), commandTaskMaster);
            case EVENT:
                return new LuckyNoTaskCommand(
                        parseEvent(commandArguments), commandTaskMaster);
            case MARK:
                return new LuckyNoMarkCommand(
                        parseTaskNumber(commandArguments, taskCount),
                        true, commandTaskMaster);
            case UNMARK:
                return new LuckyNoMarkCommand(
                        parseTaskNumber(commandArguments, taskCount),
                        false, commandTaskMaster);
            case DELETE:
                return new LuckyNoDeleteCommand(
                        parseTaskNumber(commandArguments, taskCount), commandTaskMaster);
            case FIND:
                return parseFind(commandArguments, commandTaskMaster);
            default:
                assert false : "Unhandled command name: " + commandName;
                throw new IllegalStateException("Unhandled command name.");
        }
    }

    /**
     * Rejects arguments for commands that must stand alone.
     *
     * @param commandArguments command arguments
     * @param commandName command name used in the error message
     * @throws LuckyNoInputException if arguments are present
     */
    private void checkNoArguments(String commandArguments, String commandName)
            throws LuckyNoInputException {
        if (!commandArguments.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.extraArgumentsMessage(commandName));
        }
    }

    /**
     * Validates and converts a one-based task number.
     *
     * @param taskNumberText task-number text
     * @param taskCount number of tasks currently available
     * @return validated one-based task number
     * @throws LuckyNoInputException if the number is missing or out of range
     */
    private int parseTaskNumber(String taskNumberText, int taskCount)
            throws LuckyNoInputException {
        if (taskNumberText.isEmpty() || taskNumberText.matches(".*\\s+.*")) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.missingTaskNumberMessage());
        }

        try {
            int taskNumber = Integer.parseInt(taskNumberText);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new NumberFormatException();
            }
            return taskNumber;
        } catch (NumberFormatException exception) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidTaskNumberMessage());
        }
    }

    /**
     * Parses the description of a ToDo command.
     *
     * @param commandArguments command arguments
     * @return constructed ToDo task
     * @throws LuckyNoInputException if the description is missing
     */
    private TodoTask parseTodo(String commandArguments) throws LuckyNoInputException {
        if (commandArguments.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.missingTaskDescriptionMessage());
        }
        return new TodoTask(commandArguments);
    }

    /**
     * Parses a deadline description and its {@code /by} date/time.
     *
     * @param commandArguments command arguments
     * @return constructed deadline task
     * @throws LuckyNoInputException if the format or date/time is invalid
     */
    private DeadlineTask parseDeadline(String commandArguments)
            throws LuckyNoInputException {
        int byIndex = commandArguments.indexOf("/by");
        if (byIndex <= 0) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.DEADLINE));
        }

        String description = commandArguments.substring(0, byIndex).trim();
        String byTimeText = commandArguments.substring(byIndex + 3).trim();
        if (description.isEmpty() || byTimeText.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.DEADLINE));
        }
        LocalDateTime byTime = dateTimeParser.parseEndDateTime(byTimeText).dateTime();
        if (byTime.isBefore(dateTimeParser.now())) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
        return new DeadlineTask(description, byTime);
    }

    /**
     * Parses a find command with an optional description and date filter.
     * Text before the {@code /on} tag is treated as the description query.
     *
     * @param commandArguments command arguments
     * @param taskMaster task master attached to the parsed command
     * @return parsed find command
     * @throws LuckyNoInputException if the search format or date is invalid
     */
    private LuckyNoFindCommand parseFind(
            String commandArguments, TaskMaster taskMaster)
            throws LuckyNoInputException {
        if (commandArguments.isBlank()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(CommandName.FIND));
        }

        int onIndex = commandArguments.indexOf("/on");
        if (onIndex < 0) {
            return new LuckyNoFindCommand(
                    commandArguments.trim(), null, taskMaster);
        }

        if (commandArguments.indexOf("/on", onIndex + 3) >= 0) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(CommandName.FIND));
        }

        String descriptionQuery = commandArguments.substring(0, onIndex).trim();
        String dateText = commandArguments.substring(onIndex + 3).trim();
        if (dateText.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.FIND));
        }

        LocalDateTime searchDateTime =
                dateTimeParser.parseStartDateTime(dateText).dateTime();
        if (descriptionQuery.isEmpty()) {
            descriptionQuery = null;
        }
        return new LuckyNoFindCommand(
                descriptionQuery,
                searchDateTime,
                taskMaster);
    }

    /**
     * Parses an event description and its {@code /from} and {@code /to} times.
     *
     * @param commandArguments command arguments
     * @return constructed event task
     * @throws LuckyNoInputException if the format or date/time is invalid
     */
    private EventTask parseEvent(String commandArguments)
            throws LuckyNoInputException {
        int fromIndex = commandArguments.indexOf("/from");
        int toIndex = commandArguments.indexOf("/to");
        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.EVENT));
        }

        String description = commandArguments.substring(0, fromIndex).trim();
        String startTimeText = commandArguments
                .substring(fromIndex + 5, toIndex).trim();
        String endTimeText = commandArguments.substring(toIndex + 3).trim();
        if (description.isEmpty() || startTimeText.isEmpty() || endTimeText.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.EVENT));
        }
        DateTimeParser.ParsedDateTime startTime =
                dateTimeParser.parseStartDateTime(startTimeText);
        DateTimeParser.ParsedDateTime endTime =
                dateTimeParser.parseEndDateTime(endTimeText, startTime.dateTime());
        if (endTime.dateTime().isBefore(startTime.dateTime())) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
        return new EventTask(description, startTime.dateTime(), endTime.dateTime());
    }

    private record ParsedInput(String commandToken, String arguments) {
    }
}
