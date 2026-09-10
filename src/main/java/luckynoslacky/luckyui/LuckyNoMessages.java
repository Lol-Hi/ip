package luckynoslacky.luckyui;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import luckynoslacky.luckyparser.LuckyNoParser;
import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskList;

/**
 * Stores and formats all messages that can be shown to the user.
 */
public final class LuckyNoMessages {
    private static final String DEADLINE_FORMAT =
            "<description> /by <date/time>.";
    private static final String EVENT_FORMAT =
            "<description> /from <start date/time> /to <end date/time>.";
    private static final String FIND_FORMAT = "[<description>] [/on <date>]";
    private static final String SNOOZE_BY_FORMAT =
            "<taskNumber> [/by <duration>]";
    private static final String SNOOZE_TO_FORMAT =
            "<taskNumber> [/to <end date/time>]";
    private static final String RESCHED_DEADLINE_FORMAT =
            "<taskNumber> /to <date/time>";
    private static final String RESCHED_EVENT_FORMAT =
            "<taskNumber> /from <start date/time> /to <end date/time>";
    private static final String INVALID_DURATION_FORMAT =
            "Eh can you be more specific anot, what do you mean by \"%s\" sia?";
    private static final DateTimeFormatter FIND_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd uuuu", Locale.ENGLISH);

    private LuckyNoMessages() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Joins message lines with the newline separator used by the application.
     *
     * @param lines message lines in display order
     * @return one message containing the supplied lines
     */
    private static String joinMessageLines(String... lines) {
        return String.join("\n", lines);
    }

    /**
     * Returns a message for an invalid command format.
     *
     * @param commandName command whose format is invalid
     * @param validFormats one or more valid formats for the command; when no
     *     formats are supplied, the command's default format is used
     * @return formatted error message
     */
    public static String invalidFormatMessage(
            LuckyNoParser.CommandName commandName,
            String... validFormats) {
        if (commandName == null) {
            throw new IllegalArgumentException("Command name cannot be null.");
        }

        String[] formats = validFormats.length == 0
                ? new String[]{defaultFormat(commandName)}
                : validFormats;
        String format = String.join(" or ", formats);

        return joinMessageLines(
                "Eh HELLO you know how to type command one anot? ",
                "Lai lai let me teach you: `"
                        + commandName.getInputName() + " " + format + "`");
    }

