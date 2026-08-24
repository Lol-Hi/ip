import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void missingCsvFileLoadsAsEmptyList() {
        Path dataFile = temporaryDirectory.resolve("missing.csv");
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);

        assertTrue(saver.load().isEmpty());
    }

    @Test
    void saveCreatesMissingParentDirectories() {
        Path dataFile = temporaryDirectory
                .resolve("nested")
                .resolve("luckyNoSlacky.csv");
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);

        taskMaster.addTask(new TodoTask("read book"));

        assertTrue(Files.isRegularFile(dataFile));
    }

    @Test
    void emptyCsvFileLoadsAsEmptyList() throws Exception {
        Path dataFile = temporaryDirectory.resolve("empty.csv");
        Files.writeString(dataFile, "", StandardCharsets.UTF_8);
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);

        assertTrue(saver.load().isEmpty());
    }

    @Test
    void loadsTasksAndStatusesFromCsvStorage() {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);
        TaskMaster original = new TaskMaster(100, saver);

        original.addTask(new TodoTask("read, book"));
        original.addTask(new DeadlineTask("return book", "Sunday"));
        original.addTask(new EventTask("project meeting", "Mon 2pm", "4pm"));
        original.markTaskDone(1);

        List<Task> loadedTasks = saver.load();
        TaskMaster restored = new TaskMaster(100, saver);
        restored.loadTasksFromCSVStorageRecord(loadedTasks);

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][X] read, book\n"
                        + "2.[D][ ] return book (by: Sunday)\n"
                        + "3.[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                restored.listTasks());
    }

    @Test
    void invalidTaskTypeInCsvFileIsRejected() throws Exception {
        Path dataFile = temporaryDirectory.resolve("invalid.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,finishTime\n"
                        + "X,0,unknown task,,\n",
                StandardCharsets.UTF_8);
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    @Test
    void invalidCompletionStatusInCsvFileIsRejected() throws Exception {
        Path dataFile = temporaryDirectory.resolve("invalid-status.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,finishTime\n"
                        + "T,2,read book,,\n",
                StandardCharsets.UTF_8);
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    @Test
    void invalidHeaderInCsvFileIsRejected() throws Exception {
        Path dataFile = temporaryDirectory.resolve("invalid-header.csv");
        Files.writeString(dataFile,
                "type,status,description,start,end\n",
                StandardCharsets.UTF_8);
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    @Test
    void dataPathThatIsDirectoryCannotBeLoadedOrSaved() throws Exception {
        Path dataPath = temporaryDirectory.resolve("directory");
        Files.createDirectory(dataPath);
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataPath);

        assertThrows(LuckyNoStorageException.class, saver::load);
        assertThrows(LuckyNoStorageException.class,
                () -> saver.save(new TaskMaster(100, saver)));
    }

    @Test
    void loadedTaskCountCannotExceedTaskMasterCapacity() {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        LuckyNoCSVSaver saver = new LuckyNoCSVSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(1, saver);

        assertThrows(LuckyNoStorageException.class,
                () -> taskMaster.loadTasksFromCSVStorageRecord(List.of(
                        new TodoTask("first"),
                        new TodoTask("second"))));
    }
}
