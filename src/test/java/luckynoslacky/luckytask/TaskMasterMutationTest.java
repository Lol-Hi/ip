package luckynoslacky.luckytask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import luckynoslacky.luckystorage.CsvSaver;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests status changes and deletion behavior provided by {@link TaskMaster}. */
class TaskMasterMutationTest {
    private static final LocalDateTime DEADLINE =
            LocalDateTime.of(2026, 12, 6, 23, 59);
    private static final LocalDateTime EVENT_START =
            LocalDateTime.of(2026, 8, 6, 14, 0);
    private static final LocalDateTime EVENT_END =
            LocalDateTime.of(2026, 8, 6, 16, 0);

    @TempDir
    Path temporaryDirectory;

    /** Verifies that marking a valid task updates its status. */
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

        assertThrows(IllegalArgumentException.class, () -> taskMaster.deleteTask(2));
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

        assertThrows(IllegalArgumentException.class, () -> taskMaster.deleteTask(1));
    }

    /** Verifies that invalid numbers are rejected when marking tasks. */
    @Test
    void markTaskDone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () -> taskMaster.markTaskDone(0));
        assertThrows(IllegalArgumentException.class, () -> taskMaster.markTaskDone(2));
    }

    /** Verifies that invalid numbers are rejected when unmarking tasks. */
    @Test
    void unmarkTaskUndone_invalidTaskNumber_throwsIllegalArgumentException() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () -> taskMaster.unmarkTaskUndone(0));
        assertThrows(IllegalArgumentException.class, () -> taskMaster.unmarkTaskUndone(2));
    }

    /** Verifies invalid status changes do not alter another task's state. */
    @Test
    void markOrUnmark_invalidTaskNumber_preservesTaskState() {
        TaskMaster taskMaster = createTaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class, () -> taskMaster.markTaskDone(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][ ] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));

        taskMaster.markTaskDone(1);
        assertThrows(IllegalArgumentException.class, () -> taskMaster.unmarkTaskUndone(2));
        assertEquals("Nah, all these things you need to do:\n1.[T][X] read book",
                LuckyNoMessages.listTasksMessage(taskMaster.listTasks()));
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
}
