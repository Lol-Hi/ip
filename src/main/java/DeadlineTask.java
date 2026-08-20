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
     * Parses command arguments and creates a deadline task.
     *
     * @param arguments text following the deadline command
     * @return a parsed deadline task
     * @throws IllegalArgumentException if the arguments do not contain a
     *         description and a /by time
     */
    public static DeadlineTask createDeadlineTask(String arguments) {
        int byIndex = arguments.indexOf("/by");

        if (byIndex <= 0) {
            throw new IllegalArgumentException();
        }

        String description = arguments.substring(0, byIndex).trim();
        String byTime = arguments.substring(byIndex + 3).trim();

        if (description.isEmpty() || byTime.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return new DeadlineTask(description, byTime);
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
