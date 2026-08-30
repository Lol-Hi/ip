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
    /** Colour used for chatbot speaker labels. */
    public static final String CHATBOT_LABEL_COLOR = "#388E3C";

    /** Colour used for user speaker labels. */
    public static final String USER_LABEL_COLOR = "#1976D2";

    /**
     * Creates a dialogue row with a speaker label and avatar.
     *
     * @param speakerLabel speaker label shown above the message
     * @param message message content
     * @param avatar speaker profile image
     * @param alignment horizontal alignment of the row
     * @param labelColor speaker label colour
     */
    public DialogueBox(
            String speakerLabel,
            String message,
            Image avatar,
            Pos alignment,
            String labelColor) {
        Label speaker = new Label(speakerLabel);
        speaker.getStyleClass().add("speaker-label");
        speaker.setStyle(
                "-fx-font-weight: bold; -fx-text-fill: " + labelColor + ";");

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

        setAlignment(alignment);
        setSpacing(8.0);
        getStyleClass().add("dialogue-box");

        if (alignment == Pos.CENTER_RIGHT) {
            getChildren().addAll(text, imageView);
        } else {
            getChildren().addAll(imageView, text);
        }
    }
}
