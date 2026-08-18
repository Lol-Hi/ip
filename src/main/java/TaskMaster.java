/**
 * Stores tasks entered by the user in memory.
 */
public class TaskMaster {
    private static final int DEFAULT_MAX_TASKS = 100;

    private final String[] tasks;
    private final boolean[] areTasksDone;
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

        tasks = new String[maxTasks];
        areTasksDone = new boolean[maxTasks];
    }

    /**
     * Adds a task to the task list.
     *
     * @param task task description entered by the user
     */
    public void addTask(String task) {
        if (taskCount >= tasks.length) {
            throw new IllegalStateException("The task list is full.");
        }

        tasks[taskCount] = task;
        taskCount++;
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
                    .append(".[")
                    .append(areTasksDone[i] ? "X" : " ")
                    .append("] ")
                    .append(tasks[i]);
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
        int taskIndex = taskNumber - 1;

        if (taskIndex < 0 || taskIndex >= taskCount) {
            throw new IllegalArgumentException("Invalid task number.");
        }

        areTasksDone[taskIndex] = true;
        return tasks[taskIndex];
    }

    /**
     * Marks a task as not done.
     *
     * @param taskNumber one-based number of the task to unmark
     * @return description of the task that was unmarked
     */
    public String unmarkTaskUndone(int taskNumber) {
        int taskIndex = taskNumber - 1;

        if (taskIndex < 0 || taskIndex >= taskCount) {
            throw new IllegalArgumentException("Invalid task number.");
        }

        areTasksDone[taskIndex] = false;
        return tasks[taskIndex];
    }
}
