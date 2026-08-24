import java.util.List;

/**
 * Represents the common state and behavior shared by all task types.
 */
public abstract class Task {
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
     * Returns the raw fields used to save this task as a CSV record.
     *
     * @return task fields in CSV column order
     */
    public abstract List<String> getCSVStorageFields();

    /**
     * Creates the common CSV fields for a concrete task type.
     *
     * @param taskType task type marker
     * @param startTime task start time, or an empty string when not applicable
     * @param finishTime task finish time, or an empty string when not applicable
     * @return fields in CSV column order
     */
    protected final List<String> createCSVStorageFields(
            char taskType,
            String startTime,
            String finishTime) {
        return List.of(
                String.valueOf(taskType),
                isDone ? "1" : "0",
                description,
                startTime,
                finishTime);
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
