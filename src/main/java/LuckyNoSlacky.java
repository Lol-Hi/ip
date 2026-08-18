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

    private void chatLoop() {
        while (userScanner.hasNextLine()) {
            String userInput = userScanner.nextLine();
            String trimmedInput = userInput.trim();

            if (trimmedInput.equalsIgnoreCase("bye")) {
                return;
            }

            if (trimmedInput.equalsIgnoreCase("list")) {
                printReply(tmLucky.listTasks());
            } else if (trimmedInput.isEmpty()) {
                printReply("Please enter a command.");
            } else {
                String[] commandParts = trimmedInput.split("\\s+");
                String command = commandParts[0];

                if (command.equalsIgnoreCase("mark")) {
                    handleTaskToggle(commandParts, true);
                } else if (command.equalsIgnoreCase("unmark")) {
                    handleTaskToggle(commandParts, false);
                } else {
                    tmLucky.addTask(userInput);
                    printReply("added: " + userInput);
                }
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
