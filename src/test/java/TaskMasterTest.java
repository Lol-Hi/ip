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

        assertEquals("No tasks yet.", taskMaster.listTasks());
    }

    @Test
    void addedTaskAppearsInList() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask("read book");

        assertEquals("Here are the tasks in your list:\n1.[ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void multipleTasksAreListedInOrder() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask("read book");
        taskMaster.addTask("return book");

        assertEquals("Here are the tasks in your list:\n"
                        + "1.[ ] read book\n"
                        + "2.[ ] return book",
                taskMaster.listTasks());
    }

    @Test
    void markedTaskIsShownAsDone() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask("read book");
        taskMaster.addTask("return book");
        taskMaster.markTaskDone(2);

        assertEquals("Here are the tasks in your list:\n"
                        + "1.[ ] read book\n"
                        + "2.[X] return book",
                taskMaster.listTasks());
    }

    @Test
    void unmarkedTaskIsShownAsNotDone() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask("read book");
        taskMaster.markTaskDone(1);
        taskMaster.unmarkTaskUndone(1);

        assertEquals("Here are the tasks in your list:\n1.[ ] read book",
                taskMaster.listTasks());
    }

    @Test
    void invalidTaskNumberCannotBeMarked() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask("read book");

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.markTaskDone(2));
    }

    @Test
    void invalidTaskNumberCannotBeUnmarked() {
        TaskMaster taskMaster = new TaskMaster();
        taskMaster.addTask("read book");

        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(0));
        assertThrows(IllegalArgumentException.class,
                () -> taskMaster.unmarkTaskUndone(2));
    }

    @Test
    void customCapacityIsRespected() {
        TaskMaster taskMaster = new TaskMaster(2);

        taskMaster.addTask("first");
        taskMaster.addTask("second");

        assertThrows(IllegalStateException.class,
                () -> taskMaster.addTask("third"));
    }

    @Test
    void nonPositiveCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(0));

        assertThrows(IllegalArgumentException.class,
                () -> new TaskMaster(-1));
    }
}
