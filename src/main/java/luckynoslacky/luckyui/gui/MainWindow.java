package luckynoslacky.luckyui.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import luckynoslacky.LuckyNoSlacky;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Controls the main LuckyNoSlacky JavaFX window.
 */
public class MainWindow {
    private static final double EXIT_DELAY_SECONDS = 1.5;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    private LuckyNoSlacky chatbot;
    private final ExitScheduler exitScheduler;
    private final Runnable exitAction;
    private boolean exitScheduled;

    private final Image chatbotImage = new Image(
            MainWindow.class.getResourceAsStream(
                    "/images/luckynoslacky.jpg"));

    private final Image userImage = new Image(
            MainWindow.class.getResourceAsStream("/images/user.png"));

    /**
     * Creates the main-window controller with the production exit behavior.
     */
    public MainWindow() {
        this(MainWindow::scheduleExit, Platform::exit);
    }

    /**
     * Creates the main-window controller with injectable exit behavior.
     *
     * <p>The package-private dependencies allow GUI tests to observe delayed
     * exit without terminating the JavaFX test toolkit.</p>
     *
     * @param exitScheduler scheduler used to delay application exit
     * @param exitAction action performed after the delay
     * @throws IllegalArgumentException if either dependency is null
     */
    MainWindow(ExitScheduler exitScheduler, Runnable exitAction) {
        if (exitScheduler == null || exitAction == null) {
            throw new IllegalArgumentException(
                    "Exit scheduler and action cannot be null.");
        }
        this.exitScheduler = exitScheduler;
        this.exitAction = exitAction;
    }

    /**
     * Scrolls to the latest message whenever the conversation grows.
     */
    @FXML
    private void initialize() {
        dialogContainer.heightProperty().addListener((observable, oldHeight, newHeight) ->
                        scrollPane.setVvalue(1.0));
    }

    /**
     * Provides the chatbot used to process user commands.
     *
     * @param chatbot chatbot backend
     * @throws IllegalArgumentException if {@code chatbot} is null
     */
    public void setChatbot(LuckyNoSlacky chatbot) {
        if (chatbot == null) {
            throw new IllegalArgumentException("Chatbot cannot be null.");
        }
        this.chatbot = chatbot;
        addChatbotMessage(LuckyNoMessages.greeting());

        if (chatbot.hasLoadError()) {
            addChatbotMessage(LuckyNoMessages.loadErrorMessage());
        }
    }

    /**
     * Processes a command entered by the user.
     */
    @FXML
    private void handleUserInput() {
        String userInputText = userInput.getText().trim();
        if (userInputText.isEmpty()) {
            return;
        }

        addUserMessage(userInputText);
        LuckyNoSlacky.ChatResponse response = chatbot.getResponse(userInputText);
        addChatbotMessage(response.message());
        userInput.clear();

        if (response.shouldExit() && !exitScheduled) {
            exitScheduled = true;
            userInput.setDisable(true);
            sendButton.setDisable(true);
            exitScheduler.schedule(
                    Duration.seconds(EXIT_DELAY_SECONDS), exitAction);
        }
    }

    /**
     * Schedules an action on the JavaFX timeline after the requested delay.
     *
     * @param delay delay before running the action
     * @param action action to run after the delay
     */
    private static void scheduleExit(Duration delay, Runnable action) {
        PauseTransition pause = new PauseTransition(delay);
        pause.setOnFinished(event -> action.run());
        pause.play();
    }

    /** Schedules a delayed action used by the main window's exit flow. */
    @FunctionalInterface
    interface ExitScheduler {
        /**
         * Schedules an action to run after a delay.
         *
         * @param delay delay before running the action
         * @param action action to run after the delay
         */
        void schedule(Duration delay, Runnable action);
    }

    /**
     * Adds a user message aligned to the right.
     *
     * @param message user message
     */
    private void addUserMessage(String message) {
        dialogContainer.getChildren().add(
                new DialogueBox(
                        "You said:",
                        message,
                        userImage,
                        Pos.CENTER_RIGHT,
                        DialogueBox.USER_DIALOGUE_STYLE));
    }

    /**
     * Adds a chatbot message aligned to the left.
     *
     * @param message chatbot response
     */
    private void addChatbotMessage(String message) {
        dialogContainer.getChildren().add(
                new DialogueBox(
                        "LuckyNoSlacky said:",
                        message,
                        chatbotImage,
                        Pos.CENTER_LEFT,
                        DialogueBox.CHATBOT_DIALOGUE_STYLE));
    }
}
