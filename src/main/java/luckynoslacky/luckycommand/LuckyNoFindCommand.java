package luckynoslacky.luckycommand;

import java.time.LocalDateTime;

import luckynoslacky.luckytask.TaskMaster;

/**
 * Represents a command that searches tasks occurring on a date.
 */
public class LuckyNoFindCommand extends LuckyNoCommand {
    private final LocalDateTime searchDateTime;
    private final TaskMaster taskMaster;

    /**
     * Creates a find command.
     *
     * @param searchDateTime date and time from the search query
     */
    public LuckyNoFindCommand(LocalDateTime searchDateTime) {
        this(searchDateTime, null);
    }

    /**
     * Creates a find command bound to a task master.
     *
     * @param searchDateTime date and time to search
     * @param taskMaster task master containing the tasks to search
     */
    public LuckyNoFindCommand(LocalDateTime searchDateTime, TaskMaster taskMaster) {
        super(CommandType.FIND);
        this.taskMaster = taskMaster;
        if (searchDateTime == null) {
            throw new IllegalArgumentException("Search date cannot be null.");
        }
        this.searchDateTime = searchDateTime;
    }

    /**
     * Searches the task master and returns the matching task response.
     *
     * @return search response
     */
    @Override
    public String execute() {
        return this.taskMaster.searchTasks(searchDateTime);
    }
}
