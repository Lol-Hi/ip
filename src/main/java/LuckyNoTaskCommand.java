/**
 * Represents a command that creates a task.
 */
public class LuckyNoTaskCommand extends LuckyNoCommand {
    private final Task task;

    /**
     * Creates a task-creation command containing the already constructed task.
     *
     * @param task task to add
     */
    public LuckyNoTaskCommand(Task task) {
        super("createTask");
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        this.task = task;
    }

    /**
     * Returns the task carried by this command.
     *
     * @return task to add
     */
    public Task getTask() {
        return task;
    }
}
