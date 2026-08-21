/**
 * Represents either a mark or an unmark request.
 */
public class LuckyNoMarkCommand extends LuckyNoCommand {
    private final int taskNumber;
    private final boolean markDone;

    /**
     * Creates a command that sets a task's done status explicitly.
     *
     * @param taskNumber one-based task number
     * @param markDone whether the task should be marked done
     */
    public LuckyNoMarkCommand(int taskNumber, boolean markDone) {
        super("toggleTask");
        this.taskNumber = taskNumber;
        this.markDone = markDone;
    }

    /**
     * Returns the one-based task number.
     *
     * @return task number
     */
    public int getTaskNumber() {
        return taskNumber;
    }

    /**
     * Returns whether this command requests the task to be done.
     *
     * @return true for mark, false for unmark
     */
    public boolean shouldMarkDone() {
        return markDone;
    }
}
