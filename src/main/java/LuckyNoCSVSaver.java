import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * Saves the current task list as a CSV file.
 */
public class LuckyNoCSVSaver {
    private static final List<String> CSV_HEADER = List.of(
            "Task type",
            "isCompleted",
            "Description",
            "startTime",
            "finishTime");
    private static final int EXPECTED_FIELD_COUNT = 5;

    private static final Path DEFAULT_DATA_FILE =
            Paths.get("./data/luckyNoSlacky.csv");

    private final Path dataFile;

    /**
     * Creates a saver that writes to the application's default data file.
     */
    public LuckyNoCSVSaver() {
        this(DEFAULT_DATA_FILE);
    }

    /**
     * Creates a saver that writes to a specified file.
     *
     * @param dataFile destination CSV file
     */
    LuckyNoCSVSaver(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Rewrites the CSV file with the current task list.
     *
     * @param taskMaster task list to save
     */
    public void save(TaskMaster taskMaster) {
        try {
            Files.createDirectories(dataFile.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(
                    dataFile, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                printer.printRecord(CSV_HEADER);

                for (List<String> record : taskMaster.getCSVStorageRecords()) {
                    printer.printRecord(record);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to save tasks.", exception);
        }
    }

    /**
     * Loads all tasks from the CSV file.
     *
     * @return tasks stored in the file, or an empty list if the file is absent
     */
    public List<Task> load() {
        if (!Files.exists(dataFile)) {
            return List.of();
        }

        try (BufferedReader reader = Files.newBufferedReader(
                dataFile, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            List<CSVRecord> records = parser.getRecords();

            if (records.isEmpty()) {
                return List.of();
            }

            validateHeader(records.get(0));

            List<Task> tasks = new ArrayList<>();
            for (int i = 1; i < records.size(); i++) {
                tasks.add(createTaskFromCSVStorageRecord(records.get(i)));
            }

            return List.copyOf(tasks);
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load tasks.", exception);
        }
    }

    private void validateHeader(CSVRecord header) {
        if (!header.toList().equals(CSV_HEADER)) {
            throw new IllegalStateException("Invalid task data header.");
        }
    }

    private Task createTaskFromCSVStorageRecord(CSVRecord record) {
        if (record.size() != EXPECTED_FIELD_COUNT) {
            throw new IllegalStateException(
                    "Invalid number of fields in task record.");
        }

        String taskType = record.get(0);
        String completionStatus = record.get(1);
        String description = record.get(2);
        String startTime = record.get(3);
        String finishTime = record.get(4);

        validateCompletionStatus(completionStatus);

        Task task = switch (taskType) {
        case "T" -> new TodoTask(description);
        case "D" -> new DeadlineTask(description, finishTime);
        case "E" -> new EventTask(description, startTime, finishTime);
        default -> throw new IllegalStateException(
                "Unknown task type in saved data.");
        };

        if (completionStatus.equals("1")) {
            task.markAsDone();
        }

        return task;
    }

    private void validateCompletionStatus(String completionStatus) {
        if (!completionStatus.equals("0")
                && !completionStatus.equals("1")) {
            throw new IllegalStateException(
                    "Invalid task completion status.");
        }
    }
}
