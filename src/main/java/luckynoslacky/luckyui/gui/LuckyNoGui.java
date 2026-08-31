package luckynoslacky.luckyui.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import luckynoslacky.LuckyNoSlacky;

/**
 * Starts the LuckyNoSlacky JavaFX interface.
 */
public final class LuckyNoGui extends Application {
    private static final double WINDOW_WIDTH = 400.0;
    private static final double WINDOW_HEIGHT = 600.0;
    private static final double MIN_WINDOW_WIDTH = 320.0;
    private static final double MIN_WINDOW_HEIGHT = 480.0;

    /**
     * Creates the JavaFX application.
     */
    public LuckyNoGui() {
    }

    /**
     * Creates and displays the main window.
     *
     * @param stage primary JavaFX window
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                LuckyNoGui.class.getResource("/view/MainWindow.fxml"));
        Parent root = loader.load();
        MainWindow controller = loader.getController();
        controller.setChatbot(new LuckyNoSlacky());

        stage.setTitle("LuckyNoSlacky");
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.show();
    }
}
