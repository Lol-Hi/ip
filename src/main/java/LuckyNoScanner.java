import java.util.Locale;
import java.util.Optional;

/**
 * Parses user input into commands and reports invalid input consistently.
 */
public class LuckyNoScanner {
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
        DELETE("delete");

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
            return new LuckyNoCommand(LuckyNoCommand.CommandType.BYE);
        case LIST:
            checkNoArguments(arguments, CommandName.LIST.getInputName());
            return new LuckyNoCommand(LuckyNoCommand.CommandType.LIST);
        case TODO:
            return new LuckyNoTaskCommand(parseTodo(arguments));
        case DEADLINE:
            return new LuckyNoTaskCommand(parseDeadline(arguments));
        case EVENT:
            return new LuckyNoTaskCommand(parseEvent(arguments));
        case MARK:
            return new LuckyNoMarkCommand(parseTaskNumber(arguments, taskCount), true);
        case UNMARK:
            return new LuckyNoMarkCommand(parseTaskNumber(arguments, taskCount), false);
        case DELETE:
            return new LuckyNoDeleteCommand(parseTaskNumber(arguments, taskCount));
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
                            "deadline", LuckyNoMessages.deadlineFormat()));
        }

        String description = arguments.substring(0, byIndex).trim();
        String byTime = arguments.substring(byIndex + 3).trim();
        if (description.isEmpty() || byTime.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            "deadline", LuckyNoMessages.deadlineFormat()));
        }
        return new DeadlineTask(description, byTime);
    }

    private EventTask parseEvent(String arguments) throws LuckyNoInputException {
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");
        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            "event", LuckyNoMessages.eventFormat()));
        }

        String description = arguments.substring(0, fromIndex).trim();
        String fromTime = arguments.substring(fromIndex + 5, toIndex).trim();
        String toTime = arguments.substring(toIndex + 3).trim();
        if (description.isEmpty() || fromTime.isEmpty() || toTime.isEmpty()) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.invalidFormatMessage(
                            "event", LuckyNoMessages.eventFormat()));
        }
        return new EventTask(description, fromTime, toTime);
    }
}
