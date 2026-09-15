package luckynoslacky.luckyui.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javafx.scene.AccessibleRole;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskView;

/**
 * Builds the JavaFX nodes used to display one parsed task as a card.
 */
final class TaskCardRenderer {
    private static final DateTimeFormatter TASK_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE MMM dd uuuu, h.mma", Locale.ENGLISH);

    private TaskCardRenderer() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Creates one semantic task card from structured task data.
     *
     * @param task task data to render
     * @return task card node
     */
    static VBox createTaskCard(TaskView task) {
        VBox taskCard = new VBox();
        taskCard.getStyleClass().addAll(
                "personality-task-card",
                "personality-task-card-" + getTypeStyle(task.taskType()));

        Label typeLabel = createTextLabel(
                getTypeLabel(task.taskType()) + " " + getTypeEmoji(task.taskType()),
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
                task.isDone() ? "✅" : "❗", "personality-task-status");
        Label titleLabel = createTextLabel(
                task.description(), "personality-task-title");
        HBox titleRow = new HBox(statusLabel, titleLabel);
        titleRow.getStyleClass().add("personality-task-title-row");

        taskCard.getChildren().addAll(typeRow, titleRow);
        for (String detail : getDetails(task)) {
            taskCard.getChildren().add(
                    createTextLabel(detail, "personality-task-details"));
        }
        return taskCard;
    }

    /**
     * Returns the spoken description used by assistive technologies.
     *
     * @param task task data to describe
     * @return accessible task description without decorative symbols
     */
    static String getAccessibleDescription(TaskView task) {
        String taskNumberDescription = task.taskNumber() == null
                ? ""
                : ", task " + task.taskNumber();
        String statusDescription = task.isDone() ? "completed" : "incomplete";
        String description = getTypeLabel(task.taskType())
                + taskNumberDescription
                + ", " + statusDescription
                + ", " + task.description();
        List<String> details = getDetails(task);
        return details.isEmpty()
                ? description
                : description + ", " + String.join(", ", details);
    }

    /** Returns the user-facing label for a task type. */
    private static String getTypeLabel(Task.TaskType taskType) {
        return switch (taskType) {
            case TODO -> "TODO";
            case DEADLINE -> "DEADLINE";
            case EVENT -> "EVENT";
        };
    }

    /** Returns the decorative emoji for a task type. */
    private static String getTypeEmoji(Task.TaskType taskType) {
        return switch (taskType) {
            case TODO -> "📌";
            case DEADLINE -> "⏳";
            case EVENT -> "📆";
        };
    }

    /** Returns the CSS modifier for a task type. */
    private static String getTypeStyle(Task.TaskType taskType) {
        return switch (taskType) {
            case TODO -> "todo";
            case DEADLINE -> "deadline";
            case EVENT -> "event";
        };
    }

    /** Returns formatted schedule details for a task card. */
    private static List<String> getDetails(TaskView task) {
        return switch (task.taskType()) {
            case TODO -> List.of();
            case DEADLINE -> List.of("by "
                    + formatDateTime(task.taskTimes().getEndTime()));
            case EVENT -> List.of(
                    "from " + formatDateTime(task.taskTimes().getStartTime()),
                    "to " + formatDateTime(task.taskTimes().getEndTime()));
        };
    }

    /** Formats a task time with the existing CLI display convention. */
    private static String formatDateTime(LocalDateTime dateTime) {
        return TASK_TIME_FORMATTER.format(dateTime)
                .replace("AM", "am")
                .replace("PM", "pm");
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
