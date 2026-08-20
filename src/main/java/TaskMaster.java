/**
 * Stores tasks entered by the user in memory.
 */
public class TaskMaster {
    private static final int DEFAULT_MAX_TASKS = 100;

    private final Task[] taskRoster;
    private int taskCount = 0;

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

        taskRoster = new Task[maxTasks];
    }

    /**
     * Adds a task to the task list.
     *
     * @param task task description entered by the user
     */
    public void addTask(Task task) {
        if (taskCount >= taskRoster.length) {
            throw new IllegalStateException("The task list is full.");
        }

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        taskRoster[taskCount] = task;
        taskCount++;
    }

    /**
     * Returns the number of tasks currently stored.
     *
     * @return current task count
     */
    public int getTaskCount() {
        return taskCount;
    }

    /**
     * Returns all stored tasks with numbering.
     *
     * @return formatted task list
     */
    public String listTasks() {
        if (taskCount == 0) {
            return "No tasks yet.";
        }

        StringBuilder result = new StringBuilder("Here are the tasks in your list:");

        for (int i = 0; i < taskCount; i++) {
            result.append("\n")
                    .append(i + 1)
                    .append(".")
                    .append(taskRoster[i]);
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
     * Gets a task by its one-based task number.
     *
     * @param taskNumber one-based number of the task
     * @return the requested task
     */
    private Task getTask(int taskNumber) {
        int taskIndex = taskNumber - 1;

        if (taskIndex < 0 || taskIndex >= taskCount) {
            throw new IllegalArgumentException("Invalid task number.");
        }

        return taskRoster[taskIndex];
    }
}
