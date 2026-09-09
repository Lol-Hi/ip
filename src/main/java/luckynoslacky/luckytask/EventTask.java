package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

/**
 * Represents a task with a specified start and end time.
 */
public class EventTask extends Task {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /**
     * Creates an incomplete event task.
     *
     * @param description task description
     * @param startTime event start date and time
     * @param endTime event end date and time
     */
    public EventTask(String description, LocalDateTime startTime, LocalDateTime endTime) {
        super(description);

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Event times cannot be empty.");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Event end cannot be before its start.");
        }

        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the category of this task.
     *
     * @return event task category
     */
    @Override
    public TaskType getTaskType() {
        return TaskType.EVENT;
    }

    /**
     * Returns the current event start time.
     *
     * @return event start date and time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Returns the current event end time.
     *
     * @return event end date and time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Extends the event's end time by the supplied amount.
     *
     * @param amount amount by which to extend the event
     * @throws IllegalArgumentException if {@code amount} is null
     */
    public void snoozeBy(TemporalAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Snooze amount cannot be null.");
        }
        reschedule(startTime, endTime.plus(amount));
    }

    /**
     * Replaces both event times after validating their ordering.
     *
     * @param newStartTime replacement start time
     * @param newEndTime replacement end time
     * @throws IllegalArgumentException if either time is null or the end is
     *                                  before the start
     */
    public void reschedule(LocalDateTime newStartTime, LocalDateTime newEndTime) {
        if (newStartTime == null || newEndTime == null) {
            throw new IllegalArgumentException("Event times cannot be empty.");
        }
        if (newEndTime.isBefore(newStartTime)) {
            throw new IllegalArgumentException("Event end cannot be before its start.");
        }
        startTime = newStartTime;
        endTime = newEndTime;
    }

    /**
     * Returns the CSV fields for this event.
     *
     * @return event fields in CSV column order
     */
    @Override
    public List<String> getCsvStorageFields() {
        return createCsvStorageFields('E', startTime, endTime);
    }

    /**
     * Checks whether the event spans the supplied date.
     *
     * @param date date to check
     * @return true if the event occurs on the supplied date
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(startTime.toLocalDate())
                && !date.isAfter(endTime.toLocalDate());
    }

    /**
     * Returns the event display representation.
     *
     * @return event type marker, common task representation, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + Task.formatDateTime(startTime)
                + " to: " + Task.formatDateTime(endTime) + ")";
    }
}
