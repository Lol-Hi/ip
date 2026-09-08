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
    /**
     * Represents the completion state of a task and its external
     * representations.
     */
    public enum TaskStatus {
        /** Represents a task that has not been completed. */
        NOT_DONE(" ", "0"),
        /** Represents a task that has been completed. */
        DONE("X", "1");

        private final String displayIcon;
        private final String storageValue;

        /**
         * Creates a task status with its display and storage representations.
         *
         * @param displayIcon icon used in task displays
         * @param storageValue value used in CSV storage
         */
        TaskStatus(String displayIcon, String storageValue) {
            this.displayIcon = displayIcon;
            this.storageValue = storageValue;
        }

        /**
         * Returns the icon used when displaying this status.
         *
         * @return display icon
         */
        public String getDisplayIcon() {
            return displayIcon;
        }

        /**
         * Returns the value used to store this status in a CSV record.
         *
         * @return CSV completion value
         */
        public String getStorageValue() {
            return storageValue;
        }

        /**
         * Converts a CSV completion value into its corresponding task status.
         *
         * @param storageValue CSV completion value
         * @return status represented by the storage value
         * @throws IllegalArgumentException if the storage value is not {@code 0} or
         *                                  {@code 1}
         */
        public static TaskStatus fromStorageValue(String storageValue) {
            if (storageValue == null) {
                throw new IllegalArgumentException(
                        "Completion status cannot be null.");
            }

            return switch (storageValue) {
                case "0" -> NOT_DONE;
                case "1" -> DONE;
                default -> throw new IllegalArgumentException(
                        "Invalid completion status: " + storageValue);
            };
        }
    }

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd uuuu, h.mma", Locale.ENGLISH);
    private final String description;
    private TaskStatus status;

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
        this.status = TaskStatus.NOT_DONE;
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        status = TaskStatus.DONE;
    }

    /**
     * Marks this task as not done.
     */
    public void unmarkAsUndone() {
        status = TaskStatus.NOT_DONE;
    }

    /**
     * Gets the status icon used when displaying this task.
     *
     * @return X for a done task, or a space for an incomplete task
     */
    public String getStatusIcon() {
        return status.getDisplayIcon();
    }

    /**
     * Checks whether this task is done.
     *
     * @return true if the task is done
     */
    public boolean isDone() {
        return status == TaskStatus.DONE;
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
     * @param endTime task end time, or an empty string when not applicable
     * @return fields in CSV column order
     */
    protected final List<String> createCsvStorageFields(
            char taskType,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        return List.of(
                String.valueOf(taskType),
                status.getStorageValue(),
                description,
                DateTimeParser.formatForStorage(startTime),
                DateTimeParser.formatForStorage(endTime));
    }

    /**
     * Formats a date and time using the chatbot's human-readable format.
     *
     * @param dateTime date and time to format
     * @return formatted date and time
     */
    protected static String formatDateTime(LocalDateTime dateTime) {
        return DISPLAY_FORMATTER.format(dateTime)
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
