package luckynoslacky.luckycommand;

import java.time.LocalDateTime;

import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a request to replace the time or times of a timed task.
 */
public class LuckyNoReschedCommand extends LuckyNoCommand {
    private final int taskNumber;
    private final LocalDateTime newStartTime;
    private final LocalDateTime newEndTime;
    private final TaskMaster taskMaster;

    /**
     * Creates a deadline rescheduling command.
     *
     * @param taskNumber one-based task number
     * @param newEndTime replacement deadline
     * @param taskMaster task master containing the task
     * @throws IllegalArgumentException if an argument is null
     */
    public LuckyNoReschedCommand(
            int taskNumber, LocalDateTime newEndTime, TaskMaster taskMaster) {
        if (newEndTime == null) {
            throw new IllegalArgumentException("Rescheduled end time cannot be null.");
        }
        this.taskNumber = taskNumber;
        this.newStartTime = null;
        this.newEndTime = newEndTime;
        this.taskMaster = requireTaskMaster(taskMaster);
    }

    /**
     * Creates an event rescheduling command.
     *
     * @param taskNumber one-based task number
     * @param newStartTime replacement event start
     * @param newEndTime replacement event end
     * @param taskMaster task master containing the task
     * @throws IllegalArgumentException if an argument is null
     */
    public LuckyNoReschedCommand(
            int taskNumber,
            LocalDateTime newStartTime,
            LocalDateTime newEndTime,
            TaskMaster taskMaster) {
        if (newStartTime == null || newEndTime == null) {
            throw new IllegalArgumentException("Rescheduled event times cannot be null.");
        }
        this.taskNumber = taskNumber;
        this.newStartTime = newStartTime;
        this.newEndTime = newEndTime;
        this.taskMaster = requireTaskMaster(taskMaster);
    }

    /**
     * Applies the rescheduling and returns the updated task.
     *
     * @return rescheduling response
     */
    @Override
    public String execute() {
        Task updatedTask = newStartTime == null
                ? taskMaster.rescheduleDeadline(taskNumber, newEndTime)
                : taskMaster.rescheduleEvent(
                        taskNumber, newStartTime, newEndTime);
        return LuckyNoMessages.rescheduledTaskMessage(updatedTask);
    }
}
