package luckynoslacky.luckycommand;

import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a request to leave the chatbot.
 */
public class LuckyNoByeCommand extends LuckyNoCommand {

    /** Creates a command that terminates the chatbot. */
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