    /**
     * Returns the default format for a command that supports format errors.
     *
     * @param commandName command whose default format is needed
     * @return default command format
     */
    private static String defaultFormat(LuckyNoParser.CommandName commandName) {
        return switch (commandName) {
            case DEADLINE -> DEADLINE_FORMAT;
            case EVENT -> EVENT_FORMAT;
            case FIND -> FIND_FORMAT;
            case SNOOZE -> SNOOZE_BY_FORMAT + "` or `snooze " + SNOOZE_TO_FORMAT;
            case RESCHED -> RESCHED_EVENT_FORMAT;
            default -> {
                assert false : "No format is defined for command: " + commandName;
                throw new IllegalArgumentException(
                        "No format is defined for this command.");
            }
        };
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
     * Returns the duration-based snooze format.
     *
     * @return duration-based snooze format
     */
    public static String snoozeByFormat() {
        return SNOOZE_BY_FORMAT;
    }

    /**
     * Returns the explicit-ending-time snooze format.
     *
     * @return explicit-ending-time snooze format
     */
    public static String snoozeToFormat() {
        return SNOOZE_TO_FORMAT;
    }

    /**
     * Returns the deadline rescheduling format.
     *
     * @return deadline rescheduling format
     */
    public static String reschedDeadlineFormat() {
        return RESCHED_DEADLINE_FORMAT;
    }

    /**
     * Returns the event rescheduling format.
     *
     * @return event rescheduling format
     */
    public static String reschedEventFormat() {
        return RESCHED_EVENT_FORMAT;
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
     * @param commandName command that received extra arguments
     * @return extra-arguments message
     */
    public static String extraArgumentsMessage(String commandName) {
        return "Why you so losor! Leave the "
                + commandName + " command to do its own thing lah";
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
                + "delete, find, snooze, resched, or bye, ok?";
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
     * Returns the message shown for unsupported decimal calendar durations.
     *
     * @return decimal calendar duration message
     */
    public static String decimalCalendarDurationMessage() {
        return "Paiseh bro... i cannot settle decimal values for years and months yet...";
    }

    /**
     * Returns a message for an unrecognised snooze duration.
     *
     * @param durationText duration text supplied by the user
     * @return message asking the user to clarify the duration
     */
    public static String invalidDurationMessage(String durationText) {
        return String.format(INVALID_DURATION_FORMAT, durationText);
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
     * Returns the message shown when a ToDo is used with a timed-task command.
     *
     * @param commandName snooze or reschedule command
     * @return ToDo rejection message
     */
    public static String cannotSnoozeOrRescheduleTodoMessage(
            LuckyNoParser.CommandName commandName) {
        if (commandName == null) {
            throw new IllegalArgumentException("Command name cannot be null.");
        }
        return "Eh mr blur sotong this task don't even have time for you to "
                + commandName.getInputName() + " la";
    }

    /**
     * Returns the response shown after snoozing a task.
     *
     * @param task updated task
     * @return snooze response
     */
    public static String snoozedTaskMessage(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        return joinMessageLines(
                "Nah here's your snooze you lazy bum, don't slack too much hor!",
                "  " + task);
    }

    /**
     * Returns the response shown after rescheduling a task.
     *
     * @param task updated task
     * @return rescheduling response
     */
    public static String rescheduledTaskMessage(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        return joinMessageLines(
                "Nah here's your resched you lazy bum, don't slack too much hor!",
                "  " + task);
    }

    /**
     * Returns the chatbot banner.
     *
     * @return banner text
     */
    public static String banner() {
        return joinMessageLines(
                "     .--\"\"\"\"\"--.",
                "   /  /^\\   /^\\  \\",
                "  |  .---------.  |",
                "  |  | | | | | |  |",
                "   \\ '---------' /",
                "     '-._____.-'",
                "    [NO SLACKING]",
                "  LuckyNoSlacky is here to help!");
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
        return joinMessageLines(
                "Got one more thing to remember ah: ",
                "  " + task,
                "Now you got " + taskCount + " tasks to settle.");
    }

    /**
     * Formats the response after marking a task done.
     *
     * @param formattedTask formatted task that was marked
     * @return mark response
     */
    public static String markedTaskMessage(String formattedTask) {
        return joinMessageLines(
                "Swee lah you're done with this task:",
                "  " + formattedTask);
    }

    /**
     * Formats the response after marking a task not done.
     *
     * @param formattedTask formatted task that was unmarked
     * @return unmark response
     */
    public static String unmarkedTaskMessage(String formattedTask) {
        return joinMessageLines(
                "Eh salah you're not done with this task ah, "
                        + "must remember to do ah!",
                "  " + formattedTask);
    }

    /**
     * Formats the response after deleting a task.
     *
     * @param formattedTask formatted task that was deleted
     * @param taskCount number of remaining tasks
     * @return deletion response
     */
    public static String deletedTaskMessage(String formattedTask, int taskCount) {
        return joinMessageLines(
                "Solid man can don't care about this one already:",
                "  " + formattedTask,
                "But you still got " + taskCount + " tasks to settle.");
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
     * Formats an indexed task list with the appropriate heading.
     *
     * @param taskList indexed task list to format
     * @return formatted task-list response
     */
    public static String listTasksMessage(TaskList taskList) {
        if (taskList == null) {
            throw new IllegalArgumentException("Task list cannot be null.");
        }

        if (taskList.isEmpty()) {
            return emptyTaskListMessage();
        }

        String header = taskList.getSearchDate()
                .map(date -> "Nah, all these things you need to do on: "
                        + FIND_DATE_FORMAT.format(date))
                .orElse("Nah, all these things you need to do:");

        return joinMessageLines(header, taskList.toDisplayString());
    }
}
