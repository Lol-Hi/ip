/**
 * Represents a parsed chatbot command.
 */
public abstract class LuckyNoCommand {
    /**
     * Represents the internal operation requested by a parsed command.
     */
    public enum CommandType {
        BYE,
        LIST,
        CREATE_TASK,
        TOGGLE_TASK,
        DELETE_TASK,
        FIND
    }

    private final CommandType commandType;

    /**
     * Creates a parsed command.
     *
     * @param commandType internal type used to dispatch the command
     */
    protected LuckyNoCommand(CommandType commandType) {
        this.commandType = commandType;
    }

    /** Executes this command and returns its user-facing reply. */
    public abstract String execute();

    /** Indicates whether the chat loop should terminate after execution. */
    public boolean requestsExit() {
        return false;
    }
}
