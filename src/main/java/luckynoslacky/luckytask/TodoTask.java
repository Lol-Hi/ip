package luckynoslacky.luckytask;

import java.time.LocalDateTime;
import java.util.List;

import luckynoslacky.luckyparser.DurationPeriod;

/**
 * Represents a task without any date or time information.
 */
public class TodoTask extends Task {

    /**
     * Creates an incomplete ToDo task.
     *
     * @param description task description
     */
    public TodoTask(String description) {
        super(description);
    }

    /**
     * Returns the category of this task.
     *
     * @return ToDo task category
     */
    @Override
    public TaskType getTaskType() {
        return TaskType.TODO;
    }

    /**
     * Returns the CSV fields for this ToDo.
     *
     * @return ToDo fields in CSV column order
     */
    @Override
    public List<String> getCsvStorageFields() {
        return createCsvStorageFields('T', null, null);
    }

    /**
     * Rejects snoozing because a ToDo has no ending time.
     *
     * @param amount duration by which to extend the ending time
     * @throws IllegalArgumentException always, because ToDos cannot be
     *                                  snoozed
     */
    @Override
    public void snoozeBy(DurationPeriod amount) {
        throw new IllegalArgumentException("ToDos cannot be snoozed.");
    }

    /**
     * Returns the absence of timing information for this ToDo.
     *
     * @return timing information without a start or end time
     */
    @Override
    public TaskTimes getTaskTimes() {
        return TaskTimes.none();
    }

    /**
     * Rejects schedule replacement because a ToDo has no timing information.
     *
     * @param newTimes replacement task timing information
     * @throws IllegalArgumentException always, because ToDos cannot be
     *                                  rescheduled
     */
    @Override
    public void reschedule(TaskTimes newTimes) {
        throw new IllegalArgumentException("ToDos cannot be rescheduled.");
    }

    /**
     * Rejects ending-time access because a ToDo has no ending time.
     *
     * @return never returns normally
     * @throws IllegalArgumentException always, because ToDos have no ending
     *                                  time
     */
    @Override
    public LocalDateTime getEndTime() {
        throw new IllegalArgumentException("Task has no ending time.");
    }

    /**
     * Returns the ToDo display representation.
     *
     * @return ToDo type marker followed by the common task representation
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
