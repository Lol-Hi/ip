import java.util.List;
import java.time.LocalDateTime;

/**
 * Represents a task that must be completed by a specified time.
 */
public class DeadlineTask extends Task {
    private final LocalDateTime byTime;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description task description
     * @param byTime deadline date and time
     */
    public DeadlineTask(String description, LocalDateTime byTime) {
        super(description);

        if (byTime == null) {
            throw new IllegalArgumentException("Deadline cannot be empty.");
        }

        this.byTime = byTime;
    }

    @Override
    public List<String> getCSVStorageFields() {
        return createCSVStorageFields('D', null, byTime);
    }

    /**
     * Returns the deadline display representation.
     *
     * @return deadline type marker, common task representation, and deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + Task.formatDateTime(byTime) + ")";
    }
}
