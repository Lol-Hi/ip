/**
 * Starts the LuckyNoSlacky chatbot.
 */

public class LuckyNoSlacky {
    private final LuckyNoCLI cliLucky;
    private final TaskMaster tmLucky;
    private final LuckyNoParser parserLucky;
    private final boolean loadError;

    LuckyNoSlacky() {
        cliLucky = new LuckyNoCLI();

        CSVSaver csvSaver = new CSVSaver();
        tmLucky = new TaskMaster(csvSaver);

        boolean failedToLoad = false;
        try {
            tmLucky.loadTasksFromCSVStorageRecord(csvSaver.load());
        } catch (LuckyNoStorageException exception) {
            failedToLoad = true;
        }
        loadError = failedToLoad;

        parserLucky = new LuckyNoParser(tmLucky);
    }

    private boolean chatLoop() {
        while (cliLucky.hasNextLine()) {
            String userInput = cliLucky.readCommand();
            try {
                LuckyNoCommand command = parserLucky.parseCommand(userInput);
                cliLucky.showReply(command.execute());
                if (command.requestsExit()) {
                    return true;
                }
            } catch (LuckyNoInputException exception) {
                cliLucky.showReply(exception.getMessage());
            } catch (LuckyNoStorageException exception) {
                cliLucky.showSavingError();
            }
        }
        return false;
    }

    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky();
        lucky.cliLucky.showGreeting();

        if (lucky.loadError) {
            lucky.cliLucky.showLoadingError();
            return;
        }

        if (!lucky.chatLoop()) {
            lucky.cliLucky.showGoodbye();
        }
    }
}
