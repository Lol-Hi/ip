/**
 * Represents a request to delete a task.
 */
public class LuckyNoDeleteCommand extends LuckyNoCommand {
    private final int taskNumber;

    /**
     * Creates a task-deletion command.
     *
     * @param taskNumber one-based task number
     */
    public LuckyNoDeleteCommand(int taskNumber) {
        super(CommandType.DELETE_TASK);
        this.taskNumber = taskNumber;
    }

    /**
     * Returns the one-based task number.
     *
     * @return task number
     */
    public int getTaskNumber() {
        return taskNumber;
    }
}
