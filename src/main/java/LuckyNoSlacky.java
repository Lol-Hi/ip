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

        LuckyNoCSVSaver csvSaver = new LuckyNoCSVSaver();
        tmLucky = new TaskMaster(csvSaver);

        boolean failedToLoad = false;
        try {
            tmLucky.loadTasksFromCSVStorageRecord(csvSaver.load());
        } catch (LuckyNoStorageException exception) {
            failedToLoad = true;
        }
        loadError = failedToLoad;

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

    private void handleTaskDeletion(LuckyNoDeleteCommand command) {
        String task = tmLucky.deleteTask(command.getTaskNumber());
        printReply(LuckyNoMessages.deletedTaskMessage(
                task, tmLucky.getTaskCount()));
    }

    private void chatLoop() {
        while (userScanner.hasNextLine()) {
            String userInput = userScanner.nextLine();
            try {
                LuckyNoCommand command = luckyNoScanner.parseCommand(
                        userInput, tmLucky.getTaskCount());
                switch (command.getCommandType()) {
                case BYE:
                    return;
                case LIST:
                    printReply(tmLucky.listTasks());
                    break;
                case CREATE_TASK:
                    addTask(((LuckyNoTaskCommand) command).getTask());
                    break;
                case TOGGLE_TASK:
                    handleTaskToggle((LuckyNoMarkCommand) command);
                    break;
                case DELETE_TASK:
                    handleTaskDeletion((LuckyNoDeleteCommand) command);
                    break;
                case FIND:
                    LuckyNoFindCommand findCommand =
                            (LuckyNoFindCommand) command;
                    printReply(tmLucky.searchTasks(
                            findCommand.getSearchDateTime()));
                    break;
                default:
                    throw new IllegalStateException("Unknown parsed command.");
                }
            } catch (LuckyNoInputException exception) {
                printReply(exception.getMessage());
            } catch (LuckyNoStorageException exception) {
                printReply(LuckyNoMessages.saveErrorMessage());
            }
        }
    }

    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky();
        lucky.greet();

        if (lucky.loadError) {
            printReply(LuckyNoMessages.loadErrorMessage());
            return;
        }

        lucky.chatLoop();
        lucky.exit();
    }
}
