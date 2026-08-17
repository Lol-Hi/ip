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

        assertEquals("1. read book", taskMaster.listTasks());
    }

    @Test
    void multipleTasksAreListedInOrder() {
        TaskMaster taskMaster = new TaskMaster();

        taskMaster.addTask("read book");
        taskMaster.addTask("return book");

        assertEquals("1. read book\n2. return book", taskMaster.listTasks());
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
