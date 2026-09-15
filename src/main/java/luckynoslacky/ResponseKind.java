package luckynoslacky;

/**
 * Identifies the structural content of a chatbot response.
 */
public enum ResponseKind {
    /** A response containing ordinary conversational text. */
    PLAIN_TEXT,
    /** A response containing one or more formatted task lines. */
    TASK_CONTENT
}
