package luckynoslacky.luckyui.gui;

import javafx.application.Application;

/**
 * Launches the minimal JavaFX configuration smoke test.
 */
public final class LuckyNoLauncher {
    private LuckyNoLauncher() {
    }

    /**
     * Starts the JavaFX application.
     *
     * @param args command-line arguments passed to JavaFX
     */
    public static void main(String[] args) {
        Application.launch(LuckyNoGUI.class, args);
    }
}
