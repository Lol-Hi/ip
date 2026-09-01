package luckynoslacky.luckycommand;

import luckynoslacky.luckytask.TaskMaster;

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
     * @param commandType internal type associated with the command
     */
    protected LuckyNoCommand(CommandType commandType) {
        this.commandType = commandType;
    }

    /**
     * Validates a task master dependency before storing it in a command.
     *
     * @param taskMaster task master required by the command
     * @return the non-null task master
     * @throws IllegalArgumentException if {@code taskMaster} is null
     */
    protected static TaskMaster requireTaskMaster(TaskMaster taskMaster) {
        if (taskMaster == null) {
            throw new IllegalArgumentException("Task master cannot be null.");
        }
        return taskMaster;
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
    public boolean shouldExit() {
        return false;
    }
}
