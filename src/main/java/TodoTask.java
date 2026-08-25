import java.util.List;
import java.time.LocalDate;

/**
 * Represents a task without any date or time information.
 */
public class TodoTask extends Task {

    /**
     * Creates an incomplete ToDo task.
     *
     * @param description task description
     */
    public TodoTask(String description) {
        super(description);
    }

    @Override
    public List<String> getCSVStorageFields() {
        return createCSVStorageFields('T', null, null);
    }

    @Override
    public boolean occursOn(LocalDate date) {
        return false;
    }

    /**
     * Returns the ToDo display representation.
     *
     * @return ToDo type marker followed by the common task representation
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
