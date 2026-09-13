package luckynoslacky.luckyui.gui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Displays one speaker's message in the conversation.
 */
public class DialogueBox extends HBox {
    /** CSS style applied to chatbot dialogue rows. */
    public static final String CHATBOT_DIALOGUE_STYLE = "chatbot-dialogue";

    /** CSS style applied to user dialogue rows. */
    public static final String USER_DIALOGUE_STYLE = "user-dialogue";

    /** CSS style applied to warning dialogue rows. */
    public static final String WARNING_DIALOGUE_STYLE = "warning-dialogue";

    private static final double AVATAR_SIZE = 45.0;
    private static final double CHATBOT_BUBBLE_WIDTH_FRACTION = 0.86;
    private static final double USER_BUBBLE_WIDTH_FRACTION = 0.70;
    private static final double DIALOGUE_SPACING = 8.0;

    /** Identifies the speaker and visual treatment of a dialogue row. */
    public enum DialogueType {
        /** A normal LuckyNoSlacky response. */
        CHATBOT,
        /** A command entered by the user. */
        USER,
        /** A LuckyNoSlacky warning caused by invalid input or storage. */
        WARNING
    }

    /**
     * Creates a responsive dialogue row with a circular avatar.
     *
     * @param message message content
     * @param avatar speaker profile image
     * @param dialogueType speaker and visual role of the message
     */
    public DialogueBox(
            String message,
            Image avatar,
            DialogueType dialogueType) {
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("message-content");
        messageLabel.setWrapText(true);

        VBox messageContainer = new VBox(messageLabel);
        messageContainer.setMinWidth(0.0);

        ImageView imageView = new ImageView(avatar);
        imageView.setFitWidth(AVATAR_SIZE);
        imageView.setFitHeight(AVATAR_SIZE);
        imageView.setPreserveRatio(true);
        imageView.setClip(new Circle(
                AVATAR_SIZE / 2.0,
                AVATAR_SIZE / 2.0,
                AVATAR_SIZE / 2.0));
        imageView.getStyleClass().add("avatar");

        setMaxWidth(Double.MAX_VALUE);
        setAlignment(dialogueType == DialogueType.USER
                ? Pos.CENTER_RIGHT
                : Pos.CENTER_LEFT);
        setSpacing(DIALOGUE_SPACING);
        getStyleClass().addAll("dialogue-box", getDialogueStyle(dialogueType));

        double bubbleWidthFraction = dialogueType == DialogueType.USER
                ? USER_BUBBLE_WIDTH_FRACTION
                : CHATBOT_BUBBLE_WIDTH_FRACTION;
        messageContainer.maxWidthProperty().bind(
                Bindings.createDoubleBinding(() -> {
                    double availableWidth = getWidth()
                            - AVATAR_SIZE - DIALOGUE_SPACING;
                    return Math.max(
                            0.0,
                            availableWidth * bubbleWidthFraction);
                },
                        widthProperty()));
        messageLabel.maxWidthProperty().bind(messageContainer.maxWidthProperty());

        if (dialogueType == DialogueType.USER) {
            getChildren().addAll(messageContainer, imageView);
        } else {
            getChildren().addAll(imageView, messageContainer);
        }
    }

    /**
     * Returns the CSS style for a dialogue role.
     *
     * @param dialogueType dialogue role
     * @return CSS style class for the role
     */
    private static String getDialogueStyle(DialogueType dialogueType) {
        return switch (dialogueType) {
            case CHATBOT -> CHATBOT_DIALOGUE_STYLE;
            case USER -> USER_DIALOGUE_STYLE;
            case WARNING -> WARNING_DIALOGUE_STYLE;
        };
    }
}
