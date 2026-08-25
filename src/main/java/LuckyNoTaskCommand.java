/**
 * Represents a command that creates a task.
 */
public class LuckyNoTaskCommand extends LuckyNoCommand {
    private final Task task;
    private final TaskMaster taskMaster;

    /**
     * Creates a task-creation command containing the already constructed task.
     *
     * @param task task to add
     */
    public LuckyNoTaskCommand(Task task) {
        this(task, null);
    }

    LuckyNoTaskCommand(Task task, TaskMaster taskMaster) {
        super(CommandType.CREATE_TASK);
        this.taskMaster = taskMaster;
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        this.task = task;
    }

    @Override
    public String execute() {
        this.taskMaster.addTask(task);
        return LuckyNoMessages.addedTaskMessage(
                task, this.taskMaster.getTaskCount());
    }
}
