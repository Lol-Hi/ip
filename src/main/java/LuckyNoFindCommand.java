import java.time.LocalDateTime;

/**
 * Represents a command that searches tasks occurring on a date.
 */
public class LuckyNoFindCommand extends LuckyNoCommand {
    private final LocalDateTime searchDateTime;

    /**
     * Creates a find command.
     *
     * @param searchDateTime date and time from the search query
     */
    public LuckyNoFindCommand(LocalDateTime searchDateTime) {
        super(CommandType.FIND);

        if (searchDateTime == null) {
            throw new IllegalArgumentException("Search date cannot be null.");
        }

        this.searchDateTime = searchDateTime;
    }

    /**
     * Returns the parsed search date and time.
     *
     * @return search date and time
     */
    public LocalDateTime getSearchDateTime() {
        return searchDateTime;
    }
}
