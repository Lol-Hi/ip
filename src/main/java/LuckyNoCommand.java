/**
 * Represents a parsed chatbot command.
 */
public class LuckyNoCommand {
    /**
     * Represents the internal operation requested by a parsed command.
     */
    public enum CommandType {
        BYE,
        LIST,
        CREATE_TASK,
        TOGGLE_TASK,
        DELETE_TASK
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
     * Returns the internal command type.
     *
     * @return command type
     */
    public CommandType getCommandType() {
        return commandType;
    }
}
