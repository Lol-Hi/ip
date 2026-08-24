import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores tasks entered by the user in memory.
 */
public class TaskMaster {
    private static final int DEFAULT_MAX_TASKS = 100;

    private final ArrayList<Task> taskRoster;
    private final int maxTasks;
    private final LuckyNoCSVSaver saver;

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
        this(maxTasks, new LuckyNoCSVSaver());
    }

    /**
     * Creates a task master with the default capacity and a configurable saver.
     *
     * @param saver saver used after task-list mutations
     */
    TaskMaster(LuckyNoCSVSaver saver) {
        this(DEFAULT_MAX_TASKS, saver);
    }

    /**
     * Creates a task master with a configurable capacity and saver.
     *
     * @param maxTasks maximum number of tasks that can be stored
     * @param saver saver used after task-list mutations
     */
    TaskMaster(int maxTasks, LuckyNoCSVSaver saver) {
        if (maxTasks <= 0) {
            throw new IllegalArgumentException("Maximum tasks must be positive.");
        }

        if (saver == null) {
            throw new IllegalArgumentException("Saver cannot be null.");
        }

        this.maxTasks = maxTasks;
        this.saver = saver;
        taskRoster = new ArrayList<>();
    }

    /**
     * Adds a task to the task list.
     *
     * @param task task description entered by the user
     */
    public void addTask(Task task) {
        if (taskRoster.size() >= maxTasks) {
            throw new IllegalStateException("The task list is full.");
        }

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        taskRoster.add(task);
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            taskRoster.remove(taskRoster.size() - 1);
            throw exception;
        }
    }

    /**
     * Returns the number of tasks currently stored.
     *
     * @return current task count
     */
    public int getTaskCount() {
        return taskRoster.size();
    }

    /**
     * Returns all stored tasks with numbering.
     *
     * @return formatted task list
     */
    public String listTasks() {
        if (taskRoster.isEmpty()) {
            return LuckyNoMessages.emptyTaskListMessage();
        }

        StringBuilder result = new StringBuilder(LuckyNoMessages.taskListHeader());

        for (int i = 0; i < taskRoster.size(); i++) {
            result.append("\n")
                    .append(i + 1)
                    .append(".")
                    .append(taskRoster.get(i));
        }

        return result.toString();
    }

    /**
     * Marks a task as done.
     *
     * @param taskNumber one-based number of the task to mark
     * @return description of the task that was marked
     */
    public String markTaskDone(int taskNumber) {
        Task task = getTask(taskNumber);
        boolean wasDone = task.isDone();
        task.markAsDone();
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            restoreTaskStatus(task, wasDone);
            throw exception;
        }
        return task.toString();
    }

    /**
     * Marks a task as not done.
     *
     * @param taskNumber one-based number of the task to unmark
     * @return description of the task that was unmarked
     */
    public String unmarkTaskUndone(int taskNumber) {
        Task task = getTask(taskNumber);
        boolean wasDone = task.isDone();
        task.unmarkAsUndone();
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
        int taskIndex = taskNumber - 1;

        if (taskIndex < 0 || taskIndex >= taskRoster.size()) {
            throw new IllegalArgumentException("Invalid task number.");
        }

        Task deletedTask = taskRoster.remove(taskIndex);
        try {
            saveChanges();
        } catch (LuckyNoStorageException exception) {
            taskRoster.add(taskIndex, deletedTask);
            throw exception;
        }
        return deletedTask.toString();
    }

    /**
     * Returns the task records in CSV column order.
     *
     * @return immutable list of CSV records
     */
    public List<List<String>> getCSVStorageRecords() {
        return taskRoster.stream()
                .map(Task::getCSVStorageFields)
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Replaces the in-memory task list with tasks loaded from CSV storage.
     * This method does not save the list again.
     *
     * @param tasks tasks loaded from CSV storage
     */
    void loadTasksFromCSVStorageRecord(List<Task> tasks) {
        if (tasks == null) {
            throw new LuckyNoStorageException("Tasks cannot be null.");
        }

        if (tasks.size() > maxTasks) {
            throw new LuckyNoStorageException(
                    "Saved task list exceeds the maximum capacity.");
        }

        if (tasks.stream().anyMatch(task -> task == null)) {
            throw new LuckyNoStorageException(
                    "Saved task list contains a null task.");
        }

        taskRoster.clear();
        taskRoster.addAll(tasks);
    }

    private void saveChanges() {
        saver.save(this);
    }

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
        int taskIndex = taskNumber - 1;

        if (taskIndex < 0 || taskIndex >= taskRoster.size()) {
            throw new IllegalArgumentException("Invalid task number.");
        }

        return taskRoster.get(taskIndex);
    }
}
