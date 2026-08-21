/**
 * Stores and formats all messages that can be shown to the user.
 */
public final class LuckyNoMessages {
    private static final String DEADLINE_FORMAT =
            "<description> /by <date/time>.";
    private static final String EVENT_FORMAT =
            "<description> /from <start> /to <end>.";

    private LuckyNoMessages() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Returns a message for an invalid command format.
     *
     * @param command command whose format is invalid
     * @param format expected command format
     * @return formatted error message
     */
    public static String invalidFormatMessage(String command, String format) {
        return "Eh HELLO you know how to type command one anot? \n"
                + "Lai lai let me teach you: " + command + " " + format;
    }

    /**
     * Returns the expected deadline command format.
     *
     * @return deadline format
     */
    public static String deadlineFormat() {
        return DEADLINE_FORMAT;
    }

    /**
     * Returns the expected event command format.
     *
     * @return event format
     */
    public static String eventFormat() {
        return EVENT_FORMAT;
    }

    /**
     * Returns the message for a missing task number.
     *
     * @return missing task number message
     */
    public static String missingTaskNumberMessage() {
        return "Eh which task you talking about har? Can say clearly anot.";
    }

    /**
     * Returns the message for an invalid task number entered by the user.
     *
     * @return invalid task number message
     */
    public static String invalidTaskNumberMessage() {
        return "You siao ah how to spin this task from thin air?";
    }

    /**
     * Returns the message for extra arguments after a command.
     *
     * @param command command that received extra arguments
     * @return extra-arguments message
     */
    public static String extraArgumentsMessage(String command) {
        return "Why you so losor! Leave the "
                + command + " command to do its own thing lah";
    }

    /**
     * Returns the message for a blank input line.
     *
     * @return missing command message
     */
    public static String missingCommandMessage() {
        return "Eh you mute issit?? Just say what you want lah!";
    }

    /**
     * Returns the message for a task command without a description.
     *
     * @return missing task description message
     */
    public static String missingTaskDescriptionMessage() {
        return "You don't tell me what to do how I know what to do???";
    }

    /**
     * Returns the message for an unrecognised command.
     *
     * @return unknown command message
     */
    public static String unknownCommandMessage() {
        return "What talking you? "
                + "I only understand todo, deadline, event, list, mark, unmark, or bye, ok?";
    }

    /**
     * Returns the chatbot banner.
     *
     * @return banner text
     */
    public static String banner() {
        return "     .--\"\"\"\"\"--.\n"
                + "   /  /^\\   /^\\  \\\n"
                + "  |  .---------.  |\n"
                + "  |  | | | | | |  |\n"
                + "   \\ '---------' /\n"
                + "     '-._____.-'\n"
                + "    [NO SLACKING]\n"
                + "  LuckyNoSlacky is here to help!";
    }

    /**
     * Returns the chatbot greeting.
     *
     * @return greeting message
     */
    public static String greeting() {
        return "Limpeh is LuckyNoSlacky, and I will confirm "
                + "make sure you're lucky and not slacky!";
    }

    /**
     * Returns the chatbot goodbye message.
     *
     * @return goodbye message
     */
    public static String goodbye() {
        return "Huh so fast zao ah, rest well ah!";
    }

    /**
     * Formats the response after adding a task.
     *
     * @param task task that was added
     * @param taskCount number of tasks after adding the task
     * @return task-added response
     */
    public static String addedTaskMessage(Task task, int taskCount) {
        return "Got one more thing to remember ah: \n"
                + "  " + task
                + "\nNow you got " + taskCount
                + " tasks to settle.";
    }

    /**
     * Formats the response after marking a task done.
     *
     * @param task formatted task that was marked
     * @return mark response
     */
    public static String markedTaskMessage(String task) {
        return "Swee lah you're done with this task:\n  " + task;
    }

    /**
     * Formats the response after marking a task not done.
     *
     * @param task formatted task that was unmarked
     * @return unmark response
     */
    public static String unmarkedTaskMessage(String task) {
        return "Eh salah you're not done with this task ah, "
                + "must remember to do ah!\n  " + task;
    }

    /**
     * Returns the empty task-list message.
     *
     * @return empty-list message
     */
    public static String emptyTaskListMessage() {
        return "Chill lah bro got nothing yet lah!";
    }

    /**
     * Returns the heading for a non-empty task list.
     *
     * @return task-list heading
     */
    public static String taskListHeader() {
        return "Nah all these stuff you need to do:";
    }
}
