package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckyexception.LuckyNoStorageException;
import luckynoslacky.luckyparser.DurationPeriod;
import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Tests the task storage and listing behavior of TaskMaster.
 */
class TaskMasterTest {
    private static final LocalDateTime DEADLINE =
            LocalDateTime.of(2026, 12, 6, 23, 59);
    private static final LocalDateTime EVENT_START =
            LocalDateTime.of(2026, 8, 6, 14, 0);
    private static final LocalDateTime EVENT_END =
            LocalDateTime.of(2026, 8, 6, 16, 0);

    @TempDir
    Path temporaryDirectory;

    /** Verifies the response for an empty task list. */
    @Test
    void listTasks_emptyTaskList_returnsEmptyTaskListMessage() {
        TaskMaster taskMaster = createTaskMaster();

        assertEquals("Chill lah bro got nothing yet lah!",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that a valid task is added and listed. */
    @Test
    void addTask_validTask_includesTaskInList() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));

        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that listing preserves task insertion order. */
    @Test
    void listTasks_multipleTasks_preservesOrder() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][ ] return book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that all supported task types are formatted in a list. */
    @Test
    void listTasks_differentTaskTypes_formatsAllTypes() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("borrow book"));
        taskMaster.addTask(new DeadlineTask("return book", DEADLINE));
        taskMaster.addTask(new EventTask("project meeting", EVENT_START, EVENT_END));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] borrow book\n"
                        + "2.[D][ ] return book (by: Sun Dec 06 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that search returns deadlines and events on a date. */
    @Test
    void findTasks_matchingDate_returnsDeadlinesAndEvents() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        taskMaster.addTask(new EventTask(
                "project meeting",
                LocalDateTime.of(2026, 8, 25, 14, 0),
                LocalDateTime.of(2026, 8, 27, 16, 0)));

        assertEquals("Nah, all these things you need to do on: Aug 26 2026\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)\n"
                        + "3.[E][ ] project meeting (from: Tue Aug 25 2026, 2.00pm"
                        + " to: Thu Aug 27 2026, 4.00pm)",
                LuckyNoMessages.listTasksMessage(
                        taskMaster.findTasks(LocalDateTime.of(2026, 8, 26, 0, 0))));
    }

    /** Verifies that search excludes ToDos and dates without matches. */
    @Test
    void findTasks_noMatchingDateOrTodoOnly_returnsEmptyTaskListMessage() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));

        assertEquals("Chill lah bro got nothing yet lah!",
                LuckyNoMessages.listTasksMessage(
                        taskMaster.findTasks(LocalDateTime.of(2026, 8, 25, 0, 0))));
        assertEquals("Chill lah bro got nothing yet lah!",
                LuckyNoMessages.listTasksMessage(
                        taskMaster.findTasks(LocalDateTime.of(2026, 8, 27, 0, 0))));
    }

    /** Verifies that marking a valid task persists its done status. */
    @Test
    void findTasks_descriptionQuery_matchesDescriptionsCaseInsensitively() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        taskMaster.addTask(new TodoTask("buy bread"));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)",
                LuckyNoMessages.listTasksMessage(taskMaster.findTasks("BOOK")));
    }

    @Test
    void findTasks_descriptionAndDateQueries_returnsIntersection() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new DeadlineTask(
                "return book", LocalDateTime.of(2026, 8, 26, 23, 59)));
        taskMaster.addTask(new EventTask(
                "book meeting",
                LocalDateTime.of(2026, 8, 27, 14, 0),
                LocalDateTime.of(2026, 8, 27, 16, 0)));

        assertEquals("Nah, all these things you need to do on: Aug 26 2026\n"
                        + "2.[D][ ] return book (by: Wed Aug 26 2026, 11.59pm)",
                LuckyNoMessages.listTasksMessage(taskMaster.findTasks(
                        "book",
                        LocalDateTime.of(2026, 8, 26, 0, 0))));
    }

    @Test
    void markTaskDone_validTask_marksTaskAsDone() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));
        taskMaster.markTaskDone(2);

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][X] return book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that unmarking a done task clears its status. */
    @Test
    void unmarkTaskUndone_doneTask_marksTaskAsUndone() {
        TaskMaster taskMaster = createTaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.markTaskDone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that marking an already done task is idempotent. */
    @Test
    void markTaskDone_alreadyDoneTask_remainsDone() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.markTaskDone(1);
        taskMaster.markTaskDone(1);

        assertEquals("Nah, all these things you need to do:\n1.[T][X] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that unmarking an incomplete task is idempotent. */
    @Test
    void unmarkTaskUndone_alreadyUndoneTask_remainsUndone() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.unmarkTaskUndone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that a deadline snooze extends its due time. */
    @Test
    void snoozeTaskBy_deadlineDuration_updatesDeadline() {
        TaskMaster taskMaster = createTaskMaster();
        DeadlineTask task = new DeadlineTask("return book", DEADLINE);
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        taskMaster.snoozeTaskBy(1, new DurationPeriod(
                java.time.Period.ZERO, java.time.Duration.ofHours(2)));

        assertEquals(DEADLINE.plusHours(2), task.getByTime());
    }

    /** Verifies that an event snooze extends only its end time. */
    @Test
    void snoozeTaskBy_eventDuration_updatesOnlyEventEnd() {
        TaskMaster taskMaster = createTaskMaster();
        EventTask task = new EventTask("project meeting", EVENT_START, EVENT_END);
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        taskMaster.snoozeTaskBy(1, new DurationPeriod(
                java.time.Period.ZERO, java.time.Duration.ofHours(2)));

        assertEquals(EVENT_START, task.getStartTime());
        assertEquals(EVENT_END.plusHours(2), task.getEndTime());
    }

    /** Verifies that a combined snooze is persisted with task status. */
    @Test
    void snoozeTaskBy_combinedDuration_persistsUpdatedTimeAndStatus() {
        Path dataFile = temporaryDirectory.resolve("combined-snooze.csv");
        CsvSaver saver = new CsvSaver(dataFile);
        TaskMaster taskMaster = new TaskMaster(100, saver);
        EventTask task = new EventTask(
                "project meeting",
                LocalDateTime.of(2028, 1, 31, 14, 0),
                LocalDateTime.of(2028, 1, 31, 16, 0));
        task.markAsDone();
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        taskMaster.snoozeTaskBy(1, new DurationPeriod(
                java.time.Period.ofMonths(1), java.time.Duration.ofHours(12)));

        EventTask reloadedTask = (EventTask) new CsvSaver(dataFile).load().get(0);
        assertEquals(
                LocalDateTime.of(2028, 1, 31, 14, 0),
                reloadedTask.getStartTime());
        assertEquals(
                LocalDateTime.of(2028, 3, 1, 4, 0),
                reloadedTask.getEndTime());
        assertTrue(reloadedTask.isDone());
    }

    /** Verifies that explicit rescheduling replaces a deadline. */
    @Test
    void rescheduleDeadline_newTime_replacesDeadline() {
        TaskMaster taskMaster = createTaskMaster();
        DeadlineTask task = new DeadlineTask("return book", DEADLINE);
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));
        LocalDateTime newDeadline = LocalDateTime.of(2026, 12, 7, 10, 0);

        taskMaster.rescheduleDeadline(1, newDeadline);

        assertEquals(newDeadline, task.getByTime());
    }

    /** Verifies that explicit event rescheduling replaces both event times. */
    @Test
    void rescheduleEvent_validTimes_replacesBothEventTimes() {
        TaskMaster taskMaster = createTaskMaster();
        EventTask task = new EventTask("project meeting", EVENT_START, EVENT_END);
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));
        LocalDateTime newStart = LocalDateTime.of(2026, 8, 7, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 8, 7, 11, 0);

        taskMaster.rescheduleEvent(1, newStart, newEnd);

        assertEquals(newStart, task.getStartTime());
        assertEquals(newEnd, task.getEndTime());
    }

    /** Verifies that a ToDo cannot be snoozed or rescheduled. */
    @Test
    void snoozeOrReschedule_todoTask_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.loadTasksFromCsvStorageRecord(List.of(new TodoTask("read book")));

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.snoozeTaskBy(1, new DurationPeriod(
                        java.time.Period.ZERO, java.time.Duration.ofHours(1))));
        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.rescheduleDeadline(1, DEADLINE));
    }

    /** Verifies that a failed snooze save restores the original event times. */
    @Test
    void snoozeTaskBy_saveFailure_restoresOriginalEventTimes() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        EventTask task = new EventTask("project meeting", EVENT_START, EVENT_END);
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.snoozeTaskBy(1, new DurationPeriod(
                        java.time.Period.ZERO, java.time.Duration.ofHours(2))));

        assertEquals(EVENT_START, task.getStartTime());
        assertEquals(EVENT_END, task.getEndTime());
    }

    /** Verifies that a failed event rescheduling save restores both times. */
    @Test
    void rescheduleEvent_saveFailure_restoresOriginalEventTimes() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        EventTask task = new EventTask("project meeting", EVENT_START, EVENT_END);
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));
        LocalDateTime newStart = LocalDateTime.of(2026, 8, 7, 10, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 8, 7, 11, 0);

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.rescheduleEvent(1, newStart, newEnd));

        assertEquals(EVENT_START, task.getStartTime());
        assertEquals(EVENT_END, task.getEndTime());
    }

    /** Verifies that task-type lookup exposes only the requested category. */
    @Test
    void getTaskType_existingTask_returnsTaskCategory() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.loadTasksFromCsvStorageRecord(List.of(
                new TodoTask("read book"), new DeadlineTask("return book", DEADLINE),
                new EventTask("project meeting", EVENT_START, EVENT_END)));

        assertEquals(Task.TaskType.TODO, taskMaster.getTaskType(1));
        assertEquals(Task.TaskType.DEADLINE, taskMaster.getTaskType(2));
        assertEquals(Task.TaskType.EVENT, taskMaster.getTaskType(3));
    }

    /** Verifies that deletion removes and renumbers later tasks. */
    @Test
    void deleteTask_middleTask_removesAndRenumbersTasks() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new DeadlineTask("second", DEADLINE));
        taskMaster.addTask(new EventTask("third", EVENT_START, EVENT_END));

        assertEquals("[D][ ] second (by: Sun Dec 06 2026, 11.59pm)",
                taskMaster.deleteTask(2));
        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] first\n"
                        + "2.[E][ ] third (from: Thu Aug 06 2026, 2.00pm"
                        + " to: Thu Aug 06 2026, 4.00pm)",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that an invalid deletion leaves the list unchanged. */
    @Test
    void deleteTask_invalidTaskNumber_preservesTaskList() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.deleteTask(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that deletion frees capacity for a later task. */
    @Test
    void deleteTask_fullList_freesCapacity() {
        TaskMaster taskMaster = createTaskMaster(2);
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        taskMaster.deleteTask(1);
        taskMaster.addTask(new TodoTask("third"));

        assertEquals("Nah, all these things you need to do:\n"
                        + "1.[T][ ] second\n"
                        + "2.[T][ ] third",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that deletion from an empty list is rejected. */
    @Test
    void deleteTask_emptyList_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.deleteTask(1));
    }

    /** Verifies that invalid numbers are rejected when marking tasks. */
    @Test
    void markTaskDone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.markTaskDone(0));
        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.markTaskDone(2));
    }

    /** Verifies that invalid numbers are rejected when unmarking tasks. */
    @Test
    void unmarkTaskUndone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.unmarkTaskUndone(0));
        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.unmarkTaskUndone(2));
    }

    /** Verifies invalid status changes do not alter another task's state. */
    @Test
    void markOrUnmark_invalidTaskNumber_preservesTaskState() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.markTaskDone(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));

        taskMaster.markTaskDone(1);
        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.unmarkTaskUndone(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][X] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that the configured task capacity is enforced. */
    @Test
    void addTask_customCapacity_throwsWhenFull() {
        TaskMaster taskMaster = createTaskMaster(2);

        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        assertThrows(IllegalStateException.class, () ->
                taskMaster.addTask(new TodoTask("third")));
    }

    /** Verifies that non-positive capacities are rejected. */
    @Test
    void taskMaster_nonPositiveCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new TaskMaster(0));

        assertThrows(IllegalArgumentException.class, () ->
                new TaskMaster(-1));
    }

    /** Verifies that a failed save rolls back an added task. */
    @Test
    void addTask_saveFailure_removesTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.addTask(new TodoTask("read book")));
        assertEquals(0, taskMaster.getTaskCount());
    }

    /** Verifies that a failed save rolls back a status change. */
    @Test
    void markTaskDone_saveFailure_restoresPreviousStatus() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.markTaskDone(1));
        assertFalse(task.isDone());
    }

    /** Verifies that a failed save restores a deleted task. */
    @Test
    void deleteTask_saveFailure_restoresDeletedTask() {
        TaskMaster taskMaster = new TaskMaster(100, new FailingSaver());
        TodoTask task = new TodoTask("read book");
        taskMaster.loadTasksFromCsvStorageRecord(List.of(task));

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.deleteTask(1));
        assertEquals(1, taskMaster.getTaskCount());
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
    }

    /** Verifies that null or null-containing loaded lists are rejected. */
    @Test
    void loadTasksFromCsvStorageRecord_nullOrNullTaskList_throwsStorageException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.loadTasksFromCsvStorageRecord(null));
        assertThrows(LuckyNoStorageException.class, () ->
                taskMaster.loadTasksFromCsvStorageRecord(
                        java.util.Arrays.asList(new TodoTask("read book"), null)));
    }

    /** Verifies that searching with a null date is rejected. */
    @Test
    void findTasks_nullDate_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();

        assertThrows(IllegalArgumentException.class, () ->
                taskMaster.findTasks(null, null));
    }

    /** Creates a task master with the default test capacity. */
    private TaskMaster createTaskMaster() {
        return createTaskMaster(100);
    }

    /** Creates a task master with a temporary backing file and capacity. */
    private TaskMaster createTaskMaster(int maxTasks) {
        return new TaskMaster(
                maxTasks,
                new CsvSaver(temporaryDirectory.resolve("tasks.csv")));
    }

    /** Creates a saver that fails every save attempt. */
    private static class FailingSaver extends CsvSaver {
        FailingSaver() {
            super(Path.of("unused.csv"));
        }

        /** Always throws to simulate a persistence failure. */
        @Override
        public void save(TaskMaster taskMaster) {
            throw new LuckyNoStorageException("simulated save failure");
        }
    }
}
