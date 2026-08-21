import java.util.Locale;

/**
 * Parses user input into commands and reports invalid input consistently.
 */
public class LuckyNoScanner {
    /**
     * Parses one line of chatbot input.
     *
     * @param input raw user input
     * @param taskCount number of tasks currently stored
     * @return parsed command
     * @throws LuckyNoInputError if the input is invalid
     */
    public LuckyNoCommand parseCommand(String input, int taskCount)
            throws LuckyNoInputError {
        if (input == null || input.trim().isEmpty()) {
            throw new LuckyNoInputError(LuckyNoMessages.missingCommandMessage());
        }

        String trimmedInput = input.trim();
        String[] commandParts = trimmedInput.split("\\s+", 2);
        String command = commandParts[0].toLowerCase(Locale.ROOT);
        String arguments = commandParts.length == 2
                ? commandParts[1].trim()
                : "";

        switch (command) {
        case "bye":
            checkNoArguments(arguments, "bye");
            return new LuckyNoCommand("bye");
        case "list":
            checkNoArguments(arguments, "list");
            return new LuckyNoCommand("list");
        case "todo":
            return new LuckyNoTaskCommand(parseTodo(arguments));
        case "deadline":
            return new LuckyNoTaskCommand(parseDeadline(arguments));
        case "event":
            return new LuckyNoTaskCommand(parseEvent(arguments));
        case "mark":
            return new LuckyNoMarkCommand(parseTaskNumber(arguments, taskCount), true);
        case "unmark":
            return new LuckyNoMarkCommand(parseTaskNumber(arguments, taskCount), false);
        default:
            throw new LuckyNoInputError(LuckyNoMessages.unknownCommandMessage());
        }
    }

    private void checkNoArguments(String arguments, String command)
            throws LuckyNoInputError {
        if (!arguments.isEmpty()) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.extraArgumentsMessage(command));
        }
    }

    private int parseTaskNumber(String arguments, int taskCount)
            throws LuckyNoInputError {
        if (arguments.isEmpty() || arguments.matches(".*\\s+.*")) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.missingTaskNumberMessage());
        }

        try {
            int taskNumber = Integer.parseInt(arguments);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new NumberFormatException();
            }
            return taskNumber;
        } catch (NumberFormatException exception) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.invalidTaskNumberMessage());
        }
    }

    private TodoTask parseTodo(String arguments) throws LuckyNoInputError {
        if (arguments.isEmpty()) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.missingTaskDescriptionMessage());
        }
        return new TodoTask(arguments);
    }

    private DeadlineTask parseDeadline(String arguments) throws LuckyNoInputError {
        int byIndex = arguments.indexOf("/by");
        if (byIndex <= 0) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.invalidFormatMessage(
                            "deadline", LuckyNoMessages.deadlineFormat()));
        }

        String description = arguments.substring(0, byIndex).trim();
        String byTime = arguments.substring(byIndex + 3).trim();
        if (description.isEmpty() || byTime.isEmpty()) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.invalidFormatMessage(
                            "deadline", LuckyNoMessages.deadlineFormat()));
        }
        return new DeadlineTask(description, byTime);
    }

    private EventTask parseEvent(String arguments) throws LuckyNoInputError {
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");
        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.invalidFormatMessage(
                            "event", LuckyNoMessages.eventFormat()));
        }

        String description = arguments.substring(0, fromIndex).trim();
        String fromTime = arguments.substring(fromIndex + 5, toIndex).trim();
        String toTime = arguments.substring(toIndex + 3).trim();
        if (description.isEmpty() || fromTime.isEmpty() || toTime.isEmpty()) {
            throw new LuckyNoInputError(
                    LuckyNoMessages.invalidFormatMessage(
                            "event", LuckyNoMessages.eventFormat()));
        }
        return new EventTask(description, fromTime, toTime);
    }
}
