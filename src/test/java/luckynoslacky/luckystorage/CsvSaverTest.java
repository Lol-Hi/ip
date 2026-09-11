package luckynoslacky.luckystorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckyparser.DurationPeriod;
import luckynoslacky.luckytask.DeadlineTask;
import luckynoslacky.luckytask.EventTask;
import luckynoslacky.luckytask.Task;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TodoTask;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests CSV persistence of task lists.
 */
class CsvSaverTest {
    private static final LocalDateTime DEADLINE =
            LocalDateTime.of(2026, 12, 6, 23, 59);
    private static final LocalDateTime EVENT_START =
            LocalDateTime.of(2026, 8, 6, 14, 0);
    private static final LocalDateTime EVENT_END =
            LocalDateTime.of(2026, 8, 6, 16, 0);
    @TempDir
    Path temporaryDirectory;

    /** Verifies CSV escaping for task records with special characters. */
    @Test
    void save_taskListWithSpecialCharacters_writesCsvRecords() throws Exception {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);

        taskMaster.addTask(new TodoTask("read, book"));
        taskMaster.addTask(new DeadlineTask("return \"book\"", DEADLINE));
        taskMaster.addTask(new EventTask("project meeting", EVENT_START, EVENT_END));
        taskMaster.markTaskDone(1);

        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            List<CSVRecord> records = parser.getRecords();

