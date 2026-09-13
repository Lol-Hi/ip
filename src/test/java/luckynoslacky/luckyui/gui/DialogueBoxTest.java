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
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
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

    /** Verifies that user dialogue is right-aligned with a circular avatar. */
    @Test
    void dialogueBox_userMessage_displaysCircularAvatarOnRight(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "todo read book",
                    avatar,
                    DialogueBox.DialogueType.USER);
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(0);
            Label messageLabel = (Label) messageContainer.getChildren().get(0);
            ImageView imageView = (ImageView) dialogue.getChildren().get(1);

            assertEquals(Pos.CENTER_RIGHT, dialogue.getAlignment());
            assertEquals("todo read book", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                DialogueBox.USER_DIALOGUE_STYLE));
            assertTrue(messageLabel.getStyleClass().contains("message-content"));
            assertTrue(imageView.getClip() instanceof Circle);
        });
    }

    /** Verifies that chatbot dialogue is left-aligned with its response bubble. */
    @Test
    void dialogueBox_chatbotMessage_displaysBubbleOnLeft(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "Got it.",
                    avatar,
                    DialogueBox.DialogueType.CHATBOT);
            stage.setScene(new Scene(dialogue));
            stage.show();

            VBox messageContainer = (VBox) dialogue.getChildren().get(1);
            Label messageLabel = (Label) messageContainer.getChildren().get(0);

            assertEquals(Pos.CENTER_LEFT, dialogue.getAlignment());
            assertEquals("Got it.", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                DialogueBox.CHATBOT_DIALOGUE_STYLE));
            assertTrue(messageLabel.getStyleClass().contains("message-content"));
        });
    }

    /** Verifies that warning dialogue uses the distinct warning style. */
    @Test
    void dialogueBox_warningMessage_appliesWarningStyle(FxRobot robot) {
        robot.interact(() -> {
            WritableImage avatar = new WritableImage(45, 45);
            DialogueBox dialogue = new DialogueBox(
                    "That command needs more detail.",
                    avatar,
                    DialogueBox.DialogueType.WARNING);
            stage.setScene(new Scene(dialogue));
            stage.show();

            assertEquals(Pos.CENTER_LEFT, dialogue.getAlignment());
            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.WARNING_DIALOGUE_STYLE));
        });
    }
}
