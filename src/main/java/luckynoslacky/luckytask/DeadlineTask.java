package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Represents a task that must be completed by a specified time.
 */
public class DeadlineTask extends Task {
    private LocalDateTime byTime;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description task description
     * @param byTime deadline date and time
     */
    public DeadlineTask(String description, LocalDateTime byTime) {
        super(description);

        if (byTime == null) {
            throw new IllegalArgumentException("Deadline cannot be empty.");
        }

        this.byTime = byTime;
    }

    /**
     * Returns the category of this task.
     *
     * @return deadline task category
     */
    @Override
    public TaskType getTaskType() {
        return TaskType.DEADLINE;
    }

    /**
     * Returns the current deadline.
     *
     * @return deadline date and time
     */
    public LocalDateTime getByTime() {
        return byTime;
    }

    /**
     * Extends the deadline by the supplied amount.
     *
     * @param amount amount by which to extend the deadline
     * @throws IllegalArgumentException if {@code amount} is null
     */
    public void snoozeBy(TemporalAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Snooze amount cannot be null.");
        }
        byTime = byTime.plus(amount);
    }

    /**
     * Replaces the deadline.
     *
     * @param newByTime replacement deadline
     * @throws IllegalArgumentException if {@code newByTime} is null
     */
    public void rescheduleTo(LocalDateTime newByTime) {
        if (newByTime == null) {
            throw new IllegalArgumentException("Deadline cannot be empty.");
        }
        byTime = newByTime;
    }

    /**
     * Returns the CSV fields for this deadline.
     *
     * @return deadline fields in CSV column order
     */
    @Override
    public List<String> getCsvStorageFields() {
        return createCsvStorageFields('D', null, byTime);
    }

    /**
     * Checks whether the deadline falls on the supplied date.
     *
     * @param date date to check
     * @return true if the deadline is on the supplied date
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return byTime.toLocalDate().equals(date);
    }

    /**
     * Returns the deadline display representation.
     *
     * @return deadline type marker, common task representation, and deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + Task.formatDateTime(byTime) + ")";
    }
}
