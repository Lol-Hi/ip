package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the indexed task results produced by a task-list query.
 */
public class TaskList {
    private final List<IndexedTask> indexedTasks;
    private final Optional<LocalDate> searchDate;

    /**
     * Creates a task list without a date-search context.
     */
    public TaskList() {
        this(null);
    }

    /**
     * Creates a task list with an optional date-search context.
     *
     * @param searchDate date used by the query, or {@code null} for a
     *                   non-date query
     */
    public TaskList(LocalDate searchDate) {
        this.indexedTasks = new ArrayList<>();
        this.searchDate = Optional.ofNullable(searchDate);
    }

    /**
     * Creates an indexed result from tasks satisfying a matching predicate.
     * The original one-based task numbers are retained in the result.
     *
     * @param tasks source tasks to inspect
     * @param searchDate date used by the query, or {@code null}
     * @param matcher predicate deciding which tasks to include
     * @return indexed task-list result
     * @throws IllegalArgumentException if a parameter is null or a source task
     *                                  is null
     */
    public static TaskList fromTasks(
            List<Task> tasks,
            LocalDate searchDate,
            Predicate<Task> matcher) {
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks cannot be null.");
        }
        if (matcher == null) {
            throw new IllegalArgumentException("Matcher cannot be null.");
        }
        if (tasks.stream().anyMatch(task -> task == null)) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        TaskList result = new TaskList(searchDate);
        IntStream.range(0, tasks.size())
                .filter(index -> matcher.test(tasks.get(index)))
                .forEach(index -> result.addTask(
                        index + 1,
                        tasks.get(index)));
        return result;
    }

    /**
     * Adds a task together with its original one-based task number.
     *
     * @param taskNumber original task number
     * @param task task to add
     * @throws IllegalArgumentException if the task number is not positive or
     *                                  {@code task} is null
     */
    public void addTask(int taskNumber, Task task) {
        indexedTasks.add(new IndexedTask(taskNumber, task));
    }

    /**
     * Returns whether this task list contains no matching tasks.
     *
     * @return true if no indexed tasks are stored
     */
    public boolean isEmpty() {
        return indexedTasks.isEmpty();
    }

    /**
     * Returns the date used by the query, if one was supplied.
     *
     * @return optional date-search context
     */
    public Optional<LocalDate> getSearchDate() {
        return searchDate;
    }

    /**
     * Formats the indexed tasks as newline-separated display lines.
     *
     * @return indexed task lines, or an empty string when there are no tasks
     */
    public String toDisplayString() {
        return indexedTasks.stream()
                .map(IndexedTask::toDisplayString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Stores a task together with its original one-based task number.
     *
     * @param taskNumber original task number
     * @param task task being represented
     */
    public record IndexedTask(int taskNumber, Task task) {
        /** Validates the invariant of an indexed task. */
        public IndexedTask {
            if (taskNumber <= 0) {
                throw new IllegalArgumentException(
                        "Task number must be positive.");
            }
            if (task == null) {
                throw new IllegalArgumentException("Task cannot be null.");
            }
        }

        /**
         * Returns this task in the format used by task-list messages.
         *
         * @return one-based task number followed by the task text
         */
        public String toDisplayString() {
            return taskNumber + "." + task;
        }
    }
}
