package luckynoslacky.luckytask;

import java.time.LocalDateTime;
import java.util.List;

import luckynoslacky.luckyparser.DurationPeriod;

/**
 * Represents a task that must be completed by a specified time.
 */
public class DeadlineTask extends Task {
    private TaskTimes times;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description task description
     * @param byTime deadline date and time
     */
    public DeadlineTask(String description, LocalDateTime byTime) {
        this(description, TaskTimes.makeDeadlineTimes(byTime));
    }

    /**
     * Creates an incomplete deadline task from validated timing information.
     *
     * @param description task description
     * @param times deadline timing information
     * @throws IllegalArgumentException if the timing information is not a
     *                                  deadline schedule
     */
    public DeadlineTask(String description, TaskTimes times) {
        super(description);

        TaskTimes.verifyDeadlineTimes(times);
        this.times = times;
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
        return times.getEndTime();
    }

    /**
     * Returns the deadline through the common timed-task interface.
     *
     * @return deadline date and time
     */
    @Override
    public LocalDateTime getEndTime() {
        return times.getEndTime();
    }

    /**
     * Returns this deadline's timing information.
     *
     * @return deadline timing information
     */
    @Override
    public TaskTimes getTaskTimes() {
        return times;
    }

    /**
     * Extends the deadline by the supplied amount.
     *
     * @param amount amount by which to extend the deadline
     * @throws IllegalArgumentException if {@code amount} is null
     */
    @Override
    public void snoozeBy(DurationPeriod amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Snooze amount cannot be null.");
        }
        reschedule(TaskTimes.makeDeadlineTimes(
                amount.addTo(getEndTime())));
    }

    /**
     * Replaces the deadline schedule.
     *
     * @param newTimes replacement deadline timing information
     * @throws IllegalArgumentException if the timing information is not a
     *                                  deadline schedule
     */
    @Override
    public void reschedule(TaskTimes newTimes) {
        TaskTimes.verifyDeadlineTimes(newTimes);
        times = TaskTimes.makeDeadlineTimes(newTimes.getEndTime());
    }

    /**
     * Returns the CSV fields for this deadline.
     *
     * @return deadline fields in CSV column order
     */
    @Override
    public List<String> getCsvStorageFields() {
        return createCsvStorageFields(
                'D', times.getStartTime(), times.getEndTime());
    }

    /**
     * Returns the deadline display representation.
     *
     * @return deadline type marker, common task representation, and deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + Task.formatDateTime(getEndTime()) + ")";
    }
}
