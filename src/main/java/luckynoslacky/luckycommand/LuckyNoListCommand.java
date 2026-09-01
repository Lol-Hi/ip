package luckynoslacky.luckycommand;

import luckynoslacky.luckytask.TaskMaster;

/**
 * Represents a request to list all stored tasks.
 */
public class LuckyNoListCommand extends LuckyNoCommand {

    private final TaskMaster taskMaster;

    /**
     * Creates a list command bound to a task master.
     *
     * @param taskMaster task master whose tasks should be listed
     * @throws IllegalArgumentException if {@code taskMaster} is null
     */
    public LuckyNoListCommand(TaskMaster taskMaster) {
        this.taskMaster = requireTaskMaster(taskMaster);
    }

    /**
     * Returns the formatted list of tasks.
     *
     * @return task-list response
     */
    @Override
    public String execute() {
        return taskMaster.listTasks();
    }
}
