package luckynoslacky.luckyparser;

import java.time.DateTimeException;
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
import luckynoslacky.luckycommand.LuckyNoReschedCommand;
import luckynoslacky.luckycommand.LuckyNoSnoozeCommand;
import luckynoslacky.luckycommand.LuckyNoTaskCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.Task;
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
        FIND("find"),
        /** Task-snoozing command. */
        SNOOZE("snooze"),
        /** Task-rescheduling command. */
        RESCHED("resched");

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
            case SNOOZE:
                return parseSnooze(commandArguments, taskCount, commandTaskMaster);
            case RESCHED:
                return parseResched(commandArguments, taskCount, commandTaskMaster);
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
     * Parses a snooze command for a timed task.
     *
     * @param commandArguments command arguments
     * @param taskCount current number of tasks
     * @param taskMaster task master attached to the parsed command
     * @return parsed snooze command
     * @throws LuckyNoInputException if the format or task number is invalid
     */
    private LuckyNoSnoozeCommand parseSnooze(
            String commandArguments,
            int taskCount,
            TaskMaster taskMaster)
            throws LuckyNoInputException {
        String[] parts = splitTaskNumber(commandArguments);
        int taskNumber = parseTaskNumber(parts[0], taskCount);
        rejectTodoTask(taskNumber, taskMaster, CommandName.SNOOZE);

        if (parts[1].isEmpty()) {
            return new LuckyNoSnoozeCommand(taskNumber, taskMaster);
        }
        if (startsWithMarker(parts[1], "/by")) {
            String durationText = markerValue(
                    parts[1], "/by", CommandName.SNOOZE,
                    LuckyNoMessages.snoozeByFormat(), LuckyNoMessages.snoozeToFormat());
            DurationPeriod amount = DurationParser.parse(durationText);
            validateSnoozeAmount(taskNumber, amount, taskMaster);
            return new LuckyNoSnoozeCommand(
                    taskNumber, amount, taskMaster);
        }
        if (startsWithMarker(parts[1], "/to")) {
            String endTimeText = markerValue(
                    parts[1], "/to", CommandName.SNOOZE,
                    LuckyNoMessages.snoozeByFormat(), LuckyNoMessages.snoozeToFormat());
            LocalDateTime endTime = parseEndDateTimeIgnoringTrailingText(endTimeText);
            validateDeadlineOrEventEnd(taskNumber, endTime, taskMaster);
            return new LuckyNoSnoozeCommand(taskNumber, endTime, taskMaster);
        }
        throw invalidSnoozeFormat();
    }

    /**
     * Parses a rescheduling command after identifying the task category.
     *
     * @param commandArguments command arguments
     * @param taskCount current number of tasks
     * @param taskMaster task master attached to the parsed command
     * @return parsed rescheduling command
     * @throws LuckyNoInputException if the format, task number, or times are invalid
     */
    private LuckyNoReschedCommand parseResched(
            String commandArguments,
            int taskCount,
            TaskMaster taskMaster)
            throws LuckyNoInputException {
        String[] parts = splitTaskNumber(commandArguments);
        int taskNumber = parseTaskNumber(parts[0], taskCount);
        Task.TaskType taskType = taskMaster.getTaskType(taskNumber);

        if (taskType == Task.TaskType.TODO) {
            rejectTodoTask(taskNumber, taskMaster, CommandName.RESCHED);
        }
        if (taskType == Task.TaskType.DEADLINE && startsWithMarker(parts[1], "/to")) {
            String endTimeText = markerValue(
                    parts[1], "/to", CommandName.RESCHED,
                    LuckyNoMessages.reschedDeadlineFormat());
            LocalDateTime endTime = parseEndDateTimeIgnoringTrailingText(endTimeText);
            if (endTime.isBefore(dateTimeParser.now())) {
                throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
            }
            return new LuckyNoReschedCommand(taskNumber, endTime, taskMaster);
        }

        if (taskType == Task.TaskType.EVENT && startsWithMarker(parts[1], "/from")) {
            int toIndex = markerIndex(parts[1], "/to", 5);
            if (toIndex < 0) {
                throw reschedFormat(taskType);
            }
            String startTimeText = parts[1].substring(5, toIndex).trim();
            String endTimeText = markerValue(
                    parts[1].substring(toIndex), "/to", CommandName.RESCHED,
                    LuckyNoMessages.reschedEventFormat());
            if (startTimeText.isEmpty() || endTimeText.isEmpty()) {
                throw reschedFormat(taskType);
            }
            if (startTimeText.contains("/")) {
                throw reschedFormat(taskType);
            }
            LocalDateTime startTime = parseStartDateTimeIgnoringTrailingText(
                    startTimeText);
            LocalDateTime endTime = parseEndDateTimeIgnoringTrailingText(
                    endTimeText, startTime);
            if (endTime.isBefore(startTime)) {
                throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
            }
            return new LuckyNoReschedCommand(
                    taskNumber, startTime, endTime, taskMaster);
        }
        throw reschedFormat(taskType);
    }

    /**
     * Splits a command's first task number from its remaining arguments.
     *
     * @param commandArguments command arguments
     * @return task-number text and remaining arguments
     */
    private String[] splitTaskNumber(String commandArguments) {
        if (commandArguments == null || commandArguments.isBlank()) {
            return new String[]{"", ""};
        }
        String[] parts = commandArguments.trim().split("\\s+", 2);
        return parts.length == 1
                ? new String[]{parts[0], ""}
                : new String[]{parts[0], parts[1].trim()};
    }

    /**
     * Checks whether an argument starts with a complete syntax marker.
     *
     * @param text argument text
     * @param marker marker to find
     * @return true if the marker is at the beginning of the text
     */
    private boolean startsWithMarker(String text, String marker) {
        return text.length() >= marker.length()
                && text.regionMatches(true, 0, marker, 0, marker.length())
                && (text.length() == marker.length()
                || Character.isWhitespace(text.charAt(marker.length())));
    }

    /**
     * Extracts the value following a syntax marker and rejects extra markers.
     *
     * @param text marker and value text
     * @param marker marker to remove
     * @param commandName command being parsed
     * @param validFormats valid formats for the command
     * @return marker value
     * @throws LuckyNoInputException if the value is empty or contains a slash
     */
    private String markerValue(
            String text,
            String marker,
            CommandName commandName,
            String... validFormats)
            throws LuckyNoInputException {
        if (!startsWithMarker(text, marker)) {
            throw invalidFormat(commandName, validFormats);
        }
        String value = text.substring(marker.length()).trim();
        if (value.isEmpty()) {
            throw invalidFormat(commandName, validFormats);
        }
        if (value.contains("/")) {
            throw invalidFormat(commandName, validFormats);
        }
        return value;
    }

    /**
     * Finds a marker after a specified character offset.
     *
     * @param text text to search
     * @param marker marker to find
     * @param fromIndex first index to inspect
     * @return marker index, or -1 when absent
     */
    private int markerIndex(String text, String marker, int fromIndex) {
        String normalizedText = text.toLowerCase(Locale.ROOT);
        String normalizedMarker = marker.toLowerCase(Locale.ROOT);
        int index = normalizedText.indexOf(normalizedMarker, fromIndex);
        while (index >= 0 && index > 0
                && !Character.isWhitespace(text.charAt(index - 1))) {
            index = normalizedText.indexOf(normalizedMarker, index + 1);
        }
        return index;
    }

    /**
     * Rejects commands that target a ToDo task.
     *
     * @param taskNumber one-based task number
     * @param taskMaster task master containing the task
     * @param commandName snooze or reschedule command
     * @throws LuckyNoInputException if the selected task is a ToDo
     */
    private void rejectTodoTask(
            int taskNumber,
            TaskMaster taskMaster,
            CommandName commandName)
            throws LuckyNoInputException {
        if (taskMaster.getTaskType(taskNumber) == Task.TaskType.TODO) {
            throw new LuckyNoInputException(
                    LuckyNoMessages.cannotSnoozeOrRescheduleTodoMessage(commandName));
        }
    }

    /**
     * Validates the result of a duration-based snooze.
     *
     * @param taskNumber one-based task number
     * @param amount snooze amount
     * @param taskMaster task master containing the task
     * @throws LuckyNoInputException if a deadline would remain in the past
     */
    private void validateSnoozeAmount(
            int taskNumber, DurationPeriod amount, TaskMaster taskMaster)
            throws LuckyNoInputException {
        if (taskMaster.getTaskType(taskNumber) == Task.TaskType.DEADLINE) {
            try {
                if (amount.addTo(taskMaster.getTaskEndTime(taskNumber))
                        .isBefore(dateTimeParser.now())) {
                    throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
                }
            } catch (DateTimeException exception) {
                throw invalidSnoozeFormat();
            }
        }
    }

    /**
     * Validates a replacement ending time against the selected task.
     *
     * @param taskNumber one-based task number
     * @param endTime replacement ending time
     * @param taskMaster task master containing the task
     * @throws LuckyNoInputException if the replacement violates time ordering
     */
    private void validateDeadlineOrEventEnd(
            int taskNumber, LocalDateTime endTime, TaskMaster taskMaster)
            throws LuckyNoInputException {
        Task.TaskType taskType = taskMaster.getTaskType(taskNumber);
        if (taskType == Task.TaskType.DEADLINE
                && endTime.isBefore(dateTimeParser.now())) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
        if (taskType == Task.TaskType.EVENT
                && endTime.isBefore(taskMaster.getTaskStartTime(taskNumber))) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
    }

    /**
     * Parses an end datetime while ignoring trailing commentary.
     *
     * @param dateTimeText datetime text
     * @return parsed datetime
     * @throws LuckyNoInputException if no prefix is a valid datetime
     */
    private LocalDateTime parseEndDateTimeIgnoringTrailingText(String dateTimeText)
            throws LuckyNoInputException {
        return parseDateTimePrefix(dateTimeText, null, false);
    }

    /**
     * Parses an event end datetime relative to its new start time.
     *
     * @param dateTimeText datetime text
     * @param referenceDateTime event start datetime
     * @return parsed datetime
     * @throws LuckyNoInputException if no prefix is a valid datetime
     */
    private LocalDateTime parseEndDateTimeIgnoringTrailingText(
            String dateTimeText, LocalDateTime referenceDateTime)
            throws LuckyNoInputException {
        return parseDateTimePrefix(dateTimeText, referenceDateTime, false);
    }

    /**
     * Parses an event start datetime while ignoring trailing commentary.
     *
     * @param dateTimeText datetime text
     * @return parsed datetime
     * @throws LuckyNoInputException if no prefix is a valid datetime
     */
    private LocalDateTime parseStartDateTimeIgnoringTrailingText(String dateTimeText)
            throws LuckyNoInputException {
        return parseDateTimePrefix(dateTimeText, null, true);
    }

    /**
     * Tries progressively shorter prefixes until one parses as a datetime.
     *
     * @param dateTimeText datetime text
     * @param referenceDateTime reference for an event end, or null
     * @param isStart whether the input is an event start
     * @return parsed datetime
     * @throws LuckyNoInputException if no prefix parses successfully
     */
    private LocalDateTime parseDateTimePrefix(
            String dateTimeText,
            LocalDateTime referenceDateTime,
            boolean isStart)
            throws LuckyNoInputException {
        String[] words = dateTimeText.trim().split("\\s+");
        for (int wordCount = words.length; wordCount > 0; wordCount--) {
            String candidate = String.join(
                    " ", Arrays.copyOf(words, wordCount));
            try {
                DateTimeParser.ParsedDateTime parsed = isStart
                        ? dateTimeParser.parseStartDateTime(candidate)
                        : referenceDateTime == null
                        ? dateTimeParser.parseEndDateTime(candidate)
                        : dateTimeParser.parseEndDateTime(candidate, referenceDateTime);
                return parsed.dateTime();
            } catch (LuckyNoInputException exception) {
                // Try a shorter prefix so valid trailing commentary is ignored.
            }
        }
        throw new LuckyNoInputException(LuckyNoMessages.invalidDateTimeMessage());
    }

    /**
     * Creates an invalid snooze-format exception.
     *
     * @return invalid snooze-format exception
     */
    private LuckyNoInputException invalidSnoozeFormat() {
        return invalidFormat(
                CommandName.SNOOZE,
                LuckyNoMessages.snoozeByFormat(),
                LuckyNoMessages.snoozeToFormat());
    }

    /**
     * Creates an invalid format exception with the supplied alternatives.
     *
     * @param commandName command being parsed
     * @param formats valid command formats
     * @return invalid-format exception
     */
    private LuckyNoInputException invalidFormat(
            CommandName commandName, String... formats) {
        return new LuckyNoInputException(
                LuckyNoMessages.invalidFormatMessage(commandName, formats));
    }

    /**
     * Returns the invalid-format exception for a rescheduling task type.
     *
     * @param taskType selected task type
     * @return invalid-format exception
     */
    private LuckyNoInputException reschedFormat(Task.TaskType taskType) {
        return taskType == Task.TaskType.DEADLINE
                ? invalidFormat(CommandName.RESCHED,
                LuckyNoMessages.reschedDeadlineFormat())
                : invalidFormat(CommandName.RESCHED,
                LuckyNoMessages.reschedEventFormat());
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
