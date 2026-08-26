package luckynoslacky.luckycommand;

/**
 * Represents a parsed chatbot command.
 */
public abstract class LuckyNoCommand {
    /**
     * Represents the internal operation requested by a parsed command.
     */
    public enum CommandType {
        /** Ends the chatbot session. */
        BYE,
        /** Lists all tasks. */
        LIST,
        /** Creates a task. */
        CREATE_TASK,
        /** Changes a task's done status. */
        TOGGLE_TASK,
        /** Deletes a task. */
        DELETE_TASK,
        /** Searches tasks by date. */
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

    /**
     * Executes this command and returns its user-facing reply.
     *
     * @return reply produced by the command
     */
    public abstract String execute();

    /**
     * Indicates whether the chat loop should terminate after execution.
     *
     * @return true if the command requests termination
     */
    public boolean requestsExit() {
        return false;
    }
}
