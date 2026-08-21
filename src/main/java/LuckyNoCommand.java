/**
 * Represents a parsed chatbot command.
 */
public class LuckyNoCommand {
    private final String commandName;

    /**
     * Creates a parsed command.
     *
     * @param commandName internal name used to dispatch the command
     */
    protected LuckyNoCommand(String commandName) {
        this.commandName = commandName;
    }

    /**
     * Returns the internal command name.
     *
     * @return command name
     */
    public String getCommandName() {
        return commandName;
    }
}
