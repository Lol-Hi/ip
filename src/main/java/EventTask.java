/**
 * Represents a task with a specified start and end time.
 */
public class EventTask extends Task {
    private final String fromTime;
    private final String toTime;

    /**
     * Creates an incomplete event task.
     *
     * @param description task description
     * @param fromTime event start description
     * @param toTime event end description
     */
    public EventTask(String description, String fromTime, String toTime) {
        super(description);

        if (fromTime == null || fromTime.isBlank()
                || toTime == null || toTime.isBlank()) {
            throw new IllegalArgumentException("Event times cannot be empty.");
        }

        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    /**
     * Returns the event display representation.
     *
     * @return event type marker, common task representation, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + fromTime + " to: " + toTime + ")";
    }
}
