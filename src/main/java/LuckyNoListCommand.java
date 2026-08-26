/**
 * Represents a request to list all stored tasks.
 */
public class LuckyNoListCommand extends LuckyNoCommand {

    private final TaskMaster taskMaster;

    LuckyNoListCommand(TaskMaster taskMaster) {
        super(CommandType.LIST);
        this.taskMaster = taskMaster;
    }

    @Override
    public String execute() {
        return taskMaster.listTasks();
    }
}
