package luckynoslacky.luckytask;

import java.time.DateTimeException;
import java.time.LocalDateTime;

/**
 * Validates and creates scheduling information for tasks in a task list.
 */
class TaskSchedulingService {
    private final TaskList taskList;

    /**
     * Creates a scheduling service for the supplied task list.
     *
     * @param taskList task list whose schedules are validated
     */
    TaskSchedulingService(TaskList taskList) {
        if (taskList == null) {
            throw new IllegalArgumentException("Task list cannot be null.");
        }

        this.taskList = taskList;
    }

    /**
     * Returns the scheduling state needed to parse a timed-task command.
     *
     * @param taskNumber one-based number of the task
     * @return immutable task scheduling state
     */
    TaskSchedule getTaskSchedule(int taskNumber) {
        Task task = taskList.getTask(taskNumber);
        return new TaskSchedule(task.getTaskType(), task.getTaskTimes());
    }

    /**
     * Validates that a task can have its schedule modified.
     *
     * @param taskNumber one-based number of the task
     * @throws TaskSchedulingException if the task is a ToDo
     */
    void validateTimedTask(int taskNumber) {
        if (taskList.getTask(taskNumber).getTaskType() == Task.TaskType.TODO) {
            throw new TaskSchedulingException(
                    TaskSchedulingException.Reason.TODO_TASK);
        }
    }

    /**
     * Validates the result of extending a task's ending time.
     *
     * @param taskNumber one-based number of the task
     * @param amount duration by which to extend the ending time
     * @param currentTime current date and time for deadline validation
     * @throws TaskSchedulingException if the task or resulting time is invalid
     */
    void validateSnoozeBy(
            int taskNumber,
            DurationPeriod amount,
            LocalDateTime currentTime) {
        if (amount == null || currentTime == null) {
            throw new IllegalArgumentException("Snooze validation values cannot be null.");
        }
        TaskSchedule schedule = getTaskSchedule(taskNumber);
        validateTimedTask(taskNumber);
        try {
            LocalDateTime snoozedEndTime = amount.addTo(
                    schedule.taskTimes().getEndTime());
            validateNewEndTime(schedule, snoozedEndTime, currentTime);
        } catch (DateTimeException exception) {
            throw new TaskSchedulingException(
                    TaskSchedulingException.Reason.TIME_OVERFLOW, exception);
        }
    }

    /**
     * Validates a replacement ending time for a deadline or event.
     *
     * @param taskNumber one-based number of the task
     * @param endTime replacement ending time
     * @param currentTime current date and time for deadline validation
     * @throws TaskSchedulingException if the task or replacement time is invalid
     */
    void validateSnoozeTo(
            int taskNumber,
            LocalDateTime endTime,
            LocalDateTime currentTime) {
        if (endTime == null || currentTime == null) {
            throw new IllegalArgumentException("Snooze validation values cannot be null.");
        }
        TaskSchedule schedule = getTaskSchedule(taskNumber);
        validateTimedTask(taskNumber);
        validateNewEndTime(schedule, endTime, currentTime);
    }

    /**
     * Creates validated replacement timing information for a task.
     *
     * @param taskNumber one-based number of the task
     * @param startTime replacement event start time, or {@code null} for a deadline
     * @param endTime replacement deadline or event end time
     * @param currentTime current date and time for deadline validation
     * @return validated timing information matching the task category
     * @throws TaskSchedulingException if the task or replacement schedule is invalid
     */
    TaskTimes createRescheduledTimes(
            int taskNumber,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LocalDateTime currentTime) {
        if (endTime == null || currentTime == null) {
            throw new IllegalArgumentException("Rescheduling values cannot be null.");
        }
        TaskSchedule schedule = getTaskSchedule(taskNumber);
        validateTimedTask(taskNumber);
        if (schedule.taskType() == Task.TaskType.DEADLINE) {
            validateNewEndTime(schedule, endTime, currentTime);
            return TaskTimes.makeDeadlineTimes(endTime);
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Event start time cannot be null.");
        }
        if (endTime.isBefore(startTime)) {
            throw new TaskSchedulingException(
                    TaskSchedulingException.Reason.END_BEFORE_START);
        }
        return TaskTimes.makeEventTimes(startTime, endTime);
    }

    /** Validates a new ending time against a deadline or event's schedule. */
    private void validateNewEndTime(
            TaskSchedule schedule,
            LocalDateTime endTime,
            LocalDateTime currentTime) {
        if (schedule.taskType() == Task.TaskType.DEADLINE
                && endTime.isBefore(currentTime)) {
            throw new TaskSchedulingException(
                    TaskSchedulingException.Reason.PAST_DEADLINE);
        }
        if (schedule.taskType() == Task.TaskType.EVENT
                && endTime.isBefore(schedule.taskTimes().getStartTime())) {
            throw new TaskSchedulingException(
                    TaskSchedulingException.Reason.END_BEFORE_START);
        }
    }
}
