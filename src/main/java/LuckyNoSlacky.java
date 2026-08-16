import java.util.Scanner;

/**
 * Starts the LuckyNoSlacky chatbot.
 */

public class LuckyNoSlacky {
    private static final String DIVIDER = "\n  ____________________________________________________________\n";

    private Scanner userScanner;

    LuckyNoSlacky() {
        userScanner = new Scanner(System.in);
    }
    private static void printReply(String output) {
        System.out.print(DIVIDER + "  " + output + DIVIDER);
    }

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
        String hello = "Hello, I'm LuckyNoSlacky!\n  What can I do for you?";
        System.out.print(DIVIDER + banner);
        printReply(hello);
    }

    private void exit() {
        String goodbye = "Bye, hope to see you again soon!";
        printReply(goodbye);
    }

    private void chatLoop() {
        while (userScanner.hasNextLine()) {
            String userInput = userScanner.nextLine();
            if (userInput.equalsIgnoreCase("bye")) {
                return;
            }
            echo(userInput);
        }
        // TODO: possible improvement: for the chat loop to return error codes for exit handle
    }

    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky();
        lucky.greet();
        lucky.chatLoop();
        lucky.exit();
    }
}
