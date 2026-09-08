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
            Label messageLabel = (Label) messageContainer.getChildren().get(1);

            assertEquals(Pos.CENTER_RIGHT, dialogue.getAlignment());
            assertEquals("You said:", speakerLabel.getText());
            assertEquals("todo read book", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.USER_DIALOGUE_STYLE));
            assertTrue(speakerLabel.getStyleClass().contains("speaker-label"));
            assertTrue(messageLabel.getStyleClass().contains("message-content"));
        });
    }

    /** Verifies that chatbot dialogue is left-aligned and green. */
    @Test
    void dialogueBox_chatbotMessage_displaysGreenLabelOnLeft(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "LuckyNoSlacky said:",
                    "Got it.",
                    avatar,
                    Pos.CENTER_LEFT,
                    DialogueBox.CHATBOT_DIALOGUE_STYLE);
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(1);
            Label speakerLabel = (Label) messageContainer.getChildren().get(0);
            Label messageLabel = (Label) messageContainer.getChildren().get(1);

            assertEquals(Pos.CENTER_LEFT, dialogue.getAlignment());
            assertEquals("LuckyNoSlacky said:", speakerLabel.getText());
            assertEquals("Got it.", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.CHATBOT_DIALOGUE_STYLE));
            assertTrue(speakerLabel.getStyleClass().contains("speaker-label"));
            assertTrue(messageLabel.getStyleClass().contains("message-content"));
        });
    }
}
