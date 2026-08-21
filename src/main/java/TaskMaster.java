import java.util.ArrayList;

/**
 * Stores tasks entered by the user in memory.
 */
public class TaskMaster {
    private static final int DEFAULT_MAX_TASKS = 100;

    private final ArrayList<Task> taskRoster;
    private final int maxTasks;

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
        if (maxTasks <= 0) {
            throw new IllegalArgumentException("Maximum tasks must be positive.");
        }

        this.maxTasks = maxTasks;
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
        task.markAsDone();
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
        task.unmarkAsUndone();
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

        return taskRoster.remove(taskIndex).toString();
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
