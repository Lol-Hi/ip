package luckynoslacky.luckycommand;

import luckynoslacky.ResponseKind;
import luckynoslacky.ResponseTone;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoMessages;

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
        return LuckyNoMessages.listTasksMessage(taskMaster.listTasks());
    }

    /**
     * Returns the information tone for the task-list response.
     *
     * @return information response tone
     */
    @Override
    public ResponseTone getResponseTone() {
        return ResponseTone.INFO;
    }

    /**
     * Returns the task-content kind for the task-list response.
     *
     * @return task-content response kind
     */
    @Override
    public ResponseKind getResponseKind() {
        return ResponseKind.TASK_CONTENT;
    }
}
