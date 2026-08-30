package luckynoslacky.luckycommand;

import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents either a mark or an unmark request.
 */
public class LuckyNoMarkCommand extends LuckyNoCommand {
    private final int taskNumber;
    private final boolean markDone;
    private final TaskMaster taskMaster;

    /**
     * Creates a command that sets a task's done status explicitly.
     *
     * @param taskNumber one-based task number
     * @param markDone whether the task should be marked done
     */
    public LuckyNoMarkCommand(int taskNumber, boolean markDone) {
        this(taskNumber, markDone, null);
    }

    /**
     * Creates a task-status command bound to a task master.
     *
     * @param taskNumber one-based task number
     * @param markDone whether the task should be marked done
     * @param taskMaster task master containing the task
     */
    public LuckyNoMarkCommand(int taskNumber, boolean markDone, TaskMaster taskMaster) {
        super(CommandType.TOGGLE_TASK);
        this.taskMaster = taskMaster;
        this.taskNumber = taskNumber;
        this.markDone = markDone;
    }

    /**
     * Applies the requested status and returns the resulting reply.
     *
     * @return mark or unmark response
     */
    @Override
    public String execute() {
        String formattedTask = markDone
                ? this.taskMaster.markTaskDone(taskNumber)
                : this.taskMaster.unmarkTaskUndone(taskNumber);

        return markDone
                ? LuckyNoMessages.markedTaskMessage(formattedTask)
                : LuckyNoMessages.unmarkedTaskMessage(formattedTask);
    }
}
