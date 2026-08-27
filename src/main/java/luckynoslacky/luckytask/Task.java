package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import luckynoslacky.luckyparser.DateTimeParser;

/**
 * Represents the common state and behavior shared by all task types.
 */
public abstract class Task {
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd uuuu, h.mma", Locale.ENGLISH);
    private final String description;
    private boolean isDone;

    /**
     * Creates an incomplete task.
     *
     * @param description task description
     */
    protected Task(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be empty.");
        }

        this.description = description;
        this.isDone = false;
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void unmarkAsUndone() {
        isDone = false;
    }

    /**
     * Gets the status icon used when displaying this task.
     *
     * @return X for a done task, or a space for an incomplete task
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Checks whether this task is done.
     *
     * @return true if the task is done
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Checks whether this task's description contains a search query.
     * Matching is case-insensitive and uses substring matching.
     *
     * @param query text to search for in the description
     * @return true if the description contains the query
     */
    public boolean matchesDescription(String query) {
        return description.toLowerCase(Locale.ROOT)
                .contains(query.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the raw fields used to save this task as a CSV record.
     *
     * @return task fields in CSV column order
     */
    public abstract List<String> getCsvStorageFields();

    /**
     * Checks whether this task occurs on a date.
     *
     * @param date date to check
     * @return true if this task should appear in a date search
     */
    public abstract boolean occursOn(LocalDate date);

    /**
     * Creates the common CSV fields for a concrete task type.
     *
     * @param taskType task type marker
     * @param startTime task start time, or an empty string when not applicable
     * @param finishTime task finish time, or an empty string when not applicable
     * @return fields in CSV column order
     */
    protected final List<String> createCsvStorageFields(
            char taskType,
            LocalDateTime startTime,
            LocalDateTime finishTime) {
        return List.of(
                String.valueOf(taskType),
                isDone ? "1" : "0",
                description,
                DateTimeParser.formatForStorage(startTime),
                DateTimeParser.formatForStorage(finishTime));
    }

    /**
     * Formats a date and time using the chatbot's human-readable format.
     *
     * @param value date and time to format
     * @return formatted date and time
     */
    protected static String formatDateTime(LocalDateTime value) {
        return DISPLAY_FORMATTER.format(value)
                .replace("AM", "am")
                .replace("PM", "pm");
    }

    /**
     * Returns the common display representation of a task.
     *
     * @return status icon and task description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
