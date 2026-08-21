import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the task storage and listing behavior of TaskMaster.
 */
class TaskMasterTest {

    @Test
    void emptyTaskListReturnsNoTasksMessage() {
        TaskMaster taskMaster = new TaskMaster();

        assertEquals("Chill lah bro got nothing yet lah!", taskMaster.listTasks());
    }

    @Test
    void addedTaskAppearsInList() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask(new TodoTask("read book"));

        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void multipleTasksAreListedInOrder() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][ ] return book",
                taskMaster.listTasks());
    }

    @Test
    void differentTaskTypesCanBeStoredTogether() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask(new TodoTask("borrow book"));
        taskMaster.addTask(new DeadlineTask("return book", "Sunday"));
        taskMaster.addTask(new EventTask("project meeting", "Mon 2pm", "4pm"));

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] borrow book\n"
                        + "2.[D][ ] return book (by: Sunday)\n"
                        + "3.[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                taskMaster.listTasks());
    }

    @Test
    void markedTaskIsShownAsDone() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.addTask(new TodoTask("return book"));
        taskMaster.markTaskDone(2);

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] read book\n"
                        + "2.[T][X] return book",
                taskMaster.listTasks());
    }

    @Test
    void unmarkedTaskIsShownAsNotDone() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask(new TodoTask("read book"));
        taskMaster.markTaskDone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void markingAlreadyDoneTaskKeepsItDone() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.markTaskDone(1);
        taskMaster.markTaskDone(1);

        assertEquals("Nah all these stuff you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    @Test
    void unmarkingAlreadyUndoneTaskKeepsItUndone() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        taskMaster.unmarkTaskUndone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void deletingMiddleTaskRemovesItAndRenumbersRemainingTasks() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new DeadlineTask("second", "Sunday"));
        taskMaster.addTask(new EventTask("third", "Mon 2pm", "4pm"));

        assertEquals("[D][ ] second (by: Sunday)", taskMaster.deleteTask(2));
        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] first\n"
                        + "2.[E][ ] third (from: Mon 2pm to: 4pm)",
                taskMaster.listTasks());
    }

    @Test
    void invalidDeletionDoesNotAlterTaskList() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.deleteTask(2));
        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void deletingTaskFreesCapacityForAnotherTask() {
        TaskMaster taskMaster = new TaskMaster(2);
        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        taskMaster.deleteTask(1);
        taskMaster.addTask(new TodoTask("third"));

        assertEquals("Nah all these stuff you need to do:\n"
                        + "1.[T][ ] second\n"
                        + "2.[T][ ] third",
                taskMaster.listTasks());
    }

    @Test
    void deletingFromEmptyTaskListIsRejected() {
        TaskMaster taskMaster = new TaskMaster();

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.deleteTask(1));
    }

    @Test
    void invalidTaskNumberCannotBeMarked() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
    }

    @Test
    void invalidTaskNumberCannotBeUnmarked() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
    }

    @Test
    void invalidStatusChangeDoesNotAlterTaskState() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask(new TodoTask("read book"));

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
        assertEquals("Nah all these stuff you need to do:\n1.[T][ ] read book",
                taskMaster.listTasks());

        taskMaster.markTaskDone(1);
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
        assertEquals("Nah all these stuff you need to do:\n1.[T][X] read book",
                taskMaster.listTasks());
    }

    @Test
    void customCapacityIsRespected() {
        TaskMaster taskMaster = new TaskMaster(2);

        taskMaster.addTask(new TodoTask("first"));
        taskMaster.addTask(new TodoTask("second"));

        assertThrows(IllegalStateException.class,
                () -> taskMaster.addTask(new TodoTask("third")));
    }

    @Test
    void nonPositiveCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(0));

        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(-1));
    }
}
