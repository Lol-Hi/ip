package luckynoslacky.luckycommand;

import luckynoslacky.ResponseTone;
import luckynoslacky.luckyui.LuckyNoMessages;

/**
 * Represents a request to leave the chatbot.
 */
public class LuckyNoByeCommand extends LuckyNoCommand {
    /**
     * Creates a command that requests termination of the chat loop.
     */
    public LuckyNoByeCommand() {
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
     * Returns the neutral tone for the farewell response.
     *
     * @return neutral response tone
     */
    @Override
    public ResponseTone getResponseTone() {
        return ResponseTone.NEUTRAL;
    }

    /**
     * Indicates that this command ends the chat loop.
     *
     * @return true because the bye command requests termination
     */
    @Override
    public boolean shouldExit() {
        return true;
    }
}
