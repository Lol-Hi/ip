package luckynoslacky.luckyui.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import luckynoslacky.LuckyNoSlacky;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests observable interactions in the main JavaFX window. */
@ExtendWith(ApplicationExtension.class)
class MainWindowTest {
    private Stage stage;

    /** Loads the production FXML window before each test. */
    @Start
    void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(
                MainWindow.class.getResource("/view/MainWindow.fxml"));
        Parent root = loader.load();
        MainWindow controller = loader.getController();
        controller.setChatbot(new LuckyNoSlacky());

        stage.setScene(new Scene(root));
        stage.show();
    }

    /** Verifies that the window displays a user input and chatbot reply. */
    @Test
    void mainWindow_unknownCommand_displaysUserAndWarningMessages(FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("unknown").push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        assertEquals(3, dialogueContainer.getChildren().size());
        DialogueBox userDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(1);
        DialogueBox chatbotDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(2);
        assertEquals("unknown", getMessageText(userDialogue, 0));
        assertEquals(
                LuckyNoMessages.unknownCommandMessage(),
                getMessageText(chatbotDialogue, 1));
        assertTrue(userDialogue.getStyleClass().contains(
                DialogueBox.USER_DIALOGUE_STYLE));
        assertTrue(chatbotDialogue.getStyleClass().contains(
                DialogueBox.WARNING_DIALOGUE_STYLE));
    }

    /** Verifies that the input control remains enabled after a non-exit command. */
    @Test
    void mainWindow_unknownCommand_keepsInputEnabled(FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("unknown").push(KeyCode.ENTER);

        assertFalse(inputField.isDisabled());
        assertTrue(inputField.getText().isEmpty());
    }

    /** Verifies that clicking Send submits the command like pressing Enter. */
    @Test
    void mainWindow_sendButton_submitsCommand(FxRobot robot) {
        robot.clickOn("#userInput")
                .write("unknown")
                .clickOn("#sendButton");

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        assertEquals(3, dialogueContainer.getChildren().size());
    }

    /** Verifies that the main controls expose the drafted accessible labels. */
    @Test
    void mainWindow_controls_exposeAccessibleLabels() {
        TextField inputField = lookupTextField();
        Button sendButton = lookupButton();
        ScrollPane conversationHistory = lookupScrollPane();

        assertEquals(
                "Lai tell me what you want me to do, then click Send",
                inputField.getAccessibleText());
        assertEquals(
                "You can also press Enter to send the command without using the mouse.",
                inputField.getAccessibleHelp());
        assertEquals("Send command", sendButton.getAccessibleText());
        assertEquals(
                "Sends the command currently entered in the command field.",
                sendButton.getAccessibleHelp());
        assertEquals(
                "Here you see the whole chat history! Just use your arrow "
                        + "or page keys to scroll up scroll down can liao",
                conversationHistory.getAccessibleText());
    }

    /** Verifies that the command field receives focus when the window opens. */
    @Test
    void mainWindow_startApplication_focusesCommandInput(FxRobot robot) {
        robot.interact(() -> assertTrue(lookupTextField().isFocused()));
    }

    /** Verifies that Tab follows the specified forward focus order. */
    @Test
    void mainWindow_tabTraversal_movesForwardThroughControls(FxRobot robot) {
        TextField inputField = lookupTextField();
        Button sendButton = lookupButton();
        ScrollPane conversationHistory = lookupScrollPane();

        robot.clickOn(inputField).push(KeyCode.TAB);
        assertTrue(sendButton.isFocused());

        robot.push(KeyCode.TAB);
        assertTrue(conversationHistory.isFocused());

        robot.push(KeyCode.TAB);
        assertTrue(inputField.isFocused());
    }

    /** Verifies that Shift+Tab follows the specified reverse focus order. */
    @Test
    void mainWindow_tabTraversal_movesBackwardThroughControls(FxRobot robot) {
        TextField inputField = lookupTextField();
        Button sendButton = lookupButton();
        ScrollPane conversationHistory = lookupScrollPane();

        robot.clickOn(inputField).push(KeyCode.TAB);
        robot.push(KeyCode.TAB);
        assertTrue(conversationHistory.isFocused());

        robot.press(KeyCode.SHIFT).push(KeyCode.TAB).release(KeyCode.SHIFT);
        assertTrue(sendButton.isFocused());

        robot.press(KeyCode.SHIFT).push(KeyCode.TAB).release(KeyCode.SHIFT);
        assertTrue(inputField.isFocused());
    }

    /** Verifies that a Send-button submission restores focus to the input. */
    @Test
    void mainWindow_sendButtonSubmission_returnsFocusToCommandInput(FxRobot robot) {
        robot.clickOn("#userInput")
                .write("unknown")
                .clickOn("#sendButton");

        assertTrue(lookupTextField().isFocused());
    }

    /** Verifies that submitting blank input leaves the conversation unchanged. */
    @Test
    void mainWindow_blankInput_doesNotAddDialogue(FxRobot robot) {
        VBox dialogueContainer = robot.lookup("#dialogContainer").query();

        robot.clickOn("#sendButton");

        assertEquals(1, dialogueContainer.getChildren().size());
    }

    /** Verifies that the main window rejects a missing chatbot dependency. */
    @Test
    void setChatbot_nullChatbot_throwsIllegalArgumentException() {
        MainWindow controller = new MainWindow();

        assertThrows(IllegalArgumentException.class, () ->
                controller.setChatbot(null));
    }

    /** Returns the message text from a dialogue row's text container. */
    private String getMessageText(DialogueBox dialogue, int dialogueIndex) {
        VBox messageContainer = (VBox) dialogue.getChildren().get(dialogueIndex);
        return ((Label) messageContainer.getChildren().get(0)).getText();
    }

    /** Returns the command input from the production FXML scene. */
    private TextField lookupTextField() {
        return (TextField) stage.getScene().lookup("#userInput");
    }

    /** Returns the Send button from the production FXML scene. */
    private Button lookupButton() {
        return (Button) stage.getScene().lookup("#sendButton");
    }

    /** Returns the conversation history from the production FXML scene. */
    private ScrollPane lookupScrollPane() {
        return (ScrollPane) stage.getScene().lookup("#scrollPane");
    }
}
