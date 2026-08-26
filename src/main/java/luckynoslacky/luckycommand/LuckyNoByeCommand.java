package luckynoslacky.luckycommand;

import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a request to leave the chatbot.
 */
public class LuckyNoByeCommand extends LuckyNoCommand {

    /** Creates a command that ends the chatbot session. */
    public LuckyNoByeCommand() {
        super(CommandType.BYE);
    }

    /**
     * Returns the chatbot's goodbye message.
     *
     * @return goodbye message
     */
    @Override
    public String execute() {
        return LuckyNoMessages.goodbye();
    }

    /**
     * Indicates that this command ends the chat loop.
     *
     * @return true because the bye command requests termination
     */
    @Override
    public boolean requestsExit() {
        return true;
    }
}
