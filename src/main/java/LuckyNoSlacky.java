/**
 * Starts the LuckyNoSlacky chatbot.
 */
public class LuckyNoSlacky {
    private static final String DIVIDER = "\n____________________________________________________________\n";

    private void greet() {
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
    }

    private void exit() {
        String goodbye = "  Bye, hope to see you again soon!";
        System.out.print(DIVIDER + goodbye + DIVIDER);
    }

    public static void main(String[] args) {
        LuckyNoSlacky lucky = new LuckyNoSlacky();
        lucky.greet();
        lucky.exit();
    }
}
