package luckynoslacky.luckyui.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Tests the reusable JavaFX dialogue message component. */
@ExtendWith(ApplicationExtension.class)
class DialogueBoxTest {
    private Stage stage;

    /** Provides a JavaFX stage for component tests. */
    @Start
    void start(Stage stage) {
        this.stage = stage;
    }

    /** Verifies that user dialogue is right-aligned and blue. */
    @Test
    void dialogueBox_userMessage_displaysBlueLabelOnRight(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "You said:",
                    "todo read book",
                    avatar,
                    Pos.CENTER_RIGHT,
                    DialogueBox.USER_DIALOGUE_STYLE);
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(0);
            Label speakerLabel = (Label) messageContainer.getChildren().get(0);
            VBox messageBody = (VBox) messageContainer.getChildren().get(1);
            Label messageLabel = (Label) messageBody.getChildren().get(0);

            assertEquals(Pos.CENTER_RIGHT, dialogue.getAlignment());
            assertEquals("You said:", speakerLabel.getText());
            assertEquals("todo read book", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.USER_DIALOGUE_STYLE));
            assertTrue(speakerLabel.getStyleClass().contains("speaker-label"));
            assertTrue(messageLabel.getStyleClass().contains("prose-content"));
        });
    }

    /** Verifies that neutral chatbot dialogue is left-aligned and pineapple-yellow. */
    @Test
    void dialogueBox_neutralChatbotMessage_displaysPineappleLabelOnLeft(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "LuckyNoSlacky said:",
                    "Got it.",
                    avatar,
                    Pos.CENTER_LEFT,
                    DialogueBox.CHATBOT_DIALOGUE_STYLE,
                    "neutral-dialogue");
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(1);
            Label speakerLabel = (Label) messageContainer.getChildren().get(0);
            VBox messageBody = (VBox) messageContainer.getChildren().get(1);
            Label messageLabel = (Label) messageBody.getChildren().get(0);

            assertEquals(Pos.CENTER_LEFT, dialogue.getAlignment());
            assertEquals("LuckyNoSlacky said:", speakerLabel.getText());
            assertEquals("Got it.", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.CHATBOT_DIALOGUE_STYLE));
            assertTrue(dialogue.getStyleClass().contains("neutral-dialogue"));
            assertTrue(speakerLabel.getStyleClass().contains("speaker-label"));
            assertTrue(messageLabel.getStyleClass().contains("prose-content"));
        });
    }

    /** Verifies that task lines use the monospace task style. */
    @Test
    void dialogueBox_taskLine_usesMonospaceTaskStyle(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "LuckyNoSlacky said:",
                    "Got one more thing to remember ah:\n  [T][ ] read book",
                    avatar,
                    Pos.CENTER_LEFT,
                    DialogueBox.CHATBOT_DIALOGUE_STYLE);
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(1);
            VBox messageBody = (VBox) messageContainer.getChildren().get(1);
            VBox taskList = (VBox) messageBody.getChildren().get(1);
            Label taskLabel = (Label) taskList.getChildren().get(0);

            assertEquals("  [T][ ] read book", taskLabel.getText());
            assertTrue(taskLabel.getStyleClass().contains("task-content"));
            assertTrue(taskList.getStyleClass().contains("task-list"));
        });
    }

    /** Verifies that numbered task lines are grouped into one dark task panel. */
    @Test
    void dialogueBox_numberedTaskList_groupsAdjacentLinesInOnePanel(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "LuckyNoSlacky said:",
                    "Nah, all these things you need to do:\n"
                            + "1.[T][ ] read book\n"
                            + "2.[T][ ] read / book\n"
                            + "12.[D][X] submit report",
                    avatar,
                    Pos.CENTER_LEFT,
                    DialogueBox.CHATBOT_DIALOGUE_STYLE);
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(1);
            VBox messageBody = (VBox) messageContainer.getChildren().get(1);
            Label introduction = (Label) messageBody.getChildren().get(0);
            VBox taskList = (VBox) messageBody.getChildren().get(1);

            assertEquals("Nah, all these things you need to do:", introduction.getText());
            assertTrue(introduction.getStyleClass().contains("prose-content"));
            assertTrue(taskList.getStyleClass().contains("task-list"));
            assertEquals(3, taskList.getChildren().size());
            Label firstTask = (Label) taskList.getChildren().get(0);
            Label lastTask = (Label) taskList.getChildren().get(2);
            assertEquals("1.[T][ ] read book", firstTask.getText());
            assertEquals("12.[D][X] submit report", lastTask.getText());
            assertEquals(2, messageBody.getChildren().size());
            assertEquals(2, messageContainer.getChildren().size());
        });
    }

    /** Verifies that a chatbot response can apply a semantic tone style. */
    @Test
    void dialogueBox_successTone_addsSuccessDialogueStyle(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "🍀 LuckyNoSlacky said:",
                    "Swee lah you're done with this task:",
                    avatar,
                    Pos.CENTER_LEFT,
                    DialogueBox.CHATBOT_DIALOGUE_STYLE,
                    "success-dialogue");
            stage.setScene(new Scene(dialogue));
            stage.show();

            assertTrue(dialogue.getStyleClass().contains("success-dialogue"));
        });
    }
}
