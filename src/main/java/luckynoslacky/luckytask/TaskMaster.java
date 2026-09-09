package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckystorage.CsvSaver;

/**
 * Stores tasks entered by the user in memory.
 */
public class TaskMaster {
    private static final int DEFAULT_MAX_TASKS = 100;

    private final List<Task> tasks;
    private final int maxTasks;
    private final CsvSaver csvSaver;

    /**
     * Creates a task master with the default capacity of 100 tasks.
     */
    public TaskMaster() {
        this(DEFAULT_MAX_TASKS);
    }

    /**
     * Creates a task master with a configurable capacity.
     *
     * @param maxTasks maximum number of tasks that can be stored
     */
    public TaskMaster(int maxTasks) {
        this(maxTasks, new CsvSaver());
    }

    /**
     * Creates a task master with the default capacity and a configurable saver.
     *
     * @param saver saver used after task-list mutations
     */
    public TaskMaster(CsvSaver saver) {
        this(DEFAULT_MAX_TASKS, saver);
    }

    /**
     * Creates a task master with a configurable capacity and saver.
     *
     * @param maxTasks maximum number of tasks that can be stored
     * @param saver saver used after task-list mutations
     */
    public TaskMaster(int maxTasks, CsvSaver saver) {
        if (maxTasks <= 0) {
            throw new IllegalArgumentException("Maximum tasks must be positive.");
        }

        if (saver == null) {
            throw new IllegalArgumentException("Saver cannot be null.");
        }

        this.maxTasks = maxTasks;
        this.csvSaver = saver;
        tasks = new ArrayList<>();
    }

    /**
     * Adds a task to the task list.
     *
     * @param task task description entered by the user
     */
    public void addTask(Task task) {
        if (tasks.size() >= maxTasks) {
            throw new IllegalStateException("The task list is full.");
        }

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        int previousTaskCount = tasks.size();
        tasks.add(task);
        assert tasks.size() == previousTaskCount + 1
                : "Task count did not increase after adding a task";
        assert tasks.size() <= maxTasks
                : "Task list exceeded its maximum capacity";
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
    }

    /**
     * Returns the number of tasks currently stored.
     *
     * @return current task count
     */
    public int getTaskCount() {
        return tasks.size();
    }

    /**
     * Returns all stored tasks with their original one-based numbers.
     *
     * @return indexed task list
     */
    public TaskList listTasks() {
        return TaskList.fromTasks(tasks, null, task -> true);
    }

    /**
     * Searches tasks by description.
     *
     * @param descriptionQuery text to search for in task descriptions
     * @return indexed matching task list
     */
    public TaskList findTasks(String descriptionQuery) {
        return findTasks(descriptionQuery, null);
    }

    /**
     * Lists deadlines and events occurring on the queried date.
     *
     * @param dateTimeQuery date and time from the find command
     * @return indexed matching task list
     */
    public TaskList findTasks(LocalDateTime dateTimeQuery) {
        return findTasks(null, dateTimeQuery);
    }

    /**
     * Searches tasks using the supplied optional description and date filters.
     * When both filters are present, a task must satisfy both filters.
     *
     * @param descriptionQuery optional text to search for in task descriptions
     * @param dateTimeQuery optional date and time on which a task must occur
     * @return indexed matching task list
     */
    public TaskList findTasks(
            String descriptionQuery,
            LocalDateTime dateTimeQuery) {
        if (descriptionQuery != null && descriptionQuery.isBlank()) {
            throw new IllegalArgumentException("Search description cannot be blank.");
        }

        if (descriptionQuery == null && dateTimeQuery == null) {
            throw new IllegalArgumentException("Search query cannot be empty.");
        }

        LocalDate searchDate = dateTimeQuery == null
                ? null
                : dateTimeQuery.toLocalDate();

        Predicate<Task> matcher = task -> {
            boolean matchesDescription = descriptionQuery == null
                    || task.matchesDescription(descriptionQuery);
            boolean matchesDate = searchDate == null
                    || task.occursOn(searchDate);
            return matchesDescription && matchesDate;
        };

        return TaskList.fromTasks(tasks, searchDate, matcher);
    }

    /**
     * Marks a task as done.
     *
     * @param taskNumber one-based number of the task to mark
     * @return description of the task that was marked
     */
    public String markTaskDone(int taskNumber) {
        return updateTaskStatus(taskNumber, true);
    }

    /**
     * Marks a task as not done.
     *
     * @param taskNumber one-based number of the task to unmark
     * @return description of the task that was unmarked
     */
    public String unmarkTaskUndone(int taskNumber) {
        return updateTaskStatus(taskNumber, false);
    }

