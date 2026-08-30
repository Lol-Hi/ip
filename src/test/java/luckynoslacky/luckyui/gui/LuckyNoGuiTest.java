package luckynoslacky.luckyui.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Tests JavaFX application startup and window configuration. */
@ExtendWith(ApplicationExtension.class)
class LuckyNoGuiTest {
    private Stage stage;

    /** Starts the production JavaFX application on a test stage. */
    @Start
    void start(Stage stage) throws IOException {
        this.stage = stage;
        new LuckyNoGui().start(stage);
    }

    /** Verifies that application startup creates the expected window. */
    @Test
    void luckyNoGui_startApplication_createsConfiguredWindow() {
        assertEquals("LuckyNoSlacky", stage.getTitle());
        assertNotNull(stage.getScene());
        assertEquals(400.0, stage.getScene().getWidth());
        assertEquals(600.0, stage.getScene().getHeight());
        assertNotNull(stage.getScene().getRoot());
    }

    /** Verifies that the FXML and avatar resources are packaged for the GUI. */
    @Test
    void luckyNoGui_resourcePaths_areAvailable() {
        assertNotNull(LuckyNoGui.class.getResource("/view/MainWindow.fxml"));
        assertNotNull(LuckyNoGui.class.getResource("/images/luckynoslacky.jpg"));
        assertNotNull(LuckyNoGui.class.getResource("/images/user.png"));
    }
}
