package luckynoslacky.luckycommand;

import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a request to delete a task.
 */
public class LuckyNoDeleteCommand extends LuckyNoCommand {
    private final int taskNumber;
    private final TaskMaster taskMaster;

    /**
     * Creates a task-deletion command bound to a task master.
     *
     * @param taskNumber one-based task number
     * @param taskMaster task master whose task should be deleted
     * @throws IllegalArgumentException if {@code taskMaster} is null
     */
    public LuckyNoDeleteCommand(int taskNumber, TaskMaster taskMaster) {
        super(CommandType.DELETE_TASK);
        this.taskMaster = requireTaskMaster(taskMaster);
        this.taskNumber = taskNumber;
    }

    /**
     * Deletes the selected task and returns the resulting user-facing reply.
     *
     * @return deletion response
     */
    @Override
    public String execute() {
        String deletedTask = this.taskMaster.deleteTask(taskNumber);
        return LuckyNoMessages.deletedTaskMessage(
                deletedTask, this.taskMaster.getTaskCount());
    }
}