    /**
     * Extends the ending time of a deadline or event by the supplied amount.
     *
     * @param taskNumber one-based number of the task to snooze
     * @param amount amount by which to extend the ending time
     * @return the updated task
     * @throws IllegalArgumentException if the task is a ToDo or the amount is
     *                                  invalid
     * @throws LuckyNoStorageException if the updated list cannot be saved
     */
    public Task snoozeTaskBy(int taskNumber, TemporalAmount amount) {
        Task task = getTask(taskNumber);
        if (task.getTaskType() == Task.TaskType.TODO) {
            throw new IllegalArgumentException("ToDos cannot be snoozed.");
        }

        LocalDateTime previousByTime = task instanceof DeadlineTask
                ? ((DeadlineTask) task).getByTime()
                : null;
        LocalDateTime previousEndTime = task instanceof EventTask
                ? ((EventTask) task).getEndTime()
                : null;
        LocalDateTime previousStartTime = task instanceof EventTask
                ? ((EventTask) task).getStartTime()
                : null;

        if (task instanceof DeadlineTask deadlineTask) {
            deadlineTask.snoozeBy(amount);
        } else if (task instanceof EventTask eventTask) {
            eventTask.snoozeBy(amount);
        }

        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            restoreTaskTimes(
                    task, previousByTime, previousStartTime, previousEndTime);
            throw exception;
        }
        return task;
    }

    /**
     * Replaces the ending time of a deadline or event.
     *
     * @param taskNumber one-based number of the task to snooze
     * @param newEndTime replacement ending time
     * @return the updated task
     * @throws IllegalArgumentException if the task is a ToDo or the time is
     *                                  invalid
     * @throws LuckyNoStorageException if the updated list cannot be saved
     */
    public Task snoozeTaskTo(int taskNumber, LocalDateTime newEndTime) {
        Task task = getTask(taskNumber);
        if (task.getTaskType() == Task.TaskType.TODO) {
            throw new IllegalArgumentException("ToDos cannot be snoozed.");
        }

        LocalDateTime previousByTime = task instanceof DeadlineTask
                ? ((DeadlineTask) task).getByTime()
                : null;
        LocalDateTime previousEndTime = task instanceof EventTask
                ? ((EventTask) task).getEndTime()
                : null;
        LocalDateTime previousStartTime = task instanceof EventTask
                ? ((EventTask) task).getStartTime()
                : null;

        if (task instanceof DeadlineTask deadlineTask) {
            deadlineTask.rescheduleTo(newEndTime);
        } else if (task instanceof EventTask eventTask) {
            eventTask.reschedule(eventTask.getStartTime(), newEndTime);
        }

        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            restoreTaskTimes(
                    task, previousByTime, previousStartTime, previousEndTime);
            throw exception;
        }
        return task;
    }

    /**
     * Replaces the deadline of a deadline task.
     *
     * @param taskNumber one-based number of the task to reschedule
     * @param newByTime replacement deadline
     * @return the updated deadline task
     * @throws IllegalArgumentException if the task is not a deadline
     * @throws LuckyNoStorageException if the updated list cannot be saved
     */
    public Task rescheduleDeadline(int taskNumber, LocalDateTime newByTime) {
        Task task = getTask(taskNumber);
        if (!(task instanceof DeadlineTask deadlineTask)) {
            throw new IllegalArgumentException("Task is not a deadline.");
        }

        LocalDateTime previousByTime = deadlineTask.getByTime();
        deadlineTask.rescheduleTo(newByTime);
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            deadlineTask.rescheduleTo(previousByTime);
            throw exception;
        }
        return task;
    }

    /**
     * Replaces both times of an event task.
     *
     * @param taskNumber one-based number of the task to reschedule
     * @param newStartTime replacement start time
     * @param newEndTime replacement end time
     * @return the updated event task
     * @throws IllegalArgumentException if the task is not an event or the new
     *                                  times are invalid
     * @throws LuckyNoStorageException if the updated list cannot be saved
     */
    public Task rescheduleEvent(
            int taskNumber,
            LocalDateTime newStartTime,
            LocalDateTime newEndTime) {
        Task task = getTask(taskNumber);
        if (!(task instanceof EventTask eventTask)) {
            throw new IllegalArgumentException("Task is not an event.");
        }

        LocalDateTime previousStartTime = eventTask.getStartTime();
        LocalDateTime previousEndTime = eventTask.getEndTime();
        eventTask.reschedule(newStartTime, newEndTime);
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            restoreTaskTimes(
                    task, null, previousStartTime, previousEndTime);
            throw exception;
        }
        return task;
    }

    /**
     * Returns the type of a task identified by its one-based task number.
     *
     * @param taskNumber one-based number of the task
     * @return task type
     */
    public Task.TaskType getTaskType(int taskNumber) {
        return getTask(taskNumber).getTaskType();
    }

    /**
     * Returns the start time of an event task.
     *
     * @param taskNumber one-based number of the task
     * @return event start time
     * @throws IllegalArgumentException if the task is not an event
     */
    public LocalDateTime getTaskStartTime(int taskNumber) {
        Task task = getTask(taskNumber);
        if (task instanceof EventTask eventTask) {
            return eventTask.getStartTime();
        }
        throw new IllegalArgumentException("Task is not an event.");
    }

    /**
     * Returns the ending time of a deadline or event task.
     *
     * @param taskNumber one-based number of the task
     * @return deadline or event ending time
     * @throws IllegalArgumentException if the task is a ToDo
     */
    public LocalDateTime getTaskEndTime(int taskNumber) {
        Task task = getTask(taskNumber);
        if (task instanceof DeadlineTask deadlineTask) {
            return deadlineTask.getByTime();
        }
        if (task instanceof EventTask eventTask) {
            return eventTask.getEndTime();
        }
        throw new IllegalArgumentException("Task has no ending time.");
    }

    /**
     * Updates a task's completion status and persists the change.
     *
     * @param taskNumber one-based number of the task to update
     * @param shouldBeDone whether the task should be marked as done
     * @return description of the updated task
     * @throws LuckyNoStorageException if the updated list cannot be saved
     */
    private String updateTaskStatus(int taskNumber, boolean shouldBeDone) {
        Task task = getTask(taskNumber);
        boolean wasDone = task.isDone();

        if (shouldBeDone) {
            task.markAsDone();
        } else {
            task.unmarkAsUndone();
        }

        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            restoreTaskStatus(task, wasDone);
            throw exception;
        }
        return task.toString();
    }

    /**
     * Deletes a task from the task list.
     *
     * @param taskNumber one-based number of the task to delete
     * @return description of the deleted task
     */
    public String deleteTask(int taskNumber) {
        int taskIndex = getTaskIndex(taskNumber);
        int previousTaskCount = tasks.size();
        Task deletedTask = tasks.remove(taskIndex);
        assert tasks.size() == previousTaskCount - 1
                : "Task count did not decrease after deleting a task";
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            tasks.add(taskIndex, deletedTask);
            throw exception;
        }
        return deletedTask.toString();
    }

    /**
     * Returns the task records in CSV column order.
     *
     * @return immutable list of CSV records
     */
    public List<List<String>> getCsvStorageRecords() {
        return tasks.stream()
                .map(Task::getCsvStorageFields)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Replaces the in-memory task list with tasks loaded from CSV storage.
     * This method does not save the list again.
     *
     * @param loadedTasks tasks loaded from CSV storage
     */
    public void loadTasksFromCsvStorageRecord(List<Task> loadedTasks) {
        if (loadedTasks == null) {
            throw new LuckyNoStorageException("Tasks cannot be null.");
        }

        if (loadedTasks.size() > maxTasks) {
            throw new LuckyNoStorageException(
                    "Saved task list exceeds the maximum capacity.");
        }

        if (loadedTasks.stream().anyMatch(task -> task == null)) {
            throw new LuckyNoStorageException(
                    "Saved task list contains a null task.");
        }

        this.tasks.clear();
        this.tasks.addAll(loadedTasks);
    }

    /** Persists the current task list through the configured saver. */
    private void saveChanges() {
        csvSaver.save(this);
    }

    /**
     * Restores a task's status after a failed persistence operation.
     *
     * @param task task whose status should be restored
     * @param wasDone status before the attempted change
     */
    private void restoreTaskStatus(Task task, boolean wasDone) {
        if (wasDone) {
            task.markAsDone();
        } else {
            task.unmarkAsUndone();
        }
    }

    /**
     * Restores the datetime fields captured before a failed save.
     *
     * @param task task whose times should be restored
     * @param byTime previous deadline, or null for an event
     * @param startTime previous event start, or null for a deadline
     * @param endTime previous event end, or null for a deadline
     */
    private void restoreTaskTimes(
            Task task,
            LocalDateTime byTime,
            LocalDateTime startTime,
            LocalDateTime endTime) {
        if (task instanceof DeadlineTask deadlineTask) {
            deadlineTask.rescheduleTo(byTime);
        } else if (task instanceof EventTask eventTask) {
            eventTask.reschedule(startTime, endTime);
        }
    }

    /**
     * Gets a task by its one-based task number.
     *
     * @param taskNumber one-based number of the task
     * @return the requested task
     */
    private Task getTask(int taskNumber) {
        return tasks.get(getTaskIndex(taskNumber));
    }

    /**
     * Converts and validates a one-based task number.
     *
     * @param taskNumber one-based task number
     * @return corresponding zero-based task index
     * @throws IllegalArgumentException if the task number is outside the list
     */
    private int getTaskIndex(int taskNumber) {
        int taskIndex = taskNumber - 1;
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new IllegalArgumentException("Invalid task number.");
        }
        return taskIndex;
    }
}
