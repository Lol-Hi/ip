import java.util.Scanner;

/**
 * Starts the LuckyNoSlacky chatbot.
 */

public class LuckyNoSlacky {
    private static final String DIVIDER = "  ____________________________________________________________\n";

    private final Scanner userScanner;
    private final TaskMaster tmLucky;
    private final LuckyNoScanner luckyNoScanner;

    LuckyNoSlacky() {
        userScanner = new Scanner(System.in);
        tmLucky = new TaskMaster();
        luckyNoScanner = new LuckyNoScanner();
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

    private void handleTaskToggle(LuckyNoMarkCommand command) {
        boolean markDone = command.shouldMarkDone();
        String task = markDone
                ? tmLucky.markTaskDone(command.getTaskNumber())
                : tmLucky.unmarkTaskUndone(command.getTaskNumber());

        String message = markDone
                ? LuckyNoMessages.markedTaskMessage(task)
                : LuckyNoMessages.unmarkedTaskMessage(task);

        printReply(message);
    }

    private void addTask(Task task) {
        tmLucky.addTask(task);

        printReply(LuckyNoMessages.addedTaskMessage(
                task, tmLucky.getTaskCount()));
    }

    private void chatLoop() {
        while (userScanner.hasNextLine()) {
            String userInput = userScanner.nextLine();
            try {
                LuckyNoCommand command = luckyNoScanner.parseCommand(
                        userInput, tmLucky.getTaskCount());
                switch (command.getCommandName()) {
                case "bye":
                    return;
                case "list":
                    printReply(tmLucky.listTasks());
                    break;
                case "createTask":
                    addTask(((LuckyNoTaskCommand) command).getTask());
                    break;
                case "toggleTask":
                    handleTaskToggle((LuckyNoMarkCommand) command);
                    break;
                default:
                    throw new IllegalStateException("Unknown parsed command.");
                }
            } catch (LuckyNoInputError exception) {
                printReply(exception.getMessage());
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
