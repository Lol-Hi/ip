package luckynoslacky.luckyui.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import luckynoslacky.ResponseKind;
import luckynoslacky.ResponseTone;

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
            StackPane avatarFrame = (StackPane) dialogue.getChildren().get(1);
            ImageView imageView = (ImageView) avatarFrame.getChildren().get(0);

            assertEquals(Pos.CENTER_RIGHT, dialogue.getAlignment());
            assertEquals("todo read book", messageLabel.getText());
            assertTrue(dialogue.getStyleClass().contains(
                DialogueBox.USER_DIALOGUE_STYLE));
            assertTrue(messageContainer.getStyleClass().contains("message-content"));
            assertTrue(imageView.getClip() instanceof Circle);
            assertTrue(avatarFrame.getStyleClass().contains(
                    "personality-user-avatar-frame"));
            assertEquals(48.0, avatarFrame.getPrefWidth());
            assertEquals("You: todo read book", dialogue.getAccessibleText());
            assertFalse(imageView.isFocusTraversable());
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
            assertTrue(messageContainer.getStyleClass().contains("message-content"));
            assertTrue(((StackPane) dialogue.getChildren().get(0)).getStyleClass()
                    .contains("personality-chatbot-avatar-frame"));
            assertEquals("LuckyNoSlacky: Got it.", dialogue.getAccessibleText());
        });
    }

    /** Verifies that successful chatbot dialogue uses the clover style. */
    @Test
    void dialogueBox_successMessage_displaysCloverMarker(FxRobot robot) {
        robot.interact(() -> {
            DialogueBox dialogue = createChatbotDialogue(
                    "Task added.", ResponseTone.SUCCESS);

            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.SUCCESS_DIALOGUE_STYLE));
            VBox messageContainer = getMessageContainer(dialogue, 1);
            assertEquals("🍀", ((Label) messageContainer.getChildren().get(0)).getText());
            assertTrue(messageContainer.getStyleClass().contains(
                    "personality-response-success"));
            assertEquals("LuckyNoSlacky: Task added.", dialogue.getAccessibleText());
        });
    }

    /** Verifies that informational chatbot dialogue uses the info style. */
    @Test
    void dialogueBox_infoMessage_appliesInfoStyle(FxRobot robot) {
        robot.interact(() -> {
            DialogueBox dialogue = createChatbotDialogue(
                    "Here are your tasks.", ResponseTone.INFO);

            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.INFO_DIALOGUE_STYLE));
            assertEquals("LuckyNoSlacky: Here are your tasks.",
                    dialogue.getAccessibleText());
            assertNull(getMessageLabel(dialogue).getGraphic());
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
                    DialogueBox.DialogueType.CHATBOT,
                    ResponseTone.WARNING);
            stage.setScene(new Scene(dialogue));
            stage.show();

            assertEquals(Pos.CENTER_LEFT, dialogue.getAlignment());
            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.WARNING_DIALOGUE_STYLE));
            assertEquals(
                    "Bodoh sia like that also can kena warning "
                            + "That command needs more detail.",
                    dialogue.getAccessibleText());
            VBox messageContainer = getMessageContainer(dialogue, 1);
            assertEquals("⚠", ((Label) messageContainer.getChildren().get(0)).getText());
        });
    }

    /** Verifies that system errors use a distinct marker and style. */
    @Test
    void dialogueBox_systemErrorMessage_displaysErrorMarker(FxRobot robot) {
        robot.interact(() -> {
            DialogueBox dialogue = createChatbotDialogue(
                    "Unable to save tasks.", ResponseTone.SYSTEM_ERROR);

            assertTrue(dialogue.getStyleClass().contains(
                    DialogueBox.SYSTEM_ERROR_DIALOGUE_STYLE));
            VBox messageContainer = getMessageContainer(dialogue, 1);
            assertEquals("⛔", ((Label) messageContainer.getChildren().get(0)).getText());
            assertEquals("LuckyNoSlacky: Unable to save tasks.",
                    dialogue.getAccessibleText());
        });
    }

    /** Verifies that task responses group formatted lines inside one bubble. */
    @Test
    void dialogueBox_taskContent_groupsFormattedLinesInsideMessageBubble(FxRobot robot) {
        robot.interact(() -> {
            String message = "[T][ ] buy groceries\n[E][X] team meeting";
            DialogueBox dialogue = new DialogueBox(
                    message,
                    new WritableImage(45, 45),
                    DialogueBox.DialogueType.CHATBOT,
                    ResponseTone.INFO,
                    ResponseKind.TASK_CONTENT);

            VBox messageContainer = getMessageContainer(dialogue, 1);
            VBox taskBlock = (VBox) messageContainer.getChildren().get(0);

            assertTrue(taskBlock.getStyleClass().contains(
                    "personality-task-block"));
            assertEquals(2, taskBlock.getChildren().size());
            Label firstTaskLine = (Label) taskBlock.getChildren().get(0);
            Label secondTaskLine = (Label) taskBlock.getChildren().get(1);
            assertEquals("[T][ ] buy groceries",
                    firstTaskLine.getText());
            assertEquals("[E][X] team meeting",
                    secondTaskLine.getText());
            assertEquals("LuckyNoSlacky: " + message,
                    dialogue.getAccessibleText());
        });
    }

    /** Creates a chatbot dialogue for tone-specific component assertions. */
    private DialogueBox createChatbotDialogue(
            String message,
            ResponseTone responseTone) {
        return new DialogueBox(
                message,
                new WritableImage(45, 45),
                DialogueBox.DialogueType.CHATBOT,
                responseTone);
    }

    /** Returns the message label from a chatbot dialogue. */
    private Label getMessageLabel(DialogueBox dialogue) {
        VBox messageContainer = getMessageContainer(dialogue, 1);
        return messageContainer.getChildren().stream()
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .filter(label -> !label.getText().isEmpty())
                .findFirst()
                .orElseThrow();
    }

    /** Returns the message bubble at the given row position. */
    private VBox getMessageContainer(DialogueBox dialogue, int dialogueIndex) {
        return (VBox) dialogue.getChildren().get(dialogueIndex);
    }
}
