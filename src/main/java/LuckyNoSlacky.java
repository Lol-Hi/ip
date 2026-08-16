/**
 * Starts the LuckyNoSlacky chatbot.
 */
public class LuckyNoSlacky {
    private static final String DIVIDER = "\n____________________________________________________________\n";

    public static void main(String[] args) {
        String banner = "     .--\"\"\"\"\"--.\n"
                + "   /  /^\\   /^\\  \\\n"
                + "  |  .---------.  |\n"
                + "  |  | | | | | |  |\n"
                + "   \\ '---------' /\n"
                + "     '-._____.-'\n"
                + "    [NO SLACKING]\n"
                + " LuckyNoSlacky is here to help!";
        String hello = "  Hello, I'm LuckyNoSlacky!\n  What can I do for you?";
        System.out.print(DIVIDER + banner + DIVIDER + hello + DIVIDER);

        String goodbye = "  Bye, hope to see you again soon!";
        System.out.print(DIVIDER + goodbye + DIVIDER);
    }
}
