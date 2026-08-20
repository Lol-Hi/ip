/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    private final char taskType;
    private final String description;
    private final String byTime;
    private final String fromTime;
    private final String toTime;
    private boolean isDone;

    /**
     * Creates an incomplete task.
     *
     * @param description task description
     */
    public Task(String description) {
        this('T', description, null, null, null);
    }

    /**
     * Creates an incomplete deadline task.
     *
     * @param description task description
     * @param byTime deadline description
     */
    public Task(String description, String byTime) {
        this('D', description, byTime, null, null);
    }

    /**
     * Creates an incomplete event task.
     *
     * @param description task description
     * @param fromTime event start description
     * @param toTime event end description
     */
    public Task(String description, String fromTime, String toTime) {
        this('E', description, null, fromTime, toTime);
    }

    private Task(char taskType, String description, String byTime,
                 String fromTime, String toTime) {
        char normalizedType = Character.toUpperCase(taskType);

        if (normalizedType != 'T' && normalizedType != 'D' && normalizedType != 'E') {
            throw new IllegalArgumentException("Invalid task type.");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Task description cannot be empty.");
        }

        this.taskType = normalizedType;
        this.description = description;
        this.byTime = byTime;
        this.fromTime = fromTime;
        this.toTime = toTime;
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
     * Returns the task's display representation.
     *
     * @return type icon, status icon, description, and date/time details
     */
    @Override
    public String toString() {
        String result = "[" + taskType + "][" + getStatusIcon() + "] " + description;

        if (taskType == 'D') {
            result += " (by: " + byTime + ")";
        } else if (taskType == 'E') {
            result += " (from: " + fromTime + " to: " + toTime + ")";
        }

        return result;
    }
}
