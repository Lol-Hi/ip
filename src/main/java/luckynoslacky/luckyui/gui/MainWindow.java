package luckynoslacky.luckyui.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

    private LuckyNoSlacky chatbot;

    private final Image chatbotImage = new Image(
            MainWindow.class.getResourceAsStream(
                    "/images/luckynoslacky.jpg"));

    private final Image userImage = new Image(
            MainWindow.class.getResourceAsStream("/images/user.png"));

    /**
     * Creates the main-window controller.
     */
    public MainWindow() {
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

        if (response.shouldExit()) {
            userInput.setDisable(true);

            PauseTransition pause = new PauseTransition(
                    Duration.seconds(EXIT_DELAY_SECONDS));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
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
