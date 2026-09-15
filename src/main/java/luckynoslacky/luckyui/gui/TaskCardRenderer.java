package luckynoslacky.luckyui.gui;

import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Builds the JavaFX nodes used to display one parsed task as a card.
 */
final class TaskCardRenderer {
    private TaskCardRenderer() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Creates one semantic task card from parsed display data.
     *
     * @param task parsed task display data
     * @return task card node
     */
    static VBox createTaskCard(TaskLineParser.TaskDisplayData task) {
        VBox taskCard = new VBox();
        taskCard.getStyleClass().addAll(
                "personality-task-card",
                "personality-task-card-" + task.typeStyle());

        Label typeLabel = createTextLabel(
                task.typeLabel() + " " + task.typeEmoji(),
                "personality-task-type");
        HBox typeRow = new HBox(typeLabel);
        typeRow.getStyleClass().add("personality-task-type-row");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        typeRow.getChildren().add(spacer);
        if (task.taskNumber() != null) {
            typeRow.getChildren().add(createTextLabel(
                    "#" + task.taskNumber(), "personality-task-number"));
        }
        Label statusLabel = createTextLabel(
                task.statusEmoji(), "personality-task-status");
        Label titleLabel = createTextLabel(
                task.title(), "personality-task-title");
        HBox titleRow = new HBox(statusLabel, titleLabel);
        titleRow.getStyleClass().add("personality-task-title-row");

        taskCard.getChildren().addAll(typeRow, titleRow);
        for (String detail : task.details()) {
            taskCard.getChildren().add(
                    createTextLabel(detail, "personality-task-details"));
        }
        return taskCard;
    }

    /** Creates a wrapped, non-interactive task-card label. */
    private static Label createTextLabel(String text, String... styles) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setAccessibleRole(AccessibleRole.NODE);
        label.setAccessibleText("");
        label.setFocusTraversable(false);
        label.getStyleClass().addAll(styles);
        return label;
    }
}
