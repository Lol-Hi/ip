/**
 * Stores tasks entered by the user in memory.
 */
public class TaskMaster {
    private static final int DEFAULT_MAX_TASKS = 100;

    private final String[] tasks;
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

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < taskCount; i++) {
            if (i > 0) {
                result.append("\n");
            }

            result.append(i + 1)
                    .append(". ")
                    .append(tasks[i]);
        }

        return result.toString();
    }
}
