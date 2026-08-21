/**
 * Represents a task that must be completed by a specified time.
 */
public class DeadlineTask extends Task {
    private final String byTime;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description task description
     * @param byTime deadline description
     */
    public DeadlineTask(String description, String byTime) {
        super(description);

        if (byTime == null || byTime.isBlank()) {
            throw new IllegalArgumentException("Deadline cannot be empty.");
        }

        this.byTime = byTime;
    }

    /**
     * Returns the deadline display representation.
     *
     * @return deadline type marker, common task representation, and deadline
     */
    @Override
    public String toString() {
        return "[D]" + super.toString()
                + " (by: " + byTime + ")";
    }
}
