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
        Label speaker = new Label(speakerLabel);
        speaker.getStyleClass().add("speaker-label");

        Label content = new Label(message);
        content.getStyleClass().add("message-content");
        content.setWrapText(true);
        content.setMaxWidth(280.0);

        VBox text = new VBox(2.0, speaker, content);

        ImageView imageView = new ImageView(avatar);
        imageView.setFitWidth(45.0);
        imageView.setFitHeight(45.0);
        imageView.setPreserveRatio(true);
        imageView.setClip(new Circle(22.5, 22.5, 22.5));
        imageView.getStyleClass().add("avatar");

        setAlignment(alignment);
        setSpacing(8.0);
        getStyleClass().addAll("dialogue-box", dialogueStyle);

        if (alignment == Pos.CENTER_RIGHT) {
            getChildren().addAll(text, imageView);
        } else {
            getChildren().addAll(imageView, text);
        }
    }
}
