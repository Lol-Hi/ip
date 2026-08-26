package luckynoslacky.luckytask;

import java.util.List;
import java.time.LocalDate;
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

    /**
     * Returns the CSV fields for this deadline.
     *
     * @return deadline fields in CSV column order
     */
    @Override
    public List<String> getCSVStorageFields() {
        return createCSVStorageFields('D', null, byTime);
    }

    /**
     * Checks whether the deadline falls on the supplied date.
     *
     * @param date date to check
     * @return true if the deadline is on the supplied date
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return byTime.toLocalDate().equals(date);
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
