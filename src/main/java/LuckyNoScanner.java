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
     * @throws LuckyNoInputException if the input is invalid
     */
    public LuckyNoCommand parseCommand(String input, int taskCount)
            throws LuckyNoInputException {
        if (input == null || input.trim().isEmpty()) {
            throw new LuckyNoInputException(LuckyNoMessages.missingCommandMessage());
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
            throw new LuckyNoInputException(LuckyNoMessages.unknownCommandMessage());
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
