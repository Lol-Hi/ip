package luckynoslacky.luckycommand;

import luckynoslacky.luckyui.LuckyNoMessages;
import luckynoslacky.luckytask.TaskMaster;

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
     * Creates a status-setting command attached to a task master.
     *
     * @param taskNumber one-based task number
     * @param markDone whether the task should be marked done
     * @param taskMaster task master whose task should be updated
     */
    public LuckyNoMarkCommand(int taskNumber, boolean markDone, TaskMaster taskMaster) {
        super(CommandType.TOGGLE_TASK);
        this.taskMaster = taskMaster;
        this.taskNumber = taskNumber;
        this.markDone = markDone;
    }

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
