package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        tasks.add(task);
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
        TaskList taskList = new TaskList();
        for (int i = 0; i < tasks.size(); i++) {
            taskList.addTask(i + 1, tasks.get(i));
        }
        return taskList;
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
        TaskList matchingTasks = new TaskList(searchDate);
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            boolean matchesDescription = descriptionQuery == null
                    || task.matchesDescription(descriptionQuery);
            boolean matchesDate = searchDate == null
                    || task.occursOn(searchDate);
            if (matchesDescription && matchesDate) {
                matchingTasks.addTask(i + 1, task);
            }
        }

        return matchingTasks;
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
        Task deletedTask = tasks.remove(taskIndex);
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
