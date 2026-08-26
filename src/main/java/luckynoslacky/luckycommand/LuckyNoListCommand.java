package luckynoslacky.luckycommand;

import luckynoslacky.luckytask.TaskMaster;

/**
 * Represents a request to list all stored tasks.
 */
public class LuckyNoListCommand extends LuckyNoCommand {

    private final TaskMaster taskMaster;

    public LuckyNoListCommand(TaskMaster taskMaster) {
        super(CommandType.LIST);
        this.taskMaster = taskMaster;
    }

    @Override
    public String execute() {
        return taskMaster.listTasks();
    }
}
