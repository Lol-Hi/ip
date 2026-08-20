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
     * Parses command arguments and creates an event task.
     *
     * @param arguments text following the event command
     * @return a parsed event task
     * @throws IllegalArgumentException if the arguments do not contain a
     *         description, a /from time, and a /to time
     */
    public static EventTask createEventTask(String arguments) {
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");

        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new IllegalArgumentException();
        }

        String description = arguments.substring(0, fromIndex).trim();
        String fromTime = arguments.substring(fromIndex + 5, toIndex).trim();
        String toTime = arguments.substring(toIndex + 3).trim();

        if (description.isEmpty() || fromTime.isEmpty() || toTime.isEmpty()) {
            throw new IllegalArgumentException();
        }

        return new EventTask(description, fromTime, toTime);
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
