package luckynoslacky.luckyui.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Displays a minimal JavaFX window to verify the project configuration.
 */
public final class LuckyNoGUI extends Application {
    /**
     * Creates the JavaFX smoke-test application.
     */
    public LuckyNoGUI() {
    }

    /**
     * Creates and displays the JavaFX smoke-test window.
     *
     * @param stage primary JavaFX window supplied by the runtime
     */
    @Override
    public void start(Stage stage) {
        Label greeting = new Label("Hello, LuckyNoSlacky!");
        Scene scene = new Scene(greeting, 400, 200);
        stage.setTitle("LuckyNoSlacky JavaFX Test");
        stage.setScene(scene);
        stage.show();
    }
}
