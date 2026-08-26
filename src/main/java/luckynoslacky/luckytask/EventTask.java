package luckynoslacky.luckytask;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a task with a specified start and end time.
 */
public class EventTask extends Task {
    private final LocalDateTime fromTime;
    private final LocalDateTime toTime;

    /**
     * Creates an incomplete event task.
     *
     * @param description task description
     * @param fromTime event start date and time
     * @param toTime event end date and time
     */
    public EventTask(String description, LocalDateTime fromTime, LocalDateTime toTime) {
        super(description);

        if (fromTime == null || toTime == null) {
            throw new IllegalArgumentException("Event times cannot be empty.");
        }
        if (toTime.isBefore(fromTime)) {
            throw new IllegalArgumentException("Event end cannot be before its start.");
        }

        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    @Override
    public List<String> getCSVStorageFields() {
        return createCSVStorageFields('E', fromTime, toTime);
    }

    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(fromTime.toLocalDate())
                && !date.isAfter(toTime.toLocalDate());
    }

    /**
     * Returns the event display representation.
     *
     * @return event type marker, common task representation, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + Task.formatDateTime(fromTime)
                + " to: " + Task.formatDateTime(toTime) + ")";
    }
}
