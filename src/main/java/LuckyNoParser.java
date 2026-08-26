import java.util.Locale;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Parses user input into commands and reports invalid input consistently.
 */
public class LuckyNoParser {
    private final DateTimeParser dateTimeParser;
    private final TaskMaster taskMaster;

    /** Creates a scanner using the current system clock. */
    public LuckyNoParser() {
        this(new DateTimeParser(), new TaskMaster());
    }

    LuckyNoParser(DateTimeParser dateTimeParser) {
        this(dateTimeParser, new TaskMaster());
    }

    LuckyNoParser(TaskMaster taskMaster) {
        this(new DateTimeParser(), taskMaster);
    }

    LuckyNoParser(DateTimeParser dateTimeParser, TaskMaster taskMaster) {
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
        BYE("bye"),
        LIST("list"),
        TODO("todo"),
        DEADLINE("deadline"),
        EVENT("event"),
        MARK("mark"),
        UNMARK("unmark"),
        DELETE("delete"),
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
         * @param input command token
         * @return matching command, or an empty Optional if there is no match
         */
        public static Optional<CommandName> fromInput(String input) {
            for (CommandName commandName : values()) {
                if (commandName.inputName.equalsIgnoreCase(input)) {
                    return Optional.of(commandName);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * Parses one line of chatbot input.
     *
     * @param input raw user input
     * @param taskCount number of tasks currently stored
     * @return parsed command
     * @throws LuckyNoInputException if the input is invalid
     */
    public LuckyNoCommand parseCommand(String input, int taskCount)
            throws LuckyNoInputException {
        return parseCommand(input, taskCount, taskMaster);
    }

    /**
     * Parses input using the task master's current task count.
     *
     * @param input raw user input
     * @return parsed command
     * @throws LuckyNoInputException if the input is invalid
     */
    public LuckyNoCommand parseCommand(String input)
            throws LuckyNoInputException {
        return parseCommand(input, taskMaster.getTaskCount(), taskMaster);
    }

    private LuckyNoCommand parseCommand(
            String input,
            int taskCount,
            TaskMaster commandTaskMaster)
            throws LuckyNoInputException {
        if (input == null || input.trim().isEmpty()) {
            throw new LuckyNoInputException(LuckyNoMessages.missingCommandMessage());
        }

        String trimmedInput = input.trim();
        String[] commandParts = trimmedInput.split("\\s+", 2);
        String commandToken = commandParts[0].toLowerCase(Locale.ROOT);
        String arguments = commandParts.length == 2
                ? commandParts[1].trim()
                : "";

        CommandName commandName = CommandName.fromInput(commandToken)
                .orElseThrow(() -> new LuckyNoInputException(
                        LuckyNoMessages.unknownCommandMessage()));

        switch (commandName) {
        case BYE:
            checkNoArguments(arguments, CommandName.BYE.getInputName());
            return new LuckyNoByeCommand();
        case LIST:
            checkNoArguments(arguments, CommandName.LIST.getInputName());
            return new LuckyNoListCommand(commandTaskMaster);
        case TODO:
            return new LuckyNoTaskCommand(parseTodo(arguments), commandTaskMaster);
        case DEADLINE:
            return new LuckyNoTaskCommand(
                    parseDeadline(arguments), commandTaskMaster);
        case EVENT:
            return new LuckyNoTaskCommand(parseEvent(arguments), commandTaskMaster);
        case MARK:
            return new LuckyNoMarkCommand(
                    parseTaskNumber(arguments, taskCount), true, commandTaskMaster);
        case UNMARK:
            return new LuckyNoMarkCommand(
                    parseTaskNumber(arguments, taskCount), false, commandTaskMaster);
        case DELETE:
            return new LuckyNoDeleteCommand(
                    parseTaskNumber(arguments, taskCount), commandTaskMaster);
        case FIND:
            return parseFind(arguments, commandTaskMaster);
        default:
            throw new IllegalStateException("Unhandled command name.");
        }
    }

    private void checkNoArguments(String arguments, String command)
            throws LuckyNoInputException {
        if (!arguments.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.extraArgumentsMessage(command));
        }
    }

    private int parseTaskNumber(String arguments, int taskCount)
            throws LuckyNoInputException {
        if (arguments.isEmpty() || arguments.matches(".*\\s+.*")) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.missingTaskNumberMessage());
        }

        try {
            int taskNumber = Integer.parseInt(arguments);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new NumberFormatException();
            }
            return taskNumber;
        } catch (NumberFormatException exception) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidTaskNumberMessage());
        }
    }

    private TodoTask parseTodo(String arguments) throws LuckyNoInputException {
        if (arguments.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.missingTaskDescriptionMessage());
        }
        return new TodoTask(arguments);
    }

    private DeadlineTask parseDeadline(String arguments) throws LuckyNoInputException {
        int byIndex = arguments.indexOf("/by");
        if (byIndex <= 0) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.DEADLINE));
        }

        String description = arguments.substring(0, byIndex).trim();
        String byTimeText = arguments.substring(byIndex + 3).trim();
        if (description.isEmpty() || byTimeText.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.DEADLINE));
        }
        LocalDateTime byTime = dateTimeParser.parseEndDateTime(byTimeText).value();
        if (byTime.isBefore(dateTimeParser.now())) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
        return new DeadlineTask(description, byTime);
    }

    /**
     * Parses a find command. Text before the {@code /on} tag is intentionally
     * ignored so that the command can be extended with more search options.
     */
    private LuckyNoFindCommand parseFind(String arguments, TaskMaster taskMaster)
            throws LuckyNoInputException {
        int onIndex = arguments.indexOf("/on");
        if (onIndex < 0) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.FIND));
        }

        String dateText = arguments.substring(onIndex + 3).trim();
        if (dateText.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.FIND));
        }

        LocalDateTime searchDateTime =
                dateTimeParser.parseStartDateTime(dateText).value();
        return new LuckyNoFindCommand(searchDateTime, taskMaster);
    }

    private EventTask parseEvent(String arguments) throws LuckyNoInputException {
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");
        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.EVENT));
        }

        String description = arguments.substring(0, fromIndex).trim();
        String fromTimeText = arguments.substring(fromIndex + 5, toIndex).trim();
        String toTimeText = arguments.substring(toIndex + 3).trim();
        if (description.isEmpty() || fromTimeText.isEmpty() || toTimeText.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            CommandName.EVENT));
        }
        DateTimeParser.ParsedDateTime fromTime =
                dateTimeParser.parseStartDateTime(fromTimeText);
        DateTimeParser.ParsedDateTime toTime =
                dateTimeParser.parseEndDateTime(toTimeText, fromTime.value());
        if (toTime.value().isBefore(fromTime.value())) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
        return new EventTask(description, fromTime.value(), toTime.value());
    }
}
