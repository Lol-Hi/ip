package luckynoslacky.luckyui.gui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import luckynoslacky.ResponseTone;

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

    /** CSS style applied to successful chatbot dialogue rows. */
    public static final String SUCCESS_DIALOGUE_STYLE = "success-dialogue";

    /** CSS style applied to informational chatbot dialogue rows. */
    public static final String INFO_DIALOGUE_STYLE = "info-dialogue";

    /** CSS style applied to system-error dialogue rows. */
    public static final String SYSTEM_ERROR_DIALOGUE_STYLE =
            "system-error-dialogue";

    private static final double AVATAR_SIZE = 45.0;
    private static final double CHATBOT_BUBBLE_WIDTH_FRACTION = 0.86;
    private static final double USER_BUBBLE_WIDTH_FRACTION = 0.70;
    private static final double DIALOGUE_SPACING = 8.0;
    private static final String USER_ACCESSIBLE_PREFIX = "You: ";
    private static final String CHATBOT_ACCESSIBLE_PREFIX = "LuckyNoSlacky: ";
    private static final String WARNING_ACCESSIBLE_PREFIX =
            "Bodoh sia like that also can kena warning ";

    /** Identifies the speaker and visual treatment of a dialogue row. */
    public enum DialogueType {
        /** A LuckyNoSlacky response. */
        CHATBOT,
        /** A command entered by the user. */
        USER
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
        this(message, avatar, dialogueType, ResponseTone.NEUTRAL);
    }

    /**
     * Creates a responsive dialogue row with a circular avatar and response
     * tone.
     *
     * @param message message content
     * @param avatar speaker profile image
     * @param dialogueType speaker role of the message
     * @param responseTone semantic tone of a chatbot response
     */
    public DialogueBox(
            String message,
            Image avatar,
            DialogueType dialogueType,
            ResponseTone responseTone) {
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("message-content");
        messageLabel.setWrapText(true);

        if (dialogueType == DialogueType.CHATBOT) {
            addToneMarker(messageLabel, responseTone);
        }

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
        imageView.setAccessibleText("");
        imageView.setFocusTraversable(false);
        imageView.getStyleClass().add("avatar");

        setMaxWidth(Double.MAX_VALUE);
        setAccessibleRole(AccessibleRole.TEXT);
        setAccessibleText(getAccessibleText(message, dialogueType, responseTone));
        setFocusTraversable(false);
        setAlignment(dialogueType == DialogueType.USER
                ? Pos.CENTER_RIGHT
                : Pos.CENTER_LEFT);
        setSpacing(DIALOGUE_SPACING);
        getStyleClass().addAll(
                "dialogue-box",
                getDialogueStyle(dialogueType),
                getToneStyle(dialogueType, responseTone));

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
        return dialogueType == DialogueType.USER
                ? USER_DIALOGUE_STYLE
                : CHATBOT_DIALOGUE_STYLE;
    }

    /**
     * Returns the CSS style for a chatbot response tone.
     *
     * @param dialogueType dialogue role
     * @param responseTone response tone
     * @return CSS style class for the tone, or an empty string for user rows
     */
    private static String getToneStyle(
            DialogueType dialogueType,
            ResponseTone responseTone) {
        if (dialogueType == DialogueType.USER) {
            return "";
        }
        return switch (responseTone) {
            case NEUTRAL -> "";
            case SUCCESS -> SUCCESS_DIALOGUE_STYLE;
            case INFO -> INFO_DIALOGUE_STYLE;
            case WARNING -> WARNING_DIALOGUE_STYLE;
            case SYSTEM_ERROR -> SYSTEM_ERROR_DIALOGUE_STYLE;
        };
    }

    /** Adds a non-colour marker for tones that require extra emphasis. */
    private static void addToneMarker(
            Label messageLabel,
            ResponseTone responseTone) {
        String markerText = switch (responseTone) {
            case SUCCESS -> "🍀";
            case WARNING -> "⚠";
            case SYSTEM_ERROR -> "⛔";
            case NEUTRAL, INFO -> "";
        };
        if (!markerText.isEmpty()) {
            Label marker = new Label(markerText);
            marker.getStyleClass().add(getMarkerStyle(responseTone));
            marker.setAccessibleText("");
            marker.setFocusTraversable(false);
            messageLabel.setGraphic(marker);
            messageLabel.setGraphicTextGap(6.0);
        }
    }

    /**
     * Returns the marker style for an emphasized response tone.
     *
     * @param responseTone response tone
     * @return marker CSS style class
     */
    private static String getMarkerStyle(ResponseTone responseTone) {
        return switch (responseTone) {
            case SUCCESS -> "success-marker";
            case SYSTEM_ERROR -> "system-error-marker";
            case WARNING -> "warning-marker";
            case NEUTRAL, INFO -> "";
        };
    }

    /**
     * Returns the screen-reader text for a dialogue role and response tone.
     *
     * @param message message content
     * @param dialogueType dialogue role
     * @param responseTone response tone
     * @return role-aware accessible message
     */
    private static String getAccessibleText(
            String message,
            DialogueType dialogueType,
            ResponseTone responseTone) {
        if (dialogueType == DialogueType.USER) {
            return USER_ACCESSIBLE_PREFIX + message;
        }
        return responseTone == ResponseTone.WARNING
                ? WARNING_ACCESSIBLE_PREFIX + message
                : CHATBOT_ACCESSIBLE_PREFIX + message;
    }
}
