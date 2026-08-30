package luckynoslacky.luckystorage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckyparser.DateTimeParser;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TodoTask;

/**
 * Saves the current task list as a CSV file.
 */
public class CsvSaver {
    private static final List<String> CSV_HEADER = List.of(
            "Task type",
            "isCompleted",
            "Description",
            "startTime",
            "finishTime");
    private static final int EXPECTED_FIELD_COUNT = 5;

    private static final Path DEFAULT_DATA_FILE =
            Paths.get("data", "luckyNoSlacky.csv");

    private final Path dataFile;

    /**
     * Creates a saver that writes to the application's default data file.
     */
    public CsvSaver() {
        this(DEFAULT_DATA_FILE);
    }

    /**
     * Creates a saver that writes to a specified file.
     *
     * @param dataFile destination CSV file
     */
    public CsvSaver(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Rewrites the CSV file with the current task list.
     *
     * @param taskMaster task list to save
     */
    public void save(TaskMaster taskMaster) {
        if (taskMaster == null) {
            throw new IllegalArgumentException("Task master cannot be null.");
        }

        Path temporaryFile = null;
        try {
            createParentDirectory();
            temporaryFile = createTemporaryFile();
            writeCsvFile(taskMaster, temporaryFile);
            replaceDataFile(temporaryFile);
        } catch (IOException | IllegalArgumentException exception) {
            throw new LuckyNoStorageException(
                    "Unable to save tasks.", exception);
        } finally {
            deleteTemporaryFile(temporaryFile);
        }
    }

    /**
     * Creates the parent directory for the data file when necessary.
     *
     * @throws IOException if the directory cannot be created
     */
    private void createParentDirectory() throws IOException {
        Path parent = dataFile.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Creates a temporary file in the data file's directory.
     *
     * @return path to the temporary file
     * @throws IOException if the temporary file cannot be created
     */
    private Path createTemporaryFile() throws IOException {
        Path parent = dataFile.getParent();

        return parent == null
                ? Files.createTempFile("luckyNoSlacky-", ".tmp")
                : Files.createTempFile(parent, "luckyNoSlacky-", ".tmp");
    }

    /**
     * Writes the CSV header and task records to a temporary file.
     *
     * @param taskMaster task list to serialize
     * @param outputFile temporary output file
     * @throws IOException if writing the file fails
     */
    private void writeCsvFile(TaskMaster taskMaster, Path outputFile)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                outputFile, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(CSV_HEADER);

            for (List<String> record : taskMaster.getCsvStorageRecords()) {
                printer.printRecord(record);
            }
        }
    }

    /**
     * Replaces the data file with the completed temporary file.
     *
     * @param temporaryFile completed temporary file
     * @throws IOException if the replacement fails
     */
    private void replaceDataFile(Path temporaryFile) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    temporaryFile,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Deletes a temporary file after a save attempt.
     *
     * @param temporaryFile temporary file to delete, or null if none was made
     */
    private void deleteTemporaryFile(Path temporaryFile) {
        if (temporaryFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException exception) {
            // The original save error, if any, is more useful to the caller.
        }
    }

    /**
     * Loads all tasks from the CSV file.
     *
     * @return tasks stored in the file, or an empty list if the file is absent
     */
    public List<Task> load() {
        if (Files.notExists(dataFile)) {
            return List.of();
        }

        if (!Files.isRegularFile(dataFile)) {
            throw new LuckyNoStorageException(
                    "The task data path is not a regular file.");
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
                tasks.add(createTaskFromCsvStorageRecord(records.get(i)));
            }

            return List.copyOf(tasks);
        } catch (IOException exception) {
            throw new LuckyNoStorageException(
                    "Unable to load tasks.", exception);
        } catch (LuckyNoStorageException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new LuckyNoStorageException(
                    "Unable to load tasks.", exception);
        }
    }

    /**
     * Validates that a CSV header has the expected columns.
     *
     * @param header first CSV record in the data file
     * @throws LuckyNoStorageException if the header is not recognized
     */
    private void validateHeader(CSVRecord header) {
        if (!header.toList().equals(CSV_HEADER)) {
            throw new LuckyNoStorageException("Invalid task data header.");
        }
    }

    /**
     * Converts one CSV record into the corresponding task subtype.
     *
     * @param record CSV task record
     * @return task represented by the record
     * @throws LuckyNoStorageException if the record is malformed
     */
    private Task createTaskFromCsvStorageRecord(CSVRecord record) {
        if (record.size() != EXPECTED_FIELD_COUNT) {
            throw invalidRecord(record, "incorrect number of fields");
        }

        String taskType = record.get(0);
        String completionStatus = record.get(1);
        String description = record.get(2);
        String startTimeText = record.get(3);
        String finishTimeText = record.get(4);

        validateCompletionStatus(record);

        Task task;
        try {
            task = switch (taskType) {
                case "T" -> new TodoTask(description);
                case "D" -> new DeadlineTask(
                        description,
                        DateTimeParser.parseFromStorage(finishTimeText));
                case "E" -> new EventTask(
                        description,
                        DateTimeParser.parseFromStorage(startTimeText),
                        DateTimeParser.parseFromStorage(finishTimeText));
                default -> throw invalidRecord(record, "unknown task type");
            };
        } catch (IllegalArgumentException exception) {
            throw new LuckyNoStorageException(
                    "Invalid task data at row "
                            + record.getRecordNumber(),
                    exception);
        }

        if (completionStatus.equals("1")) {
            task.markAsDone();
        }

        return task;
    }

    /**
     * Validates the completion flag stored in a CSV record.
     *
     * @param record CSV task record
     * @throws LuckyNoStorageException if the flag is neither 0 nor 1
     */
    private void validateCompletionStatus(CSVRecord record) {
        String completionStatus = record.get(1);

        if (!completionStatus.equals("0")
                && !completionStatus.equals("1")) {
            throw invalidRecord(record, "invalid completion status");
        }
    }

    /**
     * Creates a storage exception describing an invalid CSV record.
     *
     * @param record malformed CSV record
     * @param reason explanation of the validation failure
     * @return exception describing the invalid record
     */
    private LuckyNoStorageException invalidRecord(
            CSVRecord record,
            String reason) {
        return new LuckyNoStorageException(
                "Invalid task record at row "
                        + record.getRecordNumber()
                        + ": " + reason);
    }
}
