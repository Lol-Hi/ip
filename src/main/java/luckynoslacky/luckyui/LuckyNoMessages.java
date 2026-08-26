package luckynoslacky.luckyui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import luckynoslacky.luckyparser.LuckyNoParser;
import luckynoslacky.luckytask.Task;

/**
 * Stores and formats all messages that can be shown to the user.
 */
public final class LuckyNoMessages {
    private static final String DEADLINE_FORMAT =
            "<description> /by <date/time>.";
    private static final String EVENT_FORMAT =
            "<description> /from <start> /to <end>.";
    private static final String FIND_FORMAT = "/on <date>";
    private static final DateTimeFormatter FIND_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);

    private LuckyNoMessages() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Returns a message for an invalid command format.
     *
     * @param commandName command whose format is invalid
     * @return formatted error message
     */
    public static String invalidFormatMessage(
            LuckyNoParser.CommandName commandName) {
        if (commandName == null) {
            throw new IllegalArgumentException("Command name cannot be null.");
        }

        String format = switch (commandName) {
            case DEADLINE -> DEADLINE_FORMAT;
            case EVENT -> EVENT_FORMAT;
            case FIND -> FIND_FORMAT;
            default -> throw new IllegalArgumentException(
                    "No format is defined for this command.");
        };

        return "Eh HELLO you know how to type command one anot? \n"
                + "Lai lai let me teach you: "
                + commandName.getInputName() + " " + format;
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
     * Returns the expected find command format.
     *
     * @return find format
     */
    public static String findFormat() {
        return FIND_FORMAT;
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
                + "I only understand todo, deadline, event, list, mark, unmark, "
                + "delete, find, or bye, ok?";
    }

    /**
     * Returns the message shown when saved tasks cannot be loaded.
     *
     * @return task-loading error message
     */
    public static String loadErrorMessage() {
        return "Eh you so free ah, no tasks were loaded! "
                + "If you think this is salah, check your task data file.";
    }

    /**
     * Returns the message shown when tasks cannot be saved.
     *
     * @return task-saving error message
     */
    public static String saveErrorMessage() {
        return "Honggan la your system abit rabs ah, I cannot save your task";
    }

    /**
     * Returns the message shown for an invalid date or time.
     *
     * @return invalid date/time message
     */
    public static String invalidDateTimeMessage() {
        return "Eh mr smart alec you tell me your calendar and clock got tell you "
                + "time like this one meh?";
    }

    /**
     * Returns the message shown when a task's time order is impossible.
     *
     * @return time-order error message
     */
    public static String timeTravelMessage() {
        return "you think you time travelling issit? check your date and time properly hor!";
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
     * Formats the response after deleting a task.
     *
     * @param task formatted task that was deleted
     * @param taskCount number of remaining tasks
     * @return deletion response
     */
    public static String deletedTaskMessage(String task, int taskCount) {
        return "Solid man can don't care about this one already:\n"
                + "  " + task
                + "\nBut you still got " + taskCount
                + " tasks to settle.";
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

    /**
     * Returns the heading for a date-search result.
     *
     * @param searchDate date used for the search
     * @return search-list heading
     */
    public static String findTasksListHeader(LocalDate searchDate) {
        if (searchDate == null) {
            throw new IllegalArgumentException("Search date cannot be null.");
        }
        return "Nah, all these stuff you need to do on: "
                + FIND_DATE_FORMAT.format(searchDate);
    }

    /**
     * Returns the message for a search with no matching deadline or event.
     *
     * @return no-match message
     */
    public static String noMatchingTasksMessage() {
        return "Wah, you very free hor, got nothing to do sia!";
    }
}
