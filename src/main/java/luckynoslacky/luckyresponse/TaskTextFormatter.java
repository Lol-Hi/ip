package luckynoslacky.luckyresponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskView;

/**
 * Formats structured task data for the command-line interface.
 */
final class TaskTextFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd uuuu, h.mma", Locale.ENGLISH);

    private TaskTextFormatter() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Formats one task without a task number.
     *
     * @param task task data to format
     * @return CLI representation of the task
     */
    static String formatTask(TaskView task) {
        return getTypeMarker(task.taskType())
                + "[" + getStatusMarker(task.isDone()) + "] "
                + task.description()
                + getScheduleText(task);
    }

    /**
     * Formats one task with its task number.
     *
     * @param task task data to format
     * @return CLI representation of the numbered task
     * @throws IllegalArgumentException if the task has no task number
     */
    static String formatNumberedTask(TaskView task) {
        if (task.taskNumber() == null) {
            throw new IllegalArgumentException("Task number cannot be null.");
        }
        return task.taskNumber() + "." + formatTask(task);
    }

    /** Returns the CLI marker for a task type. */
    private static String getTypeMarker(Task.TaskType taskType) {
        return switch (taskType) {
            case TODO -> "[T]";
            case DEADLINE -> "[D]";
            case EVENT -> "[E]";
        };
    }

    /** Returns the CLI marker for a task's completion status. */
    private static String getStatusMarker(boolean isDone) {
        return isDone ? "X" : " ";
    }

    /** Returns the optional CLI schedule suffix for a task. */
    private static String getScheduleText(TaskView task) {
        return switch (task.taskType()) {
            case TODO -> "";
            case DEADLINE -> " (by: "
                    + formatDateTime(task.taskTimes().getEndTime()) + ")";
            case EVENT -> " (from: "
                    + formatDateTime(task.taskTimes().getStartTime())
                    + " to: " + formatDateTime(task.taskTimes().getEndTime()) + ")";
        };
    }

    /** Formats a date and time with the established CLI convention. */
    private static String formatDateTime(LocalDateTime dateTime) {
        return DATE_TIME_FORMATTER.format(dateTime)
                .replace("AM", "am")
                .replace("PM", "pm");
    }
}