            assertEquals(List.of(
                    "Task type", "isCompleted", "Description", "startTime", "endTime"),
                    records.get(0).toList());
            assertEquals(List.of("T", "1", "read, book", "", ""),
                    records.get(1).toList());
            assertEquals(List.of("D", "0", "return \"book\"", "",
                            "2026-12-06 23:59"),
                    records.get(2).toList());
            assertEquals(List.of("E", "0", "project meeting",
                            "2026-08-06 14:00", "2026-08-06 16:00"),
                    records.get(3).toList());
        }
    }

    /** Verifies that a deadline duration snooze is written and reloadable. */
    @Test
    void snoozeTaskBy_deadlineDuration_writesUpdatedEndTimeToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("deadline-snooze.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0)));

        taskMaster.snoozeTaskBy(
                1, new DurationPeriod(Period.ZERO, Duration.ofHours(2)));

        assertCsvRecord(dataFile,
                List.of("D", "0", "return book", "", "2026-08-26 14:00"));
        TaskMaster restored = loadTaskMaster(dataFile);
        assertEquals(
                LocalDateTime.of(2026, 8, 26, 14, 0),
                restored.getTaskEndTime(1));
    }

    /** Verifies that an event duration snooze changes only the saved end time. */
    @Test
    void snoozeTaskBy_eventDuration_writesOnlyUpdatedEndTimeToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("event-snooze.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0)));

        taskMaster.snoozeTaskBy(
                1, new DurationPeriod(Period.ZERO, Duration.ofHours(2)));

        assertCsvRecord(dataFile,
                List.of("E", "0", "project meeting",
                        "2026-08-26 14:00", "2026-08-26 18:00"));
        TaskMaster restored = loadTaskMaster(dataFile);
        assertEquals(
                LocalDateTime.of(2026, 8, 26, 14, 0),
                restored.getTaskStartTime(1));
        assertEquals(
                LocalDateTime.of(2026, 8, 26, 18, 0),
                restored.getTaskEndTime(1));
    }

    /** Verifies that an explicit deadline snooze is written and reloadable. */
    @Test
    void snoozeTaskTo_deadlineTarget_writesNewEndTimeToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("deadline-snooze-to.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0)));

        taskMaster.snoozeTaskTo(
                1, LocalDateTime.of(2026, 8, 28, 17, 0));

        assertCsvRecord(dataFile,
                List.of("D", "0", "return book", "", "2026-08-28 17:00"));
        assertEquals(
                LocalDateTime.of(2026, 8, 28, 17, 0),
                loadTaskMaster(dataFile).getTaskEndTime(1));
    }

    /** Verifies that an explicit event snooze preserves the saved start time. */
    @Test
    void snoozeTaskTo_eventTarget_preservesStartTimeInCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("event-snooze-to.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0)));

        taskMaster.snoozeTaskTo(
                1, LocalDateTime.of(2026, 8, 28, 17, 0));

        assertCsvRecord(dataFile,
                List.of("E", "0", "project meeting",
                        "2026-08-26 14:00", "2026-08-28 17:00"));
        TaskMaster restored = loadTaskMaster(dataFile);
        assertEquals(
                LocalDateTime.of(2026, 8, 26, 14, 0),
                restored.getTaskStartTime(1));
        assertEquals(
                LocalDateTime.of(2026, 8, 28, 17, 0),
                restored.getTaskEndTime(1));
    }

    /** Verifies that a deadline reschedule is written and reloadable. */
    @Test
    void rescheduleDeadline_newTime_writesUpdatedDeadlineToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("deadline-reschedule.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0)));

        taskMaster.rescheduleDeadline(
                1, LocalDateTime.of(2026, 9, 1, 9, 30));

        assertCsvRecord(dataFile,
                List.of("D", "0", "return book", "", "2026-09-01 09:30"));
        assertEquals(
                LocalDateTime.of(2026, 9, 1, 9, 30),
                loadTaskMaster(dataFile).getTaskEndTime(1));
    }

    /** Verifies that full event rescheduling writes both event times. */
    @Test
    void rescheduleEvent_newTimes_writesBothEventTimesToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("event-reschedule.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0)));

        taskMaster.rescheduleEvent(
                1,
                LocalDateTime.of(2026, 8, 28, 10, 0),
                LocalDateTime.of(2026, 8, 28, 11, 0));

        assertCsvRecord(dataFile,
                List.of("E", "0", "project meeting",
                        "2026-08-28 10:00", "2026-08-28 11:00"));
        TaskMaster restored = loadTaskMaster(dataFile);
        assertEquals(
                LocalDateTime.of(2026, 8, 28, 10, 0),
                restored.getTaskStartTime(1));
        assertEquals(
                LocalDateTime.of(2026, 8, 28, 11, 0),
                restored.getTaskEndTime(1));
    }

    /** Verifies that partial event updates retain the omitted time in CSV. */
    @Test
    void rescheduleEvent_partialUpdates_writeRetainedTimes() throws Exception {
        Path dataFile = temporaryDirectory.resolve("partial-event-reschedule.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0)));

        taskMaster.rescheduleEvent(
                1,
                LocalDateTime.of(2026, 8, 26, 15, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0));
        assertCsvRecord(dataFile,
                List.of("E", "0", "project meeting",
                        "2026-08-26 15:00", "2026-08-26 16:00"));

        taskMaster.rescheduleEvent(
                1,
                LocalDateTime.of(2026, 8, 26, 15, 0),
                LocalDateTime.of(2026, 8, 26, 18, 0));
        assertCsvRecord(dataFile,
                List.of("E", "0", "project meeting",
                        "2026-08-26 15:00", "2026-08-26 18:00"));
        assertEquals(
                LocalDateTime.of(2026, 8, 26, 18, 0),
                loadTaskMaster(dataFile).getTaskEndTime(1));
    }

    /** Verifies that a completed task remains completed after a time update. */
    @Test
    void snoozeTaskBy_completedTask_preservesCompletionStatusInCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("completed-snooze.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0)));
        taskMaster.markTaskDone(1);

        taskMaster.snoozeTaskBy(
                1, new DurationPeriod(Period.ZERO, Duration.ofHours(2)));

        assertCsvRecord(dataFile,
                List.of("D", "1", "return book", "", "2026-08-26 14:00"));
        assertTrue(loadTaskMaster(dataFile).listTasks().toDisplayString()
                .contains("[D][X] return book"));
    }

    /** Verifies saving rewrites the file after a task is deleted. */
    @Test
    void save_afterTaskDeletion_rewritesCsvFile() throws Exception {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        CsvSaver saver = new CsvSaver(dataFile);
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

    /** Verifies a missing data file is treated as an empty task list. */
    @Test
    void load_missingFile_returnsEmptyList() {
        Path dataFile = temporaryDirectory.resolve("missing.csv");
        CsvSaver saver = new CsvSaver(dataFile);

        assertTrue(saver.load().isEmpty());
    }

    /** Verifies saving creates a missing parent directory. */
    @Test
    void save_missingParentDirectory_createsDirectoryAndFile() {
        Path dataFile = temporaryDirectory
                .resolve("nested")
                .resolve("luckyNoSlacky.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);

        taskMaster.addTask(new TodoTask("read book"));

        assertTrue(Files.isRegularFile(dataFile));
    }

    /** Verifies an empty data file loads as an empty task list. */
    @Test
    void load_emptyFile_returnsEmptyList() throws Exception {
        Path dataFile = temporaryDirectory.resolve("empty.csv");
        Files.writeString(dataFile, "", StandardCharsets.UTF_8);
        CsvSaver saver = new CsvSaver(dataFile);

        assertTrue(saver.load().isEmpty());
    }

    /** Verifies valid CSV records restore tasks and completion statuses. */
    @Test
    void load_validCsv_returnsTasksAndStatuses() {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster original = new TaskMaster(100, saver);

        original.addTask(new TodoTask("read, book"));
        original.addTask(new DeadlineTask("return book", DEADLINE));
        original.addTask(new EventTask("project meeting", EVENT_START, EVENT_END));
        original.markTaskDone(1);

        List<Task> loadedTasks = saver.load();
        TaskMaster restored = new TaskMaster(100, saver);
        restored.loadTasksFromCsvStorageRecord(loadedTasks);

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][X] read, book\n"
                        + "2.[D][ ] return book (by: Sun Dec 06 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                LuckyNoMessages.listTasksMessage(restored.listTasks()));
    }

    /** Verifies unknown task types are rejected during loading. */
    @Test
    void load_unknownTaskType_throwsStorageException() throws Exception {
        Path dataFile = temporaryDirectory.resolve("invalid.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,endTime\n"
                        + "X,0,unknown task,,\n",
                StandardCharsets.UTF_8);
        CsvSaver saver = new CsvSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    /** Verifies invalid completion flags are rejected during loading. */
    @Test
    void load_invalidCompletionStatus_throwsStorageException() throws Exception {
        Path dataFile = temporaryDirectory.resolve("invalid-status.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,endTime\n"
                        + "T,2,read book,,\n",
                StandardCharsets.UTF_8);
        CsvSaver saver = new CsvSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    /** Verifies invalid CSV headers are rejected during loading. */
    @Test
    void load_invalidHeader_throwsStorageException() throws Exception {
        Path dataFile = temporaryDirectory.resolve("invalid-header.csv");
        Files.writeString(dataFile,
                "type,status,description,start,end\n",
                StandardCharsets.UTF_8);
        CsvSaver saver = new CsvSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    /** Verifies malformed task records are rejected during loading. */
    @Test
    void load_malformedTaskRecord_throwsStorageException() throws Exception {
        Path dataFile = temporaryDirectory.resolve("malformed-record.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,endTime\n"
                        + "D,0,return book,,2030-13-01 10:00\n",
                StandardCharsets.UTF_8);
        CsvSaver saver = new CsvSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    /** Verifies records with missing fields are rejected during loading. */
    @Test
    void load_recordWithMissingFields_throwsStorageException() throws Exception {
        Path dataFile = temporaryDirectory.resolve("missing-fields.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,endTime\n"
                        + "T,0,read book,\n",
                StandardCharsets.UTF_8);
        CsvSaver saver = new CsvSaver(dataFile);

        assertThrows(LuckyNoStorageException.class, saver::load);
    }

    /** Verifies saving a null task master is rejected. */
    @Test
    void save_nullTaskMaster_throwsIllegalArgumentException() {
        CsvSaver saver = new CsvSaver(temporaryDirectory.resolve("tasks.csv"));

        assertThrows(IllegalArgumentException.class, () -> saver.save(null));
    }

    /** Verifies that a saver rejects a missing data-file path. */
    @Test
    void construct_nullDataFile_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new CsvSaver(null));
    }

    /** Verifies file paths that point to directories are rejected. */
    @Test
    void loadOrSave_directoryPath_throwsStorageException() throws Exception {
        Path dataPath = temporaryDirectory.resolve("directory");
        Files.createDirectory(dataPath);
        CsvSaver saver = new CsvSaver(dataPath);

        assertThrows(LuckyNoStorageException.class, saver::load);
        assertThrows(LuckyNoStorageException.class, () ->
                saver.save(new TaskMaster(100, saver)));
    }

    /** Verifies loading beyond task capacity is rejected. */
    @Test
    void load_tasksExceedingCapacity_throwsStorageException() {
        Path dataFile = temporaryDirectory.resolve("luckyNoSlacky.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(1, saver);

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.loadTasksFromCsvStorageRecord(List.of(
                        new TodoTask("first"),
                        new TodoTask("second"))));
    }

    /** Reads all records from a saved CSV file. */
    private List<CSVRecord> readCsvRecords(Path dataFile) throws Exception {
        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            return parser.getRecords();
        }
    }

    /** Verifies the first saved task record after the CSV header. */
    private void assertCsvRecord(
            Path dataFile,
            List<String> expectedRecord)
            throws Exception {
        List<CSVRecord> records = readCsvRecords(dataFile);
        assertEquals(expectedRecord, records.get(1).toList());
    }

    /** Loads a task master from the supplied CSV file. */
    private TaskMaster loadTaskMaster(Path dataFile) {
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.loadTasksFromCsvStorageRecord(saver.load());
        return taskMaster;
    }
}
