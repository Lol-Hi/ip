import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests CSV persistence of task lists.
 */
class LuckyNoCSVSaverTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void savesCurrentTaskListAsCsvRecords() throws Exception {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);

        taskMaster.addTask(new TodoTask("read, book"));
        taskMaster.addTask(new DeadlineTask("return \"book\"", "Sunday"));
        taskMaster.addTask(new EventTask("project meeting", "Mon 2pm", "4pm"));
        taskMaster.markTaskDone(1);

        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            List<CSVRecord> records = parser.getRecords();

            assertEquals(List.of(
                    "Task type", "isCompleted", "Description", "startTime", "finishTime"),
                    records.get(0).toList());
            assertEquals(List.of("T", "1", "read, book", "", ""),
                    records.get(1).toList());
            assertEquals(List.of("D", "0", "return \"book\"", "", "Sunday"),
                    records.get(2).toList());
            assertEquals(List.of("E", "0", "project meeting", "Mon 2pm", "4pm"),
                    records.get(3).toList());
        }
    }

    @Test
    void rewritesFileAfterTaskIsDeleted() throws Exception {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);

        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));
        taskMaster.deleteTask(1);

        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            List<CSVRecord> records = parser.getRecords();

            assertEquals(2, records.size());
            assertEquals(List.of("T", "0", "second", "", ""),
                    records.get(1).toList());
        }
    }
}
