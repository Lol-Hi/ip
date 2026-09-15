package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Represents the common state and behavior shared by all task types.
 */
public abstract class Task {
    /** Identifies the concrete task category used by command validation. */
    public enum TaskType {
        /** A task without a date or time. */
        TODO,
        /** A task with a single completion time. */
        DEADLINE,
        /** A task with a start and end time. */
        EVENT
    }

    /**
     * Represents the completion state of a task.
     */
    public enum TaskStatus {
        /** Represents a task that has not been completed. */
        NOT_DONE,
        /** Represents a task that has been completed. */
        DONE
    }

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
     * Checks whether this task is done.
     *
     * @return true if the task is done
     */
    public boolean isDone() {
        return status == TaskStatus.DONE;
    }

    /**
     * Returns this task's description without display formatting.
     *
     * @return task description
     */
    public String getDescription() {
        return description;
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
     * Returns the concrete category of this task.
     *
     * @return task category
     */
    public abstract TaskType getTaskType();

    /**
     * Checks whether this task occurs on a date.
     *
     * @param date date to check
     * @return true if this task should appear in a date search
     */
    public final boolean occursOn(LocalDate date) {
        return getTaskTimes().occursOn(date);
    }

    /**
     * Extends this task's ending time by the supplied duration.
     *
     * @param amount duration by which to extend the ending time
     * @throws IllegalArgumentException if this task cannot be snoozed or the
     *                                  duration is invalid
     */
    public abstract void snoozeBy(DurationPeriod amount);

    /**
     * Returns this task's current timing information.
     *
     * @return task timing information
     */
    public abstract TaskTimes getTaskTimes();

    /**
     * Replaces this task's schedule.
     *
     * @param newTimes replacement task timing information
     * @throws IllegalArgumentException if the schedule is invalid for this
     *                                  task type
     */
    public abstract void reschedule(TaskTimes newTimes);

    /**
     * Returns this task's ending time.
     *
     * @return ending time
     * @throws IllegalArgumentException if this task has no ending time
     */
    public abstract LocalDateTime getEndTime();

}
