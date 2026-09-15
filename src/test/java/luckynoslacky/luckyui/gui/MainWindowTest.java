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
import org.testfx.util.WaitForAsyncUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import luckynoslacky.LuckyNoSlacky;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests observable interactions in the main JavaFX window. */
@ExtendWith(ApplicationExtension.class)
class MainWindowTest {
    private MainWindow controller;
    private Stage stage;

    /** Loads the production FXML window before each test. */
    @Start
    void start(Stage stage) throws IOException {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(
                MainWindow.class.getResource("/view/MainWindow.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setChatbot(new LuckyNoSlacky());

        stage.setScene(new Scene(root));
        stage.show();
    }

    /** Verifies that the window displays a user input and chatbot reply. */
    @Test
    void mainWindow_unknownCommand_displaysBothSpeakerMessages(FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("unknown").push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        assertEquals(3, dialogueContainer.getChildren().size());
        DialogueBox userDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(1);
        DialogueBox chatbotDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(2);
        assertEquals("You said:", getSpeakerLabel(userDialogue, 0));
        assertEquals("LuckyNoSlacky said:", getSpeakerLabel(chatbotDialogue, 1));
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

    /** Verifies that submitting blank input leaves the conversation unchanged. */
    @Test
    void mainWindow_blankInput_doesNotAddDialogue(FxRobot robot) {
        VBox dialogueContainer = robot.lookup("#dialogContainer").query();

        robot.clickOn("#sendButton");

        assertEquals(1, dialogueContainer.getChildren().size());
    }

    /** Verifies that a long conversation scrolls to its latest response. */
    @Test
    void mainWindow_manyMessages_scrollsToLatestDialogue(FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();

        for (int messageNumber = 0; messageNumber < 8; messageNumber++) {
            robot.interact(() -> {
                inputField.setText("unknown");
                inputField.fireEvent(new ActionEvent());
            });
        }
        WaitForAsyncUtils.waitForFxEvents();

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        ScrollPane scrollPane = robot.lookup("#scrollPane").query();
        DialogueBox latestDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);

        assertEquals(17, dialogueContainer.getChildren().size());
        assertEquals(
                LuckyNoMessages.unknownCommandMessage(),
                getMessageText(latestDialogue, 1));
        assertEquals(1.0, scrollPane.getVvalue(), 0.0001);
    }

    /** Verifies that resizing keeps the input controls within the scene. */
    @Test
    void mainWindow_resizeWindow_preservesLayoutInvariants(FxRobot robot) {
        AnchorPane root = (AnchorPane) stage.getScene().getRoot();
        Scene scene = stage.getScene();
        TextField inputField = robot.lookup("#userInput").query();
        Button sendButton = robot.lookup("#sendButton").query();

        robot.interact(() -> {
            stage.setWidth(800.0);
            stage.setHeight(700.0);
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(root.getWidth() > 400.0);
        assertTrue(root.getHeight() > 0.0);
        assertNodeWithinScene(scene, inputField);
        assertNodeWithinScene(scene, sendButton);

        robot.interact(() -> {
            stage.setWidth(1.0);
            stage.setHeight(1.0);
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(stage.getWidth() >= stage.getMinWidth());
        assertTrue(stage.getHeight() >= stage.getMinHeight());
        assertTrue(scene.getWidth() > 0.0);
        assertTrue(scene.getHeight() > 0.0);
        assertNodeWithinScene(scene, inputField);
        assertNodeWithinScene(scene, sendButton);

        robot.interact(() -> {
            stage.setWidth(400.0);
            stage.setHeight(600.0);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Verifies that a parser error is displayed as a chatbot message. */
    @Test
    void mainWindow_parseErrorResponse_displaysExactReply(FxRobot robot) {
        StubChatbot chatbot = new StubChatbot(
                false,
                new LuckyNoSlacky.ChatResponse("exact parse error", false));
        robot.interact(() -> controller.setChatbot(chatbot));

        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("trigger parse error").push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);
        assertEquals("exact parse error", getMessageText(chatbotDialogue, 1));
        assertFalse(inputField.isDisabled());
    }

    /** Verifies that a save error is displayed as a chatbot message. */
    @Test
    void mainWindow_saveErrorResponse_displaysExactReply(FxRobot robot) {
        StubChatbot chatbot = new StubChatbot(
                false,
                new LuckyNoSlacky.ChatResponse(
                        LuckyNoMessages.saveErrorMessage(), false));
        robot.interact(() -> controller.setChatbot(chatbot));

        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("trigger save error").push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);
        assertEquals(LuckyNoMessages.saveErrorMessage(),
                getMessageText(chatbotDialogue, 1));
        assertFalse(inputField.isDisabled());
    }

    /** Verifies that a startup load error is displayed after the greeting. */
    @Test
    void mainWindow_loadErrorChatbot_displaysExactStartupError(FxRobot robot) {
        StubChatbot chatbot = new StubChatbot(
                true,
                new LuckyNoSlacky.ChatResponse("unused response", false));
        robot.interact(() -> controller.setChatbot(chatbot));

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);
        assertEquals(LuckyNoMessages.loadErrorMessage(),
                getMessageText(chatbotDialogue, 1));
    }

    /** Verifies that the main window rejects a missing chatbot dependency. */
    @Test
    void setChatbot_nullChatbot_throwsIllegalArgumentException() {
        MainWindow controller = new MainWindow();

        assertThrows(IllegalArgumentException.class, () ->
                controller.setChatbot(null));
    }

    /** Returns the speaker label from a dialogue row's text container. */
    private String getSpeakerLabel(DialogueBox dialogue, int dialogueIndex) {
        VBox messageContainer = (VBox) dialogue.getChildren().get(dialogueIndex);
        return ((Label) messageContainer.getChildren().get(0)).getText();
    }

    /** Returns the message text from a dialogue row's text container. */
    private String getMessageText(DialogueBox dialogue, int dialogueIndex) {
        VBox messageContainer = (VBox) dialogue.getChildren().get(dialogueIndex);
        return ((Label) messageContainer.getChildren().get(1)).getText();
    }

    /** Verifies that a control remains inside the visible scene bounds. */
    private void assertNodeWithinScene(Scene scene, Node node) {
        Bounds sceneBounds = scene.getRoot().localToScene(
                scene.getRoot().getBoundsInLocal());
        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());

        assertTrue(nodeBounds.getMinX() >= sceneBounds.getMinX());
        assertTrue(nodeBounds.getMinY() >= sceneBounds.getMinY());
        assertTrue(nodeBounds.getMaxX() <= sceneBounds.getMaxX());
        assertTrue(nodeBounds.getMaxY() <= sceneBounds.getMaxY());
        assertTrue(nodeBounds.getWidth() > 0.0);
        assertTrue(nodeBounds.getHeight() > 0.0);
    }

    /** Supplies deterministic chatbot responses to GUI presentation tests. */
    private static final class StubChatbot extends LuckyNoSlacky {
        private final boolean loadError;
        private final ChatResponse response;

        StubChatbot(boolean loadError, ChatResponse response) {
            super();
            this.loadError = loadError;
            this.response = response;
        }

        /** Returns the configured startup-load result. */
        @Override
        public boolean hasLoadError() {
            return loadError;
        }

        /** Returns the configured response for any submitted command. */
        @Override
        public ChatResponse getResponse(String userInput) {
            return response;
        }
    }
}
