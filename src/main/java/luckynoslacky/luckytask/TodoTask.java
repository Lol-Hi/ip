package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.util.List;

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
     * ToDos do not occur on a date because they have no date or time.
     *
     * @param date date to check
     * @return always false
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return false;
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
