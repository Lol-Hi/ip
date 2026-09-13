package luckynoslacky.luckyui.gui;

import java.io.IOException;
import java.io.InputStream;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
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
        loadApplicationFonts();
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

    /** Loads bundled fonts before any styled JavaFX controls are created. */
    private static void loadApplicationFonts() {
        loadFont("/fonts/PatrickHand-Regular.ttf", 16.0);
        loadFont("/fonts/RobotoMono-Regular.ttf", 13.0);
    }

    /**
     * Loads one bundled font resource into the JavaFX font registry.
     *
     * @param resourcePath classpath path of the font file
     * @param size initial font size used during registration
     */
    private static void loadFont(String resourcePath, double size) {
        try (InputStream fontStream = LuckyNoGui.class
                .getResourceAsStream(resourcePath)) {
            if (fontStream == null || Font.loadFont(fontStream, size) == null) {
                throw new IllegalStateException(
                        "Unable to load font resource: " + resourcePath);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to close font resource: " + resourcePath,
                    exception);
        }
    }
}
