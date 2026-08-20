import java.util.Scanner;

/**
 * Starts the LuckyNoSlacky chatbot.
 */

public class LuckyNoSlacky {
    private static final String DIVIDER = "  ____________________________________________________________\n";

    private final Scanner userScanner;
    private final TaskMaster tmLucky;

    LuckyNoSlacky() {
        userScanner = new Scanner(System.in);
        tmLucky = new TaskMaster();
    }

    private static void printReply(String output) {
        String indentedOutput = output.replace("\n", "\n  ");
        System.out.print(DIVIDER + "  " + indentedOutput + "\n" + DIVIDER);
    }

    /**
     * Echoes a piece of user input as a chatbot reply.
     *
     * @param input user input to echo
     */
    private static void echo(String input) {
        printReply(input);
    }

    private void greet() {
        String banner = "     .--\"\"\"\"\"--.\n"
                + "   /  /^\\   /^\\  \\\n"
                + "  |  .---------.  |\n"
                + "  |  | | | | | |  |\n"
                + "   \\ '---------' /\n"
                + "     '-._____.-'\n"
                + "    [NO SLACKING]\n"
                + "  LuckyNoSlacky is here to help!";
        String hello = "Hello, I'm LuckyNoSlacky!\nWhat can I do for you?";
        System.out.print(DIVIDER + banner + "\n");
        printReply(hello);
    }

    private void exit() {
        String goodbye = "Bye, hope to see you again soon!";
        printReply(goodbye);
    }

    private void handleTaskToggle(String[] commandParts, boolean markDone) {
        String command = markDone ? "mark" : "unmark";

        if (commandParts.length != 2) {
            printReply("Please provide a task number to " + command + ".");
            return;
        }

        try {
            int taskNumber = Integer.parseInt(commandParts[1]);
            String task = markDone
                    ? tmLucky.markTaskDone(taskNumber)
                    : tmLucky.unmarkTaskUndone(taskNumber);

            String message = markDone
                    ? "Nice! I've marked this task as done:"
                    : "OK, I've marked this task as not done yet:";

            printReply(message + "\n  " + task);
        } catch (IllegalArgumentException exception) {
            printReply("That task number is invalid.");
        }
    }

    private void addTask(Task task) {
        tmLucky.addTask(task);

        printReply("Got it. I've added this task:\n"
                + "  " + task
                + "\nNow you have " + tmLucky.getTaskCount()
                + " tasks in the list.");
    }

    private Task createDeadlineTask(String arguments) {
        int byIndex = arguments.indexOf("/by");

        if (byIndex <= 0) {
            throw new IllegalArgumentException(
                    "Please use: deadline <description> /by <date/time>.");
        }

        String description = arguments.substring(0, byIndex).trim();
        String byTime = arguments.substring(byIndex + 3).trim();

        if (description.isEmpty() || byTime.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please provide both a description and a deadline.");
        }

        return new Task(description, byTime);
    }

    private Task createEventTask(String arguments) {
        int fromIndex = arguments.indexOf("/from");
        int toIndex = arguments.indexOf("/to");

        if (fromIndex <= 0 || toIndex <= fromIndex) {
            throw new IllegalArgumentException(
                    "Please use: event <description> /from <start> /to <end>.");
        }

        String description = arguments.substring(0, fromIndex).trim();
        String fromTime = arguments.substring(fromIndex + 5, toIndex).trim();
        String toTime = arguments.substring(toIndex + 3).trim();

        if (description.isEmpty() || fromTime.isEmpty() || toTime.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please provide a description, start time, and end time.");
        }

        return new Task(description, fromTime, toTime);
    }

    private void chatLoop() {
        while (userScanner.hasNextLine()) {
            String userInput = userScanner.nextLine();
            String trimmedInput = userInput.trim();

            if (trimmedInput.equalsIgnoreCase("bye")) {
                return;
            }

            if (trimmedInput.isEmpty()) {
                printReply("Please enter a command.");
                continue;
            }

            String[] commandParts = trimmedInput.split("\\s+", 2);
            String command = commandParts[0].toLowerCase();
            String arguments = commandParts.length == 2
                    ? commandParts[1].trim()
                    : "";

            switch (command) {
            case "bye":
                if (arguments.isEmpty()) {
                    return;
                }
                printReply("The bye command does not take arguments.");
                break;
            case "list":
                if (arguments.isEmpty()) {
                    printReply(tmLucky.listTasks());
                } else {
                    printReply("The list command does not take arguments.");
                }
                break;
            case "todo":
                if (arguments.isEmpty()) {
                    printReply("Please provide a task description.");
                } else {
                    addTask(new Task(arguments));
                }
                break;
            case "deadline":
                try {
                    addTask(createDeadlineTask(arguments));
                } catch (IllegalArgumentException exception) {
                    printReply(exception.getMessage());
                }
                break;
            case "event":
                try {
                    addTask(createEventTask(arguments));
                } catch (IllegalArgumentException exception) {
                    printReply(exception.getMessage());
                }
                break;
            case "mark":
                handleTaskToggle(trimmedInput.split("\\s+"), true);
                break;
            case "unmark":
                handleTaskToggle(trimmedInput.split("\\s+"), false);
                break;
            default:
                printReply("I don't recognize that command. "
                        + "Please use todo, deadline, event, list, mark, unmark, or bye.");
                break;
            }
        }
    }

    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky();
        lucky.greet();
        lucky.chatLoop();
        lucky.exit();
    }
}
