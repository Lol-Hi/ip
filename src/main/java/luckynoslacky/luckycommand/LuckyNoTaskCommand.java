package luckynoslacky.luckycommand;

import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a command that creates a task.
 */
public class LuckyNoTaskCommand extends LuckyNoCommand {
    private final Task task;
    private final TaskMaster taskMaster;

    /**
     * Creates a task-creation command bound to a task master.
     *
     * @param task task to add
     * @param taskMaster task master that should receive the task
     * @throws IllegalArgumentException if either argument is null
     */
    public LuckyNoTaskCommand(Task task, TaskMaster taskMaster) {
        super(CommandType.CREATE_TASK);
        this.taskMaster = requireTaskMaster(taskMaster);
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        this.task = task;
    }

    /**
     * Adds the task and returns the resulting user-facing reply.
     *
     * @return task-added response
     */
    @Override
    public String execute() {
        this.taskMaster.addTask(task);
        return LuckyNoMessages.addedTaskMessage(
                task, this.taskMaster.getTaskCount());
    }
}
