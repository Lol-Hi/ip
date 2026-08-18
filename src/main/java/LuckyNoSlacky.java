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
            } else if (trimmedInput.split("\\s+")[0].equalsIgnoreCase("mark")) {
                String[] commandParts = trimmedInput.split("\\s+");

                if (commandParts.length != 2) {
                    printReply("Please provide a task number to mark.");
                    continue;
                }

                try {
                    int taskNumber = Integer.parseInt(commandParts[1]);
                    String task = tmLucky.markTaskDone(taskNumber);
                    printReply("Nice! I've marked this task as done:\n  [X] " + task);
                } catch (IllegalArgumentException exception) {
                    printReply("That task number is invalid.");
                }
            } else {
                tmLucky.addTask(userInput);
                printReply("added: " + userInput);
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
