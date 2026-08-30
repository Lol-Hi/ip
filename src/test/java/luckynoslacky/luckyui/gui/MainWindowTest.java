package luckynoslacky.luckyui.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import luckynoslacky.LuckyNoSlacky;

/** Tests observable interactions in the main JavaFX window. */
@ExtendWith(ApplicationExtension.class)
class MainWindowTest {
    /** Loads the production FXML window before each test. */
    @Start
    void start(Stage stage) throws IOException {
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
    void mainWindow_unknownCommand_displaysBothSpeakerMessages(FxRobot robot) {
        TextField input = robot.lookup("#userInput").query();
        robot.clickOn(input).write("unknown").push(KeyCode.ENTER);

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
        TextField input = robot.lookup("#userInput").query();
        robot.clickOn(input).write("unknown").push(KeyCode.ENTER);

        assertFalse(input.isDisabled());
        assertTrue(input.getText().isEmpty());
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

    /** Returns the speaker label from a dialogue row's text container. */
    private String getSpeakerLabel(DialogueBox dialogue, int textIndex) {
        VBox text = (VBox) dialogue.getChildren().get(textIndex);
        return ((Label) text.getChildren().get(0)).getText();
    }
}
