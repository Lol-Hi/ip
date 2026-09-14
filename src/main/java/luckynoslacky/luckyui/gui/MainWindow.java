package luckynoslacky.luckyui.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        configureKeyboardTraversal();
        Platform.runLater(userInput::requestFocus);
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
        addChatbotMessage(
                LuckyNoMessages.greeting(),
                DialogueBox.DialogueType.CHATBOT);

        if (chatbot.hasLoadError()) {
            addChatbotMessage(
                    LuckyNoMessages.loadErrorMessage(),
                    DialogueBox.DialogueType.WARNING);
        }
    }

    /**
     * Processes a command entered by the user.
     */
    @FXML
    private void handleUserInput() {
        String userInputText = userInput.getText().trim();
        if (userInputText.isEmpty()) {
            userInput.requestFocus();
            return;
        }

        addUserMessage(userInputText);
        LuckyNoSlacky.ChatResponse response = chatbot.getResponse(userInputText);
        DialogueBox.DialogueType dialogueType = response.severity()
                == LuckyNoSlacky.ResponseSeverity.WARNING
                ? DialogueBox.DialogueType.WARNING
                : DialogueBox.DialogueType.CHATBOT;
        addChatbotMessage(response.message(), dialogueType);
        userInput.clear();

        if (response.shouldExit()) {
            userInput.setDisable(true);
            sendButton.setDisable(true);
            scrollPane.requestFocus();

            PauseTransition pause = new PauseTransition(
                    Duration.seconds(EXIT_DELAY_SECONDS));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        } else {
            userInput.requestFocus();
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
                        message,
                        userImage,
                        DialogueBox.DialogueType.USER));
    }

    /**
     * Adds an application message using the appropriate visual role.
     *
     * @param message chatbot response
     * @param dialogueType visual role for the response
     */
    private void addChatbotMessage(
            String message,
            DialogueBox.DialogueType dialogueType) {
        dialogContainer.getChildren().add(
                new DialogueBox(
                        message,
                        chatbotImage,
                        dialogueType));
    }

    /** Configures keyboard traversal for the main interactive controls. */
    private void configureKeyboardTraversal() {
        configureTraversal(userInput, sendButton, scrollPane);
        configureTraversal(sendButton, scrollPane, userInput);
        configureTraversal(scrollPane, userInput, sendButton);
    }

    /**
     * Configures forward and reverse Tab traversal for one interactive node.
     *
     * @param currentNode node receiving the Tab key
     * @param forwardNode node focused by Tab
     * @param backwardNode node focused by Shift+Tab
     */
    private void configureTraversal(
            Node currentNode,
            Node forwardNode,
            Node backwardNode) {
        currentNode.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                Node nextNode = event.isShiftDown() ? backwardNode : forwardNode;
                nextNode.requestFocus();
                event.consume();
            }
        });
    }
}
