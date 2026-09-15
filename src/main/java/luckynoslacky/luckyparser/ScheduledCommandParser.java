package luckynoslacky.luckyparser;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import luckynoslacky.luckycommand.LuckyNoReschedCommand;
import luckynoslacky.luckycommand.LuckyNoSnoozeCommand;
import luckynoslacky.luckyexception.LuckyNoInputException;
import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TaskTimes;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Parses commands that change the schedule of existing timed tasks.
 */
final class ScheduledCommandParser {
    private final DateTimeParser dateTimeParser;
    private final DateTimePrefixParser dateTimePrefixParser;

    /**
     * Creates a scheduling-command parser with shared date/time dependencies.
     *
     * @param dateTimeParser parser used to resolve the current time
     * @param dateTimePrefixParser parser used to read date/time prefixes
     */
    ScheduledCommandParser(
            DateTimeParser dateTimeParser,
            DateTimePrefixParser dateTimePrefixParser) {
        if (dateTimeParser == null || dateTimePrefixParser == null) {
            throw new IllegalArgumentException("Date/time parsers cannot be null.");
        }
        this.dateTimeParser = dateTimeParser;
        this.dateTimePrefixParser = dateTimePrefixParser;
    }

    /**
     * Parses a snooze command for a timed task.
     *
     * @param commandArguments command arguments
     * @param taskCount current number of tasks
     * @param taskMaster task master attached to the command
     * @return parsed snooze command
     * @throws LuckyNoInputException if the format or task number is invalid
     */
    LuckyNoSnoozeCommand parseSnooze(
            String commandArguments,
            int taskCount,
            TaskMaster taskMaster)
            throws LuckyNoInputException {
        String[] parts = CommandArgumentParser.splitTaskNumber(commandArguments);
        int taskNumber = CommandArgumentParser.parseTaskNumber(parts[0], taskCount);
        rejectTodoTask(taskNumber, taskMaster, LuckyNoParser.CommandName.SNOOZE);

        if (parts[1].isEmpty()) {
            return new LuckyNoSnoozeCommand(taskNumber, taskMaster);
        }
        if (CommandArgumentParser.startsWithMarker(parts[1], "/by")) {
            String durationText = CommandArgumentParser.markerValue(
                    parts[1], "/by", LuckyNoParser.CommandName.SNOOZE,
                    LuckyNoMessages.snoozeByFormat(), LuckyNoMessages.snoozeToFormat());
            DurationPeriod amount = DurationParser.parse(durationText);
            validateSnoozeAmount(taskNumber, amount, taskMaster);
            return new LuckyNoSnoozeCommand(
                    taskNumber, amount, taskMaster);
        }
        if (CommandArgumentParser.startsWithMarker(parts[1], "/to")) {
            String endTimeText = CommandArgumentParser.markerValue(
                    parts[1], "/to", LuckyNoParser.CommandName.SNOOZE,
                    LuckyNoMessages.snoozeByFormat(), LuckyNoMessages.snoozeToFormat());
            LocalDateTime endTime = dateTimePrefixParser
                    .parseEndDateTimeIgnoringTrailingText(endTimeText);
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
     * @param taskMaster task master attached to the command
     * @return parsed rescheduling command
     * @throws LuckyNoInputException if the format, task number, or times are invalid
     */
    LuckyNoReschedCommand parseResched(
            String commandArguments,
            int taskCount,
            TaskMaster taskMaster)
            throws LuckyNoInputException {
        String[] parts = CommandArgumentParser.splitTaskNumber(commandArguments);
        int taskNumber = CommandArgumentParser.parseTaskNumber(parts[0], taskCount);
        Task.TaskType taskType = taskMaster.getTaskType(taskNumber);

        if (taskType == Task.TaskType.TODO) {
            rejectTodoTask(taskNumber, taskMaster, LuckyNoParser.CommandName.RESCHED);
        }
        if (taskType == Task.TaskType.DEADLINE
                && CommandArgumentParser.startsWithMarker(parts[1], "/to")) {
            String endTimeText = CommandArgumentParser.markerValue(
                    parts[1], "/to", LuckyNoParser.CommandName.RESCHED,
                    LuckyNoMessages.reschedDeadlineFormat());
            LocalDateTime endTime = dateTimePrefixParser
                    .parseEndDateTimeIgnoringTrailingText(endTimeText);
            if (endTime.isBefore(dateTimeParser.now())) {
                throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
            }
            return new LuckyNoReschedCommand(
                    taskNumber,
                    TaskTimes.makeDeadlineTimes(endTime),
                    taskMaster);
        }

        if (taskType == Task.TaskType.EVENT) {
            return parseEventResched(parts[1], taskNumber, taskMaster);
        }
        throw reschedFormat(taskType);
    }

    /**
     * Parses full or partial event rescheduling arguments.
     *
     * @param arguments event rescheduling arguments
     * @param taskNumber one-based task number
     * @param taskMaster task master containing the event
     * @return parsed event rescheduling command
     * @throws LuckyNoInputException if the markers, times, or final ordering
     *     are invalid
     */
    private LuckyNoReschedCommand parseEventResched(
            String arguments,
            int taskNumber,
            TaskMaster taskMaster)
            throws LuckyNoInputException {
        ReschedParts parts = parseEventReschedParts(arguments);
        LocalDateTime existingStart = taskMaster.getTaskStartTime(taskNumber);
        LocalDateTime existingEnd = taskMaster.getTaskEndTime(taskNumber);

        LocalDateTime startTime = parts.startTimeText() == null
                ? existingStart
                : dateTimePrefixParser
                .parseStartDateTimeIgnoringTrailingText(parts.startTimeText());
        LocalDateTime endTime = parts.endTimeText() == null
                ? existingEnd
                : dateTimePrefixParser
                .parseEndDateTimeIgnoringTrailingText(
                        parts.endTimeText(), startTime);

        if (endTime.isBefore(startTime)) {
            throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
        }
        return new LuckyNoReschedCommand(
                taskNumber,
                TaskTimes.makeEventTimes(startTime, endTime),
                taskMaster);
    }

    /**
     * Extracts the optional event rescheduling marker values.
     *
     * @param arguments event rescheduling arguments
     * @return extracted start and end time text
     * @throws LuckyNoInputException if the marker structure is invalid
     */
    private ReschedParts parseEventReschedParts(String arguments)
            throws LuckyNoInputException {
        int fromIndex = CommandArgumentParser.markerIndex(arguments, "/from", 0);
        int toIndex = CommandArgumentParser.markerIndex(arguments, "/to", 0);
        if (!hasValidEventMarkers(arguments, fromIndex, toIndex)) {
            throw reschedFormat(Task.TaskType.EVENT);
        }

        String startTimeText = fromIndex < 0
                ? null
                : extractMarkerSegment(
                        arguments, fromIndex, toIndex, "/from");
        String endTimeText = toIndex < 0
                ? null
                : extractMarkerSegment(
                        arguments, toIndex, fromIndex, "/to");
        return new ReschedParts(startTimeText, endTimeText);
    }

    /**
     * Checks the marker structure for an event rescheduling command.
     *
     * @param arguments event rescheduling arguments
     * @param fromIndex first {@code /from} marker index
     * @param toIndex first {@code /to} marker index
     * @return true if exactly the supported marker structure is present
     */
    private boolean hasValidEventMarkers(
            String arguments, int fromIndex, int toIndex) {
        if (fromIndex < 0 && toIndex < 0) {
            return false;
        }
        int firstMarkerIndex = fromIndex < 0
                ? toIndex
                : toIndex < 0
                ? fromIndex
                : Math.min(fromIndex, toIndex);
        if (firstMarkerIndex != 0) {
            return false;
        }
        return !hasDuplicateMarker(arguments, "/from", fromIndex)
                && !hasDuplicateMarker(arguments, "/to", toIndex);
    }

    /**
     * Checks whether a marker occurs more than once.
     *
     * @param text text containing markers
     * @param marker marker to check
     * @param firstIndex first marker index, or -1 when absent
     * @return true if a second marker is present
     */
    private boolean hasDuplicateMarker(
            String text, String marker, int firstIndex) {
        return firstIndex >= 0
                && CommandArgumentParser.markerIndex(
                text, marker, firstIndex + marker.length()) >= 0;
    }

    /**
     * Extracts one marker's value up to the other marker.
     *
     * @param text full marker argument text
     * @param markerIndex marker index
     * @param otherMarkerIndex other marker index
     * @param marker marker whose value is being extracted
     * @return marker value
     * @throws LuckyNoInputException if the value is empty or contains a slash
     */
    private String extractMarkerSegment(
            String text,
            int markerIndex,
            int otherMarkerIndex,
            String marker)
            throws LuckyNoInputException {
        int valueEnd = otherMarkerIndex > markerIndex
                ? otherMarkerIndex
                : text.length();
        return CommandArgumentParser.markerValue(
                text.substring(markerIndex, valueEnd),
                marker,
                LuckyNoParser.CommandName.RESCHED,
                LuckyNoMessages.reschedEventFormat());
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
            LuckyNoParser.CommandName commandName)
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
     * @throws LuckyNoInputException if the resulting time is invalid or overflows
     */
    private void validateSnoozeAmount(
            int taskNumber, DurationPeriod amount, TaskMaster taskMaster)
            throws LuckyNoInputException {
        try {
            LocalDateTime snoozedEndTime = amount.addTo(
                    taskMaster.getTaskEndTime(taskNumber));
            if (taskMaster.getTaskType(taskNumber) == Task.TaskType.DEADLINE
                    && snoozedEndTime.isBefore(dateTimeParser.now())) {
                throw new LuckyNoInputException(LuckyNoMessages.timeTravelMessage());
            }
        } catch (DateTimeException exception) {
            throw new LuckyNoInputException(LuckyNoMessages.snoozeOverflowMessage());
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
     * Creates an invalid snooze-format exception.
     *
     * @return invalid snooze-format exception
     */
    private LuckyNoInputException invalidSnoozeFormat() {
        return CommandArgumentParser.invalidFormat(
                LuckyNoParser.CommandName.SNOOZE,
                LuckyNoMessages.snoozeByFormat(),
                LuckyNoMessages.snoozeToFormat());
    }

    /**
     * Returns the invalid-format exception for a rescheduling task type.
     *
     * @param taskType selected task type
     * @return invalid-format exception
     */
    private LuckyNoInputException reschedFormat(Task.TaskType taskType) {
        return taskType == Task.TaskType.DEADLINE
                ? CommandArgumentParser.invalidFormat(
                LuckyNoParser.CommandName.RESCHED,
                LuckyNoMessages.reschedDeadlineFormat())
                : CommandArgumentParser.invalidFormat(
                LuckyNoParser.CommandName.RESCHED,
                LuckyNoMessages.reschedEventFormat());
    }

    /** Stores the optional marker values of an event rescheduling command. */
    private record ReschedParts(String startTimeText, String endTimeText) {
    }
}
