package luckynoslacky.luckycommand;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a request to extend or replace the ending time of a timed task.
 */
public class LuckyNoSnoozeCommand extends LuckyNoCommand {
    private final int taskNumber;
    private final TemporalAmount amount;
    private final LocalDateTime targetEndTime;
    private final TaskMaster taskMaster;

    /**
     * Creates a one-hour snooze command.
     *
     * @param taskNumber one-based task number
     * @param taskMaster task master containing the task
     */
    public LuckyNoSnoozeCommand(int taskNumber, TaskMaster taskMaster) {
        this(taskNumber, java.time.Duration.ofHours(1), taskMaster);
    }

    /**
     * Creates a duration-based snooze command.
     *
     * @param taskNumber one-based task number
     * @param amount duration by which to extend the ending time
     * @param taskMaster task master containing the task
     * @throws IllegalArgumentException if an argument is null
     */
    public LuckyNoSnoozeCommand(
            int taskNumber, TemporalAmount amount, TaskMaster taskMaster) {
        if (amount == null) {
            throw new IllegalArgumentException("Snooze amount cannot be null.");
        }
        this.taskNumber = taskNumber;
        this.amount = amount;
        this.targetEndTime = null;
        this.taskMaster = requireTaskMaster(taskMaster);
    }

    /**
     * Creates a snooze command with an explicit ending time.
     *
     * @param taskNumber one-based task number
     * @param targetEndTime replacement ending time
     * @param taskMaster task master containing the task
     * @throws IllegalArgumentException if an argument is null
     */
    public LuckyNoSnoozeCommand(
            int taskNumber, LocalDateTime targetEndTime, TaskMaster taskMaster) {
        if (targetEndTime == null) {
            throw new IllegalArgumentException("Snooze end time cannot be null.");
        }
        this.taskNumber = taskNumber;
        this.amount = null;
        this.targetEndTime = targetEndTime;
        this.taskMaster = requireTaskMaster(taskMaster);
    }

    /**
     * Applies the snooze and returns the updated task.
     *
     * @return snooze response
     */
    @Override
    public String execute() {
        Task updatedTask = targetEndTime == null
                ? taskMaster.snoozeTaskBy(taskNumber, amount)
                : taskMaster.snoozeTaskTo(taskNumber, targetEndTime);
        return LuckyNoMessages.snoozedTaskMessage(updatedTask);
    }
}
