package luckynoslacky.luckyui.gui;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the task-list text produced by the CLI into display data for the GUI.
 */
final class TaskLineParser {
    private static final Pattern TASK_LINE_PATTERN = Pattern.compile(
            "^\\s*(?:(\\d+)\\.)?\\[([TDE])\\]\\[([ X])\\]\\s+(.+)$");
    private static final Pattern DEADLINE_DETAILS_PATTERN = Pattern.compile(
            "^(.*?)\\s*\\(by:\\s*(.+)\\)$");
    private static final Pattern EVENT_DETAILS_PATTERN = Pattern.compile(
            "^(.*?)\\s*\\(from:\\s*(.+?)\\s+to:\\s*(.+)\\)$");

    private TaskLineParser() {
        // Prevent instantiation of this utility class.
    }

    /**
     * Parses a task-list line when it follows the CLI display format.
     *
     * @param line task-list line to parse
     * @return parsed display data, or an empty optional for ordinary text
     */
    static Optional<TaskDisplayData> parse(String line) {
        Matcher matcher = TASK_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String taskType = matcher.group(2);
        Integer taskNumber = matcher.group(1) == null
                ? null
                : Integer.valueOf(matcher.group(1));
        String status = matcher.group(3);
        TaskDetails taskDetails = parseTaskDetails(
                taskType,
                matcher.group(4).trim());
        String typeLabel = getTypeLabel(taskType);
        String typeEmoji = getTypeEmoji(taskType);
        String typeStyle = getTypeStyle(taskType);
        String statusEmoji = status.equals("X") ? "✅" : "❗";
        String statusDescription = status.equals("X")
                ? "completed"
                : "incomplete";
        String taskNumberDescription = taskNumber == null
                ? ""
                : ", task " + taskNumber;
        String accessibleDescription = typeLabel + taskNumberDescription + ", "
                + statusDescription + ", " + taskDetails.title();
        if (!taskDetails.details().isEmpty()) {
            accessibleDescription += ", "
                    + String.join(", ", taskDetails.details());
        }
        return Optional.of(new TaskDisplayData(
                taskNumber,
                typeLabel,
                typeEmoji,
                typeStyle,
                statusEmoji,
                taskDetails.title(),
                taskDetails.details(),
                accessibleDescription));
    }

    /** Returns the user-facing label for a task type. */
    private static String getTypeLabel(String taskType) {
        return switch (taskType) {
            case "T" -> "TODO";
            case "D" -> "DEADLINE";
            case "E" -> "EVENT";
            default -> throw new IllegalStateException(
                    "Unknown task type: " + taskType);
        };
    }

    /** Returns the decorative emoji for a task type. */
    private static String getTypeEmoji(String taskType) {
        return switch (taskType) {
            case "T" -> "📌";
            case "D" -> "⏳";
            case "E" -> "📆";
            default -> "";
        };
    }

    /** Returns the CSS modifier for a task type. */
    private static String getTypeStyle(String taskType) {
        return switch (taskType) {
            case "T" -> "todo";
            case "D" -> "deadline";
            case "E" -> "event";
            default -> "";
        };
    }

    /** Extracts schedule details while leaving the task title unchanged. */
    private static TaskDetails parseTaskDetails(
            String taskType,
            String rawTaskText) {
        if (taskType.equals("D")) {
            Matcher matcher = DEADLINE_DETAILS_PATTERN.matcher(rawTaskText);
            if (matcher.matches()) {
                return new TaskDetails(
                        matcher.group(1).trim(),
                        List.of("by " + matcher.group(2).trim()));
            }
        } else if (taskType.equals("E")) {
            Matcher matcher = EVENT_DETAILS_PATTERN.matcher(rawTaskText);
            if (matcher.matches()) {
                return new TaskDetails(
                        matcher.group(1).trim(),
                        List.of(
                                "from " + matcher.group(2).trim(),
                                "to " + matcher.group(3).trim()));
            }
        }
        return new TaskDetails(rawTaskText, List.of());
    }

    /** Structured data used to render one task card. */
    record TaskDisplayData(
            Integer taskNumber,
            String typeLabel,
            String typeEmoji,
            String typeStyle,
            String statusEmoji,
            String title,
            List<String> details,
            String accessibleDescription) {
    }

    /** Structured schedule details extracted from a task line. */
    private record TaskDetails(String title, List<String> details) {
    }
}
