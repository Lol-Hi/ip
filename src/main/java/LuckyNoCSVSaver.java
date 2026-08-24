import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Saves the current task list as a CSV file.
 */
public class LuckyNoCSVSaver {
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
                printer.printRecord(
                        "Task type",
                        "isCompleted",
                        "Description",
                        "startTime",
                        "finishTime");

                for (List<String> record : taskMaster.getCSVStorageRecords()) {
                    printer.printRecord(record);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to save tasks.", exception);
        }
    }
}
