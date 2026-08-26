package luckynoslacky.luckyui;

import java.util.Scanner;

/**
 * Handles direct command-line interaction with the user.
 */
public class LuckyNoCLI {
    private static final String DIVIDER =
            "  ____________________________________________________________\n";

    private final Scanner scanner;

    /** Creates a command-line interface connected to standard input. */
    public LuckyNoCLI() {
        scanner = new Scanner(System.in);
    }

    /**
     * Checks whether another command is available from standard input.
     *
     * @return true if another input line is available
     */
    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    /**
     * Reads one command from standard input.
     *
     * @return the next user command
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Prints a user-facing reply with the chatbot's standard formatting.
     *
     * @param output reply to display
     */
    public void showReply(String output) {
        String indentedOutput = output.replace("\n", "\n  ");
        System.out.print(DIVIDER + "  " + indentedOutput + "\n" + DIVIDER);
    }

    /** Displays the chatbot banner and greeting. */
    public void showGreeting() {
        System.out.print(DIVIDER + LuckyNoMessages.banner() + "\n");
        showReply(LuckyNoMessages.greeting());
    }

    /** Displays the chatbot goodbye message. */
    public void showGoodbye() {
        showReply(LuckyNoMessages.goodbye());
    }

    /** Displays the message shown when task loading fails. */
    public void showLoadingError() {
        showReply(LuckyNoMessages.loadErrorMessage());
    }

    /** Displays the message shown when task saving fails. */
    public void showSavingError() {
        showReply(LuckyNoMessages.saveErrorMessage());
    }

    /**
     * Echoes input using the normal chatbot reply formatting.
     *
     * @param input input to echo
     */
    public void echo(String input) {
        showReply(input);
    }
}
