package luckynoslacky.luckyui.gui;

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

    /** Pattern for numbered or unnumbered task lines in chatbot responses. */
    private static final String TASK_LINE_PATTERN =
            "(?:\\d+\\.\\s*)?\\[[TDE]\\]\\s*\\[[ X]\\].*";

    /**
     * Creates a dialogue row with a speaker label and avatar.
     *
     * @param speakerLabel speaker label shown above the message
     * @param message message content
     * @param avatar speaker profile image
     * @param alignment horizontal alignment of the row
     * @param dialogueStyle CSS style identifying the speaker role
     */
    public DialogueBox(
            String speakerLabel,
            String message,
            Image avatar,
            Pos alignment,
            String dialogueStyle) {
        this(speakerLabel, message, avatar, alignment, dialogueStyle, null);
    }

    /**
     * Creates a dialogue row with speaker and response-tone styles.
     *
     * @param speakerLabel speaker label shown above the message
     * @param message message content
     * @param avatar speaker profile image
     * @param alignment horizontal alignment of the row
     * @param dialogueStyle CSS style identifying the speaker role
     * @param toneStyle optional CSS style identifying the response tone
     */
    public DialogueBox(
            String speakerLabel,
            String message,
            Image avatar,
            Pos alignment,
            String dialogueStyle,
            String toneStyle) {
        Label speakerNameLabel = new Label(speakerLabel);
        speakerNameLabel.getStyleClass().add("speaker-label");

        VBox messageContainer = new VBox(4.0, speakerNameLabel, createMessageContent(message));
        messageContainer.getStyleClass().add("message-content");
        messageContainer.setMaxWidth(320.0);

        ImageView imageView = new ImageView(avatar);
        imageView.setFitWidth(45.0);
        imageView.setFitHeight(45.0);
        imageView.setPreserveRatio(true);
        imageView.setClip(new Circle(22.5, 22.5, 22.5));
        imageView.getStyleClass().add("avatar");

        setAlignment(alignment);
        setSpacing(8.0);
        getStyleClass().addAll("dialogue-box", dialogueStyle);
        if (toneStyle != null) {
            getStyleClass().add(toneStyle);
        }

        if (alignment == Pos.CENTER_RIGHT) {
            getChildren().addAll(messageContainer, imageView);
        } else {
            getChildren().addAll(imageView, messageContainer);
        }
    }

    /**
     * Converts the response into prose labels and grouped task-list panels.
     *
     * @param message complete response text
     * @return content container for the response bubble
     */
    private VBox createMessageContent(String message) {
        VBox content = new VBox(4.0);
        content.getStyleClass().add("message-body");

        String[] messageLines = message.split("\\R", -1);
        int lineIndex = 0;
        while (lineIndex < messageLines.length) {
            if (isTaskLine(messageLines[lineIndex])) {
                VBox taskList = new VBox(2.0);
                taskList.getStyleClass().add("task-list");
                while (lineIndex < messageLines.length
                        && isTaskLine(messageLines[lineIndex])) {
                    taskList.getChildren().add(createContentLabel(
                            messageLines[lineIndex], "task-content"));
                    lineIndex++;
                }
                content.getChildren().add(taskList);
            } else {
                content.getChildren().add(createContentLabel(
                        messageLines[lineIndex], "prose-content"));
                lineIndex++;
            }
        }
        return content;
    }

    /**
     * Creates a wrapped label for one content segment.
     *
     * @param content text shown in the label
     * @param styleClass CSS class describing the content type
     * @return styled content label
     */
    private Label createContentLabel(String content, String styleClass) {
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add(styleClass);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(280.0);
        return contentLabel;
    }

    /**
     * Identifies task-display lines that should use monospace styling.
     *
     * @param messageLine one line from a chatbot response
     * @return true when the line contains an optional task number and marker
     */
    private boolean isTaskLine(String messageLine) {
        return messageLine.trim().matches(TASK_LINE_PATTERN);
    }
}
