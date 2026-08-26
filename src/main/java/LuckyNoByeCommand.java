/**
 * Represents a request to leave the chatbot.
 */
public class LuckyNoByeCommand extends LuckyNoCommand {

    public LuckyNoByeCommand() {
        super(CommandType.BYE);
    }

    @Override
    public String execute() {
        return LuckyNoMessages.goodbye();
    }

    @Override
    public boolean requestsExit() {
        return true;
    }
}
