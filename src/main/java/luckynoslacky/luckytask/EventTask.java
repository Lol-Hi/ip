package luckynoslacky.luckytask;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a task with a specified start and end time.
 */
public class EventTask extends Task {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    /**
     * Creates an incomplete event task.
     *
     * @param description task description
     * @param startTime event start date and time
     * @param endTime event end date and time
     */
    public EventTask(String description, LocalDateTime startTime, LocalDateTime endTime) {
        super(description);

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Event times cannot be empty.");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Event end cannot be before its start.");
        }

        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the CSV fields for this event.
     *
     * @return event fields in CSV column order
     */
    @Override
    public List<String> getCsvStorageFields() {
        return createCsvStorageFields('E', startTime, endTime);
    }

    /**
     * Checks whether the event spans the supplied date.
     *
     * @param date date to check
     * @return true if the event occurs on the supplied date
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(startTime.toLocalDate())
                && !date.isAfter(endTime.toLocalDate());
    }

    /**
     * Returns the event display representation.
     *
     * @return event type marker, common task representation, and event times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + Task.formatDateTime(startTime)
                + " to: " + Task.formatDateTime(endTime) + ")";
    }
}
