/**
 * Indicates that a user's command cannot be parsed or does not satisfy the
 * chatbot's input requirements.
 */
public class LuckyNoInputError extends Exception {
    /**
     * Creates an input error with a user-facing message.
     *
     * @param message explanation of the invalid input
     */
    public LuckyNoInputError(String message) {
        super(message);
    }
}
