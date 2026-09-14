package luckynoslacky.luckystorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
import luckynoslacky.luckytask.TaskList;
import luckynoslacky.luckytask.TaskMaster;
import luckynoslacky.luckytask.TaskTimes;
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
    void rescheduleTask_newDeadlineTime_writesUpdatedDeadlineToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("deadline-reschedule.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 12, 0)));

        taskMaster.rescheduleTask(
                1,
                TaskTimes.makeDeadlineTimes(
                        LocalDateTime.of(2026, 9, 1, 9, 30)));

        assertCsvRecord(dataFile,
                List.of("D", "0", "return book", "", "2026-09-01 09:30"));
        assertEquals(
                LocalDateTime.of(2026, 9, 1, 9, 30),
                loadTaskMaster(dataFile).getTaskEndTime(1));
    }

    /** Verifies that full event rescheduling writes both event times. */
    @Test
    void rescheduleTask_newEventTimes_writesBothEventTimesToCsv() throws Exception {
        Path dataFile = temporaryDirectory.resolve("event-reschedule.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0)));

        taskMaster.rescheduleTask(
                1,
                TaskTimes.makeEventTimes(
                        LocalDateTime.of(2026, 8, 28, 10, 0),
                        LocalDateTime.of(2026, 8, 28, 11, 0)));

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
    void rescheduleTask_partialEventTimes_writeRetainedTimes() throws Exception {
        Path dataFile = temporaryDirectory.resolve("partial-event-reschedule.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 26, 14, 0),
                LocalDateTime.of(2026, 8, 26, 16, 0)));

        taskMaster.rescheduleTask(
                1,
                TaskTimes.makeEventTimes(
                        LocalDateTime.of(2026, 8, 26, 15, 0),
                        LocalDateTime.of(2026, 8, 26, 16, 0)));
        assertCsvRecord(dataFile,
                List.of("E", "0", "project meeting",
                        "2026-08-26 15:00", "2026-08-26 16:00"));

        taskMaster.rescheduleTask(
                1,
                TaskTimes.makeEventTimes(
                        LocalDateTime.of(2026, 8, 26, 15, 0),
                        LocalDateTime.of(2026, 8, 26, 18, 0)));
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

    /** Verifies load-side security failures retain their original cause. */
    @Test
    void load_securityFailures_throwStorageException() throws Exception {
        Path dataFile = temporaryDirectory.resolve("secured.csv");
        Files.writeString(dataFile,
                "Task type,isCompleted,Description,startTime,endTime\n",
                StandardCharsets.UTF_8);
        for (FileOperation operation : List.of(
                FileOperation.NOT_EXISTS,
                FileOperation.IS_REGULAR_FILE,
                FileOperation.NEW_BUFFERED_READER)) {
            CsvSaver saver = new CsvSaver(
                    dataFile,
                    new FaultInjectingFileOperations(Set.of(operation)));

            LuckyNoStorageException exception = assertThrows(
                    LuckyNoStorageException.class, saver::load);

            assertEquals("Unable to load tasks.", exception.getMessage());
            assertInstanceOf(SecurityException.class, exception.getCause());
        }
    }

    /** Verifies save-side security failures consistently become storage errors. */
    @Test
    void save_securityFailures_throwStorageException() {
        for (FileOperation operation : List.of(
                FileOperation.CREATE_DIRECTORIES,
                FileOperation.CREATE_TEMPORARY_FILE,
                FileOperation.NEW_BUFFERED_WRITER,
                FileOperation.MOVE_ATOMICALLY,
                FileOperation.DELETE_IF_EXISTS)) {
            CsvSaver saver = new CsvSaver(
                    temporaryDirectory.resolve(operation.name() + ".csv"),
                    new FaultInjectingFileOperations(Set.of(operation)));

            LuckyNoStorageException exception = assertThrows(
                    LuckyNoStorageException.class, () -> saver.save(createTaskList()));

            assertEquals("Unable to save tasks.", exception.getMessage());
            assertInstanceOf(SecurityException.class, exception.getCause());
        }
    }

    /** Verifies fallback replacement security failures become storage errors. */
    @Test
    void save_nonAtomicMoveSecurityFailure_throwsStorageException() {
        CsvSaver saver = new CsvSaver(
                temporaryDirectory.resolve("fallback-move.csv"),
                new FaultInjectingFileOperations(
                        Set.of(FileOperation.MOVE_REPLACING), true));

        LuckyNoStorageException exception = assertThrows(
                LuckyNoStorageException.class, () -> saver.save(createTaskList()));

        assertEquals("Unable to save tasks.", exception.getMessage());
        assertInstanceOf(SecurityException.class, exception.getCause());
    }

    /** Verifies a primary save failure retains a cleanup failure as suppressed. */
    @Test
    void save_primaryAndCleanupFailures_preservesPrimaryFailure() {
        CsvSaver saver = new CsvSaver(
                temporaryDirectory.resolve("multiple-failures.csv"),
                new FaultInjectingFileOperations(Set.of(
                        FileOperation.MOVE_ATOMICALLY,
                        FileOperation.DELETE_IF_EXISTS)));

        LuckyNoStorageException exception = assertThrows(
                LuckyNoStorageException.class, () -> saver.save(createTaskList()));

        assertEquals("MOVE_ATOMICALLY", exception.getCause().getMessage());
        assertEquals(1, exception.getSuppressed().length);
        assertInstanceOf(
                SecurityException.class,
                exception.getSuppressed()[0].getCause());
    }

    /** Verifies a cleanup-only failure causes task mutations to roll back. */
    @Test
    void addTask_cleanupFailure_rollsBackTaskList() {
        CsvSaver saver = new CsvSaver(
                temporaryDirectory.resolve("cleanup-failure.csv"),
                new FaultInjectingFileOperations(Set.of(FileOperation.DELETE_IF_EXISTS)));
        TaskMaster taskMaster = new TaskMaster(100, saver);

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.addTask(new TodoTask("read book")));

        assertEquals(0, taskMaster.getTaskCount());
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

    /** Verifies saving a null task list is rejected. */
    @Test
    void save_nullTaskList_throwsIllegalArgumentException() {
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
                saver.save(new TaskList()));
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

    /** Creates a task list containing one task for save-failure tests. */
    private TaskList createTaskList() {
        TaskList taskList = new TaskList();
        taskList.addTask(new TodoTask("read book"));
        return taskList;
    }

    /** Identifies a filesystem operation that can raise a security failure. */
    private enum FileOperation {
        NOT_EXISTS,
        IS_REGULAR_FILE,
        NEW_BUFFERED_READER,
        CREATE_DIRECTORIES,
        CREATE_TEMPORARY_FILE,
        NEW_BUFFERED_WRITER,
        MOVE_ATOMICALLY,
        MOVE_REPLACING,
        DELETE_IF_EXISTS
    }

    /** Provides real filesystem behavior with selected deterministic failures. */
    private static final class FaultInjectingFileOperations
            implements CsvSaver.FileOperations {
        private final Set<FileOperation> failingOperations;
        private final boolean doesNotSupportAtomicMove;

        private FaultInjectingFileOperations(Set<FileOperation> failingOperations) {
            this(failingOperations, false);
        }

        private FaultInjectingFileOperations(
                Set<FileOperation> failingOperations,
                boolean doesNotSupportAtomicMove) {
            this.failingOperations = EnumSet.copyOf(failingOperations);
            this.doesNotSupportAtomicMove = doesNotSupportAtomicMove;
        }

        @Override
        public boolean notExists(Path path) {
            failWhenConfigured(FileOperation.NOT_EXISTS);
            return Files.notExists(path);
        }

        @Override
        public boolean isRegularFile(Path path) {
            failWhenConfigured(FileOperation.IS_REGULAR_FILE);
            return Files.isRegularFile(path);
        }

        @Override
        public BufferedReader newBufferedReader(Path path) throws IOException {
            failWhenConfigured(FileOperation.NEW_BUFFERED_READER);
            return Files.newBufferedReader(path, StandardCharsets.UTF_8);
        }

        @Override
        public void createDirectories(Path directory) throws IOException {
            failWhenConfigured(FileOperation.CREATE_DIRECTORIES);
            Files.createDirectories(directory);
        }

        @Override
        public Path createTemporaryFile(Path directory) throws IOException {
            failWhenConfigured(FileOperation.CREATE_TEMPORARY_FILE);
            return directory == null
                    ? Files.createTempFile("luckyNoSlacky-", ".tmp")
                    : Files.createTempFile(directory, "luckyNoSlacky-", ".tmp");
        }

        @Override
        public BufferedWriter newBufferedWriter(Path path) throws IOException {
            failWhenConfigured(FileOperation.NEW_BUFFERED_WRITER);
            return Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        }

        @Override
        public void moveAtomically(Path source, Path destination) throws IOException {
            failWhenConfigured(FileOperation.MOVE_ATOMICALLY);
            if (doesNotSupportAtomicMove) {
                throw new AtomicMoveNotSupportedException(
                        source.toString(), destination.toString(), "Test fallback");
            }
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        }

        @Override
        public void moveReplacing(Path source, Path destination) throws IOException {
            failWhenConfigured(FileOperation.MOVE_REPLACING);
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public boolean deleteIfExists(Path path) throws IOException {
            failWhenConfigured(FileOperation.DELETE_IF_EXISTS);
            return Files.deleteIfExists(path);
        }

        /** Throws a security exception when an operation was configured to fail. */
        private void failWhenConfigured(FileOperation operation) {
            if (failingOperations.contains(operation)) {
                throw new SecurityException(operation.name());
            }
        }
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
