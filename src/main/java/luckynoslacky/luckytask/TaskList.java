package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Adds a task together with its original one-based task number.
     *
     * @param taskNumber original task number
     * @param task task to add
     * @throws IllegalArgumentException if the task number is not positive or
     *                                  {@code task} is null
     */
    public void addTask(int taskNumber, Task task) {
        if (taskNumber <= 0) {
            throw new IllegalArgumentException(
                    "Task number must be positive.");
        }

        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        indexedTasks.add(new IndexedTask(taskNumber, task));
    }

    /**
     * Returns whether this result contains no matching tasks.
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
                .map(indexedTask -> indexedTask.taskNumber()
                        + "." + indexedTask.task())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Stores a task together with its original one-based task number.
     *
     * @param taskNumber original task number
     * @param task task being represented
     */
    public record IndexedTask(int taskNumber, Task task) {
    }
}
