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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
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

    /** Verifies that task responses use typed cards and spoken status labels. */
    @Test
    void dialogueBox_taskContent_displaysTypedCardsAndAccessibleStatus(
            FxRobot robot) {
        robot.interact(() -> {
            String message = "1.[T][ ] buy groceries\n"
                    + "2.[D][ ] submit report (by: Wed Aug 26 2026, 11:59pm)\n"
                    + "3.[E][X] team meeting (from: Wed Aug 26 2026, 2:00pm "
                    + "to: Wed Aug 26 2026, 3:00pm)";
            DialogueBox dialogue = new DialogueBox(
                    message,
                    new WritableImage(45, 45),
                    DialogueBox.DialogueType.CHATBOT,
                    ResponseTone.INFO,
                    ResponseKind.TASK_CONTENT);

            VBox messageContainer = getMessageContainer(dialogue, 1);
            VBox taskList = (VBox) messageContainer.getChildren().get(0);

            assertTrue(taskList.getStyleClass().contains(
                    "personality-task-list"));
            assertEquals(3, taskList.getChildren().size());

            VBox todoCard = (VBox) taskList.getChildren().get(0);
            assertTrue(todoCard.getStyleClass().contains(
                    "personality-task-card-todo"));
            HBox todoTypeRow = (HBox) todoCard.getChildren().get(0);
            Label todoType = (Label) todoTypeRow.getChildren().get(0);
            Label todoNumber = (Label) todoTypeRow.getChildren().get(2);
            assertEquals("TODO 📌", todoType.getText());
            assertEquals("#1", todoNumber.getText());
            HBox todoTitleRow = (HBox) todoCard.getChildren().get(1);
            assertEquals("❗", ((Label) todoTitleRow.getChildren().get(0)).getText());
            Label todoTitle = (Label) todoTitleRow.getChildren().get(1);
            assertEquals("buy groceries", todoTitle.getText());
            assertEquals(2, todoCard.getChildren().size());

            VBox deadlineCard = (VBox) taskList.getChildren().get(1);
            assertTrue(deadlineCard.getStyleClass().contains(
                    "personality-task-card-deadline"));
            HBox deadlineTypeRow = (HBox) deadlineCard.getChildren().get(0);
            Label deadlineNumber = (Label) deadlineTypeRow.getChildren().get(2);
            assertEquals("#2", deadlineNumber.getText());
            Label deadlineDetails = (Label) deadlineCard.getChildren().get(2);
            assertEquals("by Wed Aug 26 2026, 11:59pm",
                    deadlineDetails.getText());

            VBox eventCard = (VBox) taskList.getChildren().get(2);
            assertTrue(eventCard.getStyleClass().contains(
                    "personality-task-card-event"));
            HBox eventTypeRow = (HBox) eventCard.getChildren().get(0);
            Label eventNumber = (Label) eventTypeRow.getChildren().get(2);
            assertEquals("#3", eventNumber.getText());
            HBox eventTitleRow = (HBox) eventCard.getChildren().get(1);
            Label eventStatus = (Label) eventTitleRow.getChildren().get(0);
            assertEquals("✅", eventStatus.getText());
            Label eventStart = (Label) eventCard.getChildren().get(2);
            Label eventEnd = (Label) eventCard.getChildren().get(3);
            assertEquals("from Wed Aug 26 2026, 2:00pm", eventStart.getText());
            assertEquals("to Wed Aug 26 2026, 3:00pm", eventEnd.getText());

            String accessibleText = dialogue.getAccessibleText();
            assertEquals(
                    "LuckyNoSlacky: TODO, task 1, incomplete, buy groceries. "
                            + "DEADLINE, task 2, incomplete, submit report, by Wed Aug 26 2026, 11:59pm. "
                            + "EVENT, task 3, completed, team meeting, from Wed Aug 26 2026, 2:00pm, "
                            + "to Wed Aug 26 2026, 3:00pm",
                    accessibleText);
            assertFalse(accessibleText.contains("📌"));
            assertFalse(accessibleText.contains("⏳"));
            assertFalse(accessibleText.contains("📆"));
            assertFalse(accessibleText.contains("❗"));
            assertFalse(accessibleText.contains("✅"));
            assertFalse(accessibleText.contains("[T]"));
        });
    }

    /** Verifies that confirmation task lines remain unnumbered. */
    @Test
    void dialogueBox_taskConfirmation_omitsTaskNumber(FxRobot robot) {
        robot.interact(() -> {
            DialogueBox dialogue = new DialogueBox(
                    "[T][ ] buy groceries",
                    new WritableImage(45, 45),
                    DialogueBox.DialogueType.CHATBOT,
                    ResponseTone.SUCCESS,
                    ResponseKind.TASK_CONTENT);

            VBox messageContainer = getMessageContainer(dialogue, 1);
            VBox taskList = (VBox) messageContainer.getChildren().get(1);
            VBox taskCard = (VBox) taskList.getChildren().get(0);
            HBox typeRow = (HBox) taskCard.getChildren().get(0);

            assertEquals(2, typeRow.getChildren().size());
            assertFalse(dialogue.getAccessibleText().contains("task 1"));
            assertFalse(dialogue.getAccessibleText().contains("#1"));
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
        return findMessageLabel(messageContainer);
    }

    /** Finds the first visible non-decorative label in a message bubble. */
    private Label findMessageLabel(Node node) {
        if (node instanceof Label label
                && !label.getText().isEmpty()
                && !isDecorativeLabel(label.getText())) {
            return label;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                try {
                    return findMessageLabel(child);
                } catch (java.util.NoSuchElementException exception) {
                    // Continue searching the remaining message children.
                }
            }
        }
        throw new java.util.NoSuchElementException("No visible message label");
    }

    /** Returns whether a label contains only a decorative marker. */
    private boolean isDecorativeLabel(String text) {
        return text.equals("🍀")
                || text.equals("⚠")
                || text.equals("⛔")
                || text.equals("📌")
                || text.equals("⏳")
                || text.equals("📆")
                || text.equals("❗")
                || text.equals("✅");
    }

    /** Returns the message bubble at the given row position. */
    private VBox getMessageContainer(DialogueBox dialogue, int dialogueIndex) {
        return (VBox) dialogue.getChildren().get(dialogueIndex);
    }
}
