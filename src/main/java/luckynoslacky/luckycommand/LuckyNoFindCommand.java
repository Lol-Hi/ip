package luckynoslacky.luckycommand;

import java.time.LocalDateTime;

import luckynoslacky.luckytask.TaskMaster;

/**
 * Represents a command that searches tasks by description, date, or both.
 */
public class LuckyNoFindCommand extends LuckyNoCommand {
    private final String descriptionQuery;
    private final LocalDateTime dateTimeQuery;
    private final TaskMaster taskMaster;

    /**
     * Creates a date-only find command.
     *
     * @param dateTimeQuery date and time from the search query
     */
    public LuckyNoFindCommand(LocalDateTime dateTimeQuery) {
        this(null, dateTimeQuery, null);
    }

    /**
     * Creates a date-only find command with a task master.
     *
     * @param dateTimeQuery date and time from the search query
     * @param taskMaster task master to search
     */
    public LuckyNoFindCommand(
            LocalDateTime dateTimeQuery,
            TaskMaster taskMaster) {
        this(null, dateTimeQuery, taskMaster);
    }

    /**
     * Creates a find command with optional description and date filters.
     * At least one filter must be supplied.
     *
     * @param descriptionQuery optional text to search for in task descriptions
     * @param dateTimeQuery optional date and time from the search query
     * @param taskMaster task master to search
     */
    public LuckyNoFindCommand(
            String descriptionQuery,
            LocalDateTime dateTimeQuery,
            TaskMaster taskMaster) {
        super(CommandType.FIND);
        if ((descriptionQuery == null || descriptionQuery.isBlank())
                && dateTimeQuery == null) {
            throw new IllegalArgumentException("Find query cannot be empty.");
        }
        this.descriptionQuery = descriptionQuery;
        this.dateTimeQuery = dateTimeQuery;
        this.taskMaster = taskMaster;
    }

    @Override
    public String execute() {
        return this.taskMaster.searchTasks(descriptionQuery, dateTimeQuery);
    }
}
