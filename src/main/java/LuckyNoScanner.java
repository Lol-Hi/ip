import java.util.Locale;

/**
 * Parses user input into commands and reports invalid input consistently.
 */
public class LuckyNoScanner {
    private static final String EMPTY_COMMAND_MESSAGE = "Please enter a command.";
    private static final String EMPTY_TASK_DESCRIPTION_MESSAGE =
            "Please provide a task description.";
    private static final String DEADLINE_FORMAT =
            "<description> /by <date/time>.";
    private static final String EVENT_FORMAT =
            "<description> /from <start> /to <end>.";

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
            throw new LuckyNoInputError(EMPTY_COMMAND_MESSAGE);
        }

        String trimmedInput = input.trim();
        String[] commandParts = trimmedInput.split("\\s+", 2);
        String command = commandParts[0].toLowerCase(Locale.ROOT);
        String arguments = commandParts.length == 2
                ? commandParts[1].trim()
                : "";

        switch (command) {
        case "bye":
            requireNoArguments(arguments, "bye");
            return new LuckyNoCommand("bye");
        case "list":
            requireNoArguments(arguments, "list");
            return new LuckyNoCommand("list");
        case "todo":
            if (arguments.isEmpty()) {
                throw new LuckyNoInputError(EMPTY_TASK_DESCRIPTION_MESSAGE);
            }
            return new LuckyNoTaskCommand(new TodoTask(arguments));
        case "deadline":
            return new LuckyNoTaskCommand(parseDeadline(arguments));
        case "event":
            return new LuckyNoTaskCommand(parseEvent(arguments));
        case "mark":
            return new LuckyNoMarkCommand(parseTaskNumber(
                    arguments, taskCount, "mark"), true);
        case "unmark":
            return new LuckyNoMarkCommand(parseTaskNumber(
                    arguments, taskCount, "unmark"), false);
        default:
            throw new LuckyNoInputError(unknownCommandMessage());
        }
    }

    private static String invalidFormatMessage(String command, String format) {
        return "Please use: " + command + " " + format;
    }

    private static String missingTaskNumberMessage(String command) {
        return "Please provide a task number to " + command + ".";
    }

    private static String extraArgumentsMessage(String command) {
        return "The " + command + " command does not take arguments.";
    }

    private static String invalidTaskNumberMessage() {
        return "That task number is invalid.";
    }

    private static String unknownCommandMessage() {
        return "I don't recognize that command. "
                + "Please use todo, deadline, event, list, mark, unmark, or bye.";
    }

    private void requireNoArguments(String arguments, String command)
            throws LuckyNoInputError {
        if (!arguments.isEmpty()) {
            throw new LuckyNoInputError(extraArgumentsMessage(command));
        }
    }

    private int parseTaskNumber(String arguments, int taskCount, String missingNumberMessage)
            throws LuckyNoInputError {
        if (arguments.isEmpty() || arguments.matches(".*\\s+.*")) {
            throw new LuckyNoInputError(missingTaskNumberMessage(missingNumberMessage));
        }

        try {
            int taskNumber = Integer.parseInt(arguments);
            if (taskNumber < 1 || taskNumber > taskCount) {
                throw new NumberFormatException();
            }
            return taskNumber;
        } catch (NumberFormatException exception) {
            throw new LuckyNoInputError(invalidTaskNumberMessage());
        }
    }

    private DeadlineTask parseDeadline(String arguments) throws LuckyNoInputError {
        int byIndex = arguments.indexOf("/by");
        if (byIndex <= 0) {
            throw new LuckyNoInputError(
                    invalidFormatMessage("deadline", DEADLINE_FORMAT));
        }

        String description = arguments.substring(0, byIndex).trim();
        String byTime = arguments.substring(byIndex + 3).trim();
        if (description.isEmpty() || byTime.isEmpty()) {
            throw new LuckyNoInputError(
                    invalidFormatMessage("deadline", DEADLINE_FORMAT));
        }
        return new DeadlineTask(description, byTime);
    }

    private EventTask parseEvent(String arguments) throws LuckyNoInputError {
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");
        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new LuckyNoInputError(invalidFormatMessage("event", EVENT_FORMAT));
        }

        String description = arguments.substring(0, fromIndex).trim();
        String fromTime = arguments.substring(fromIndex + 5, toIndex).trim();
        String toTime = arguments.substring(toIndex + 3).trim();
        if (description.isEmpty() || fromTime.isEmpty() || toTime.isEmpty()) {
            throw new LuckyNoInputError(invalidFormatMessage("event", EVENT_FORMAT));
        }
        return new EventTask(description, fromTime, toTime);
    }
}
