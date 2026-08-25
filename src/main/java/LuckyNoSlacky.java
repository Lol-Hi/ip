import java.util.Scanner;

/**
 * Starts the LuckyNoSlacky chatbot.
 */

public class LuckyNoSlacky {
    private static final String DIVIDER = "  ____________________________________________________________\n";

    private final Scanner userScanner;
    private final TaskMaster tmLucky;
    private final LuckyNoScanner luckyNoScanner;
    private final boolean loadError;

    LuckyNoSlacky() {
        userScanner = new Scanner(System.in);

        CSVSaver csvSaver = new CSVSaver();
        tmLucky = new TaskMaster(csvSaver);

        boolean failedToLoad = false;
        try {
            tmLucky.loadTasksFromCSVStorageRecord(csvSaver.load());
        } catch (LuckyNoStorageException exception) {
            failedToLoad = true;
        }
        loadError = failedToLoad;

        luckyNoScanner = new LuckyNoScanner(tmLucky);
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
        System.out.print(DIVIDER + LuckyNoMessages.banner() + "\n");
        printReply(LuckyNoMessages.greeting());
    }

    private void exit() {
        printReply(LuckyNoMessages.goodbye());
    }

    private boolean chatLoop() {
        while (userScanner.hasNextLine()) {
            String userInput = userScanner.nextLine();
            try {
                LuckyNoCommand command = luckyNoScanner.parseCommand(userInput);
                printReply(command.execute());
                if (command.requestsExit()) {
                    return true;
                }
            } catch (LuckyNoInputException exception) {
                printReply(exception.getMessage());
            } catch (LuckyNoStorageException exception) {
                printReply(LuckyNoMessages.saveErrorMessage());
            }
        }
        return false;
    }

    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky();
        lucky.greet();

        if (lucky.loadError) {
            printReply(LuckyNoMessages.loadErrorMessage());
            return;
        }

        if (!lucky.chatLoop()) {
            lucky.exit();
        }
    }
}
