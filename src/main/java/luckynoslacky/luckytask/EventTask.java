package luckynoslacky.luckytask;

import java.time.LocalDateTime;
import java.util.List;

import luckynoslacky.luckyparser.DurationPeriod;

/**
 * Represents a task with a specified start and end time.
 */
public class EventTask extends Task {
    private TaskTimes times;

    /**
     * Creates an incomplete event task.
     *
     * @param description task description
     * @param startTime event start date and time
     * @param endTime event end date and time
     */
    public EventTask(String description, LocalDateTime startTime, LocalDateTime endTime) {
        this(description, TaskTimes.makeEventTimes(startTime, endTime));
    }

    /**
     * Creates an incomplete event task from validated timing information.
     *
     * @param description task description
     * @param times event timing information
     * @throws IllegalArgumentException if the timing information is not an
     *                                  event schedule
     */
    public EventTask(String description, TaskTimes times) {
        super(description);

        TaskTimes.verifyEventTimes(times);
        this.times = TaskTimes.makeEventTimes(
                times.getStartTime(), times.getEndTime());
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
        return times.getStartTime();
    }

    /**
     * Returns the current event end time.
     *
     * @return event end date and time
     */
    public LocalDateTime getEndTime() {
        return times.getEndTime();
    }

    /**
     * Returns this event's timing information.
     *
     * @return event timing information
     */
    @Override
    public TaskTimes getTaskTimes() {
        return times;
    }

    /**
     * Extends the event's end time by the supplied amount.
     *
     * @param amount amount by which to extend the event
     * @throws IllegalArgumentException if {@code amount} is null
     */
    @Override
    public void snoozeBy(DurationPeriod amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Snooze amount cannot be null.");
        }
        reschedule(TaskTimes.makeEventTimes(
                getStartTime(), amount.addTo(getEndTime())));
    }

    /**
     * Replaces both event times after validating the supplied schedule.
     *
     * @param newTimes replacement event timing information
     * @throws IllegalArgumentException if the timing information is not an
     *                                  event schedule
     */
    @Override
    public void reschedule(TaskTimes newTimes) {
        TaskTimes.verifyEventTimes(newTimes);
        times = TaskTimes.makeEventTimes(
                newTimes.getStartTime(), newTimes.getEndTime());
    }

    /**
     * Returns the CSV fields for this event.
     *
     * @return event fields in CSV column order
     */
    @Override
    public List<String> getCsvStorageFields() {
        return createCsvStorageFields(
                'E', times.getStartTime(), times.getEndTime());
    }

    /**
     * Returns the event display representation.
     *
     * @return event type marker, common task representation, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + Task.formatDateTime(getStartTime())
                + " to: " + Task.formatDateTime(getEndTime()) + ")";
    }
}
