package luckynoslacky.luckycommand;

import luckynoslacky.ResponseKind;
import luckynoslacky.ResponseTone;
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

    /**
     * Returns the success tone for the task-deletion response.
     *
     * @return success response tone
     */
    @Override
    public ResponseTone getResponseTone() {
        return ResponseTone.SUCCESS;
    }

    /**
     * Returns the task-content kind for the task-deletion response.
     *
     * @return task-content response kind
     */
    @Override
    public ResponseKind getResponseKind() {
        return ResponseKind.TASK_CONTENT;
    }
}
