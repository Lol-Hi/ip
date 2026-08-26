package luckynoslacky.luckycommand;

import luckynoslacky.luckyui.LuckyNoMessages;
import luckynoslacky.luckytask.TaskMaster;

/**
 * Represents a request to delete a task.
 */
public class LuckyNoDeleteCommand extends LuckyNoCommand {
    private final int taskNumber;
    private final TaskMaster taskMaster;

    /**
     * Creates a task-deletion command.
     *
     * @param taskNumber one-based task number
     */
    public LuckyNoDeleteCommand(int taskNumber) {
        this(taskNumber, null);
    }

    /**
     * Creates a task-deletion command bound to a task master.
     *
     * @param taskNumber one-based task number
     * @param taskMaster task master whose task should be deleted
     */
    public LuckyNoDeleteCommand(int taskNumber, TaskMaster taskMaster) {
        super(CommandType.DELETE_TASK);
        this.taskMaster = taskMaster;
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

    /**
     * Returns the one-based task number.
     *
     * @return task number
     */
    @Deprecated
    public int getTaskNumber() {
        return taskNumber;
    }
}
