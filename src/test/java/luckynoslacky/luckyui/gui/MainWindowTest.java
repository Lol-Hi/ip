package luckynoslacky.luckyui.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import luckynoslacky.LuckyNoSlacky;
import luckynoslacky.luckyui.LuckyNoMessages;

/** Tests observable interactions in the main JavaFX window. */
@ExtendWith(ApplicationExtension.class)
class MainWindowTest {
    private static final int SCROLL_COMMAND_COUNT = 20;
    private Stage stage;

    /** Loads the production FXML window before each test. */
    @Start
    void start(Stage stage) throws IOException {
        this.stage = stage;
        loadWindow(new LuckyNoSlacky());
    }

    /** Verifies that the window displays a user input and chatbot reply. */
    @Test
    void mainWindow_unknownCommand_displaysUserAndWarningMessages(FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("unknown").push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        assertEquals(3, dialogueContainer.getChildren().size());
        DialogueBox userDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(1);
        DialogueBox chatbotDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(2);
        assertEquals("unknown", getMessageText(userDialogue, 0));
        assertEquals(
                LuckyNoMessages.unknownCommandMessage(),
                getMessageText(chatbotDialogue, 1));
        assertTrue(userDialogue.getStyleClass().contains(
                DialogueBox.USER_DIALOGUE_STYLE));
        assertTrue(chatbotDialogue.getStyleClass().contains(
                DialogueBox.WARNING_DIALOGUE_STYLE));
    }

    /** Verifies that the input control remains enabled after a non-exit command. */
    @Test
    void mainWindow_unknownCommand_keepsInputEnabled(FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();
        robot.clickOn(inputField).write("unknown").push(KeyCode.ENTER);

        assertFalse(inputField.isDisabled());
        assertTrue(inputField.getText().isEmpty());
    }

    /** Verifies that clicking Send submits the command like pressing Enter. */
    @Test
    void mainWindow_sendButton_submitsCommand(FxRobot robot) {
        robot.clickOn("#userInput")
                .write("unknown")
                .clickOn("#sendButton");

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        assertEquals(3, dialogueContainer.getChildren().size());
    }

    /** Verifies that the main controls expose the drafted accessible labels. */
    @Test
    void mainWindow_controls_exposeAccessibleLabels() {
        TextField inputField = lookupTextField();
        Button sendButton = lookupButton();
        ScrollPane conversationHistory = lookupScrollPane();

        assertEquals(
                "Lai tell me what you want me to do, then click Send",
                inputField.getAccessibleText());
        assertEquals(
                "You can also press Enter to send the command without using the mouse.",
                inputField.getAccessibleHelp());
        assertEquals("Send command", sendButton.getAccessibleText());
        assertEquals(
                "Sends the command currently entered in the command field.",
                sendButton.getAccessibleHelp());
        assertEquals(
                "Here you see the whole chat history! Just use your arrow "
                        + "or page keys to scroll up scroll down can liao",
                conversationHistory.getAccessibleText());
    }

    /** Verifies that the command field receives focus when the window opens. */
    @Test
    void mainWindow_startApplication_focusesCommandInput(FxRobot robot) {
        robot.interact(() -> assertTrue(lookupTextField().isFocused()));
    }

    /** Verifies that Tab follows the specified forward focus order. */
    @Test
    void mainWindow_tabTraversal_movesForwardThroughControls(FxRobot robot) {
        TextField inputField = lookupTextField();
        Button sendButton = lookupButton();
        ScrollPane conversationHistory = lookupScrollPane();

        robot.clickOn(inputField).push(KeyCode.TAB);
        assertTrue(sendButton.isFocused());

        robot.push(KeyCode.TAB);
        assertTrue(conversationHistory.isFocused());

        robot.push(KeyCode.TAB);
        assertTrue(inputField.isFocused());
    }

    /** Verifies that Shift+Tab follows the specified reverse focus order. */
    @Test
    void mainWindow_tabTraversal_movesBackwardThroughControls(FxRobot robot) {
        TextField inputField = lookupTextField();
        Button sendButton = lookupButton();
        ScrollPane conversationHistory = lookupScrollPane();

        robot.clickOn(inputField).push(KeyCode.TAB);
        robot.push(KeyCode.TAB);
        assertTrue(conversationHistory.isFocused());

        robot.press(KeyCode.SHIFT).push(KeyCode.TAB).release(KeyCode.SHIFT);
        assertTrue(sendButton.isFocused());

        robot.press(KeyCode.SHIFT).push(KeyCode.TAB).release(KeyCode.SHIFT);
        assertTrue(inputField.isFocused());
    }

    /** Verifies that a Send-button submission restores focus to the input. */
    @Test
    void mainWindow_sendButtonSubmission_returnsFocusToCommandInput(FxRobot robot) {
        robot.clickOn("#userInput")
                .write("unknown")
                .clickOn("#sendButton");

        assertTrue(lookupTextField().isFocused());
    }

    /** Verifies that submitting blank input leaves the conversation unchanged. */
    @Test
    void mainWindow_blankInput_doesNotAddDialogue(FxRobot robot) {
        VBox dialogueContainer = robot.lookup("#dialogContainer").query();

        robot.clickOn("#sendButton");

        assertEquals(1, dialogueContainer.getChildren().size());
    }

    /** Verifies that a parser error is displayed as a chatbot message. */
    @Test
    void mainWindow_parseErrorResponse_displaysExactReply(FxRobot robot) {
        StubChatbot chatbot = new StubChatbot(
                false,
                new LuckyNoSlacky.ChatResponse("exact parse error", false));
        showWindowWithChatbot(robot, chatbot);

        robot.clickOn("#userInput").write("trigger parse error")
                .push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);
        assertEquals("exact parse error", getMessageText(chatbotDialogue, 1));
        assertFalse(robot.lookup("#userInput").query().isDisabled());
    }

    /** Verifies that a save error is displayed as a chatbot message. */
    @Test
    void mainWindow_saveErrorResponse_displaysExactReply(FxRobot robot) {
        StubChatbot chatbot = new StubChatbot(
                false,
                new LuckyNoSlacky.ChatResponse(
                        LuckyNoMessages.saveErrorMessage(), false));
        showWindowWithChatbot(robot, chatbot);

        robot.clickOn("#userInput").write("trigger save error")
                .push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);
        assertEquals(LuckyNoMessages.saveErrorMessage(),
                getMessageText(chatbotDialogue, 1));
        assertFalse(robot.lookup("#userInput").query().isDisabled());
    }

    /** Verifies that a startup load error is displayed after the greeting. */
    @Test
    void mainWindow_loadErrorChatbot_displaysExactStartupError(FxRobot robot) {
        showWindowWithChatbot(robot, new StubChatbot(
                true,
                new LuckyNoSlacky.ChatResponse("unused response", false)));

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue = (DialogueBox) dialogueContainer.getChildren()
                .get(dialogueContainer.getChildren().size() - 1);
        assertEquals(LuckyNoMessages.loadErrorMessage(),
                getMessageText(chatbotDialogue, 1));
    }

    /** Verifies that the layout exposes stable visual integration regions. */
    @Test
    void mainWindow_layoutRegions_exposeStableIntegrationHooks(FxRobot robot) {
        VBox brandHeaderSlot = robot.lookup("#brandHeaderSlot").query();
        ScrollPane conversationRegion = robot.lookup("#scrollPane").query();
        HBox commandRow = robot.lookup("#commandRow").query();

        assertTrue(brandHeaderSlot.getStyleClass().contains(
                "brand-header-slot"));
        assertTrue(brandHeaderSlot.isManaged());
        assertTrue(brandHeaderSlot.isVisible());
        assertNotNull(robot.lookup("#brandTitle").query());
        assertTrue(conversationRegion.getStyleClass().contains(
                "conversation-region"));
        StackPane background = robot.lookup("#personalityBackground").query();
        assertTrue(background.getStyleClass().contains("personality-background"));
        assertTrue(background.getMinHeight()
                >= conversationRegion.getViewportBounds().getHeight());
        assertTrue(lookupTextField().getStyleClass().contains(
                "personality-input-field"));
        assertTrue(lookupButton().getStyleClass().contains(
                "personality-send-button"));
        assertTrue(commandRow.getStyleClass().contains("command-row"));
    }

    /** Verifies that a load failure still leaves the GUI usable. */
    @Test
    void mainWindow_loadFailure_remainsInteractive(FxRobot robot) {
        showWindowWithChatbot(robot, new LoadFailingChatbot());

        TextField inputField = robot.lookup("#userInput").query();
        Button sendButton = robot.lookup("#sendButton").query();
        VBox dialogueContainer = robot.lookup("#dialogContainer").query();

        assertFalse(inputField.isDisabled());
        assertFalse(sendButton.isDisabled());
        assertEquals(2, dialogueContainer.getChildren().size());

        robot.clickOn(inputField).write("list").push(KeyCode.ENTER);

        assertEquals(4, dialogueContainer.getChildren().size());
    }

    /** Verifies that a capacity error remains recoverable in the GUI. */
    @Test
    void mainWindow_taskLimit_keepsControlsEnabled(FxRobot robot) {
        showWindowWithChatbot(robot, new CapacityFullChatbot());

        TextField inputField = robot.lookup("#userInput").query();
        Button sendButton = robot.lookup("#sendButton").query();
        VBox dialogueContainer = robot.lookup("#dialogContainer").query();

        robot.clickOn(inputField).write("todo overflow").push(KeyCode.ENTER);

        assertFalse(inputField.isDisabled());
        assertFalse(sendButton.isDisabled());
        assertEquals(3, dialogueContainer.getChildren().size());
        DialogueBox chatbotDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(2);
        assertEquals(
                LuckyNoMessages.taskLimitMessage(),
                getMessageText(chatbotDialogue, 1));
    }

    /** Verifies that goodbye is visible before the delayed exit is triggered. */
    @Test
    void mainWindow_byeCommand_displaysGoodbyeBeforeDelayedExit(
            FxRobot robot) {
        RecordingExitScheduler scheduler = new RecordingExitScheduler();
        AtomicBoolean exitTriggered = new AtomicBoolean();
        Runnable exitAction = () -> exitTriggered.set(true);
        showWindowWithChatbot(robot, new GoodbyeChatbot(), scheduler, exitAction);

        robot.clickOn("#userInput").write("bye").push(KeyCode.ENTER);

        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        DialogueBox chatbotDialogue =
                (DialogueBox) dialogueContainer.getChildren().get(2);
        TextField inputField = robot.lookup("#userInput").query();
        Button sendButton = robot.lookup("#sendButton").query();
        assertEquals(3, dialogueContainer.getChildren().size());
        assertEquals(
                LuckyNoMessages.goodbye(),
                getMessageText(chatbotDialogue, 1));
        assertTrue(stage.isShowing());
        assertTrue(inputField.isDisabled());
        assertTrue(sendButton.isDisabled());
        assertEquals(1, scheduler.scheduleCount);
        assertEquals(1500.0, scheduler.delay.toMillis());
        assertFalse(exitTriggered.get());

        robot.interact(scheduler::trigger);
        assertTrue(exitTriggered.get());
    }

    /** Verifies that exit dependencies cannot be missing. */
    @Test
    void construct_nullExitDependencies_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MainWindow(null, () -> { }));
        assertThrows(IllegalArgumentException.class, () ->
                new MainWindow((delay, action) -> { }, null));
    }

    /** Verifies that a long conversation scrolls to its newest message. */
    @Test
    void mainWindow_manyMessages_scrollsToLatestDialogue(FxRobot robot) {
        showWindowWithChatbot(robot, new ScrollingChatbot());
        TextField inputField = robot.lookup("#userInput").query();

        for (int commandNumber = 0;
             commandNumber < SCROLL_COMMAND_COUNT;
             commandNumber++) {
            robot.clickOn(inputField).write("scroll").push(KeyCode.ENTER);
        }
        WaitForAsyncUtils.waitForFxEvents();

        ScrollPane scrollPane = robot.lookup("#scrollPane").query();
        VBox dialogueContainer = robot.lookup("#dialogContainer").query();
        assertEquals(1 + 2 * SCROLL_COMMAND_COUNT,
                dialogueContainer.getChildren().size());
        assertTrue(scrollPane.getVvalue()
                >= scrollPane.getVmax() - 0.01);
    }

    /** Verifies that widening the window expands input while preserving the button. */
    @Test
    void mainWindow_widenedWindow_expandsInputAndPreservesButtonWidth(
            FxRobot robot) {
        TextField inputField = robot.lookup("#userInput").query();
        Button sendButton = robot.lookup("#sendButton").query();

        robot.interact(() -> {
            stage.setWidth(400.0);
            stage.setHeight(600.0);
        });
        WaitForAsyncUtils.waitForFxEvents();
        double initialInputWidth = inputField.getWidth();
        double initialButtonWidth = sendButton.getWidth();

        robot.interact(() -> stage.setWidth(800.0));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(inputField.getWidth() > initialInputWidth + 100.0);
        assertEquals(initialButtonWidth, sendButton.getWidth(), 2.0);
        assertNodeWithinScene(robot.lookup("#commandRow").query(),
                stage.getScene());
    }

    /** Verifies that controls remain inside the scene at minimum dimensions. */
    @Test
    void mainWindow_minimumWindow_keepsControlsWithinScene(FxRobot robot) {
        robot.interact(() -> {
            stage.setWidth(320.0);
            stage.setHeight(480.0);
        });
        WaitForAsyncUtils.waitForFxEvents();

        HBox inputRow = robot.lookup("#commandRow").query();
        TextField inputField = robot.lookup("#userInput").query();
        Button sendButton = robot.lookup("#sendButton").query();
        Scene scene = stage.getScene();
        assertNodeWithinScene(inputRow, scene);
        assertNodeWithinScene(inputField, scene);
        assertNodeWithinScene(sendButton, scene);
        assertTrue(inputField.getWidth() > 0.0);
        assertTrue(sendButton.getWidth() > 0.0);
    }

    /** Verifies that the main window rejects a missing chatbot dependency. */
    @Test
    void setChatbot_nullChatbot_throwsIllegalArgumentException() {
        MainWindow controller = new MainWindow();

        assertThrows(IllegalArgumentException.class, () ->
                controller.setChatbot(null));
    }

    /** Returns the message text from a dialogue row's text container. */
    private String getMessageText(DialogueBox dialogue, int dialogueIndex) {
        VBox messageContainer = (VBox) dialogue.getChildren().get(dialogueIndex);
        return messageContainer.getChildren().stream()
                .map(this::findVisibleMessageText)
                .filter(text -> !text.isEmpty())
                .findFirst()
                .orElseThrow();
    }

    /** Finds the first visible response text while skipping tone markers. */
    private String findVisibleMessageText(Node node) {
        if (node instanceof Label label) {
            return switch (label.getText()) {
                case "🍀", "⚠", "⛔" -> "";
                default -> label.getText();
            };
        }
        if (node instanceof javafx.scene.Parent parent) {
            return parent.getChildrenUnmodifiable().stream()
                    .map(this::findVisibleMessageText)
                    .filter(text -> !text.isEmpty())
                    .findFirst()
                    .orElse("");
        }
        return "";
    }

    /** Returns the command input from the production FXML scene. */
    private TextField lookupTextField() {
        return (TextField) stage.getScene().lookup("#userInput");
    }

    /** Returns the Send button from the production FXML scene. */
    private Button lookupButton() {
        return (Button) stage.getScene().lookup("#sendButton");
    }

    /** Returns the conversation history from the production FXML scene. */
    private ScrollPane lookupScrollPane() {
        return (ScrollPane) stage.getScene().lookup("#scrollPane");
    }

    /** Replaces the current window content with a supplied test chatbot. */
    private void showWindowWithChatbot(FxRobot robot, LuckyNoSlacky chatbot) {
        robot.interact(() -> {
            try {
                loadWindow(chatbot);
            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Unable to load the test window.", exception);
            }
        });
    }

    /** Replaces the current window with a chatbot and test exit behavior. */
    private void showWindowWithChatbot(
            FxRobot robot,
            LuckyNoSlacky chatbot,
            MainWindow.ExitScheduler exitScheduler,
            Runnable exitAction) {
        robot.interact(() -> {
            try {
                loadWindow(chatbot, new MainWindow(exitScheduler, exitAction));
            } catch (IOException exception) {
                throw new IllegalStateException(
                        "Unable to load the test window.", exception);
            }
        });
    }

    /** Loads the production window with the supplied chatbot. */
    private void loadWindow(LuckyNoSlacky chatbot) throws IOException {
        loadWindow(chatbot, new MainWindow());
    }

    /** Loads the production window with a supplied controller. */
    private void loadWindow(
            LuckyNoSlacky chatbot,
            MainWindow controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainWindow.class.getResource("/view/MainWindow.fxml"));
        loader.setControllerFactory(type -> {
            if (type == MainWindow.class) {
                return controller;
            }
            throw new IllegalArgumentException(
                    "Unexpected FXML controller type.");
        });
        Parent root = loader.load();
        controller.setChatbot(chatbot);
        stage.setScene(new Scene(root, 400.0, 600.0));
        stage.show();
    }

    /** Verifies that a node's rendered bounds remain inside a scene. */
    private static void assertNodeWithinScene(Node node, Scene scene) {
        Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(nodeBounds.getMinX() >= -1.0);
        assertTrue(nodeBounds.getMinY() >= -1.0);
        assertTrue(nodeBounds.getMaxX() <= scene.getWidth() + 1.0);
        assertTrue(nodeBounds.getMaxY() <= scene.getHeight() + 1.0);
    }

    /** Simulates a chatbot whose task data could not be loaded. */
    private static final class LoadFailingChatbot extends LuckyNoSlacky {
        @Override
        public boolean hasLoadError() {
            return true;
        }
    }

    /** Simulates a full task list for a capacity-error GUI test. */
    private static final class CapacityFullChatbot extends LuckyNoSlacky {
        @Override
        public boolean hasLoadError() {
            return false;
        }

        @Override
        public ChatResponse getResponse(String userInput) {
            return new ChatResponse(
                    LuckyNoMessages.taskLimitMessage(), false);
        }
    }

    /** Simulates a chatbot that returns short responses for scroll testing. */
    private static final class ScrollingChatbot extends LuckyNoSlacky {
        /** Keeps the test window free of startup warnings. */
        @Override
        public boolean hasLoadError() {
            return false;
        }

        /** Returns a non-exit response for every scrolling command. */
        @Override
        public ChatResponse getResponse(String userInput) {
            return new ChatResponse("scroll response", false);
        }
    }

    /** Simulates a chatbot response that requests application exit. */
    private static final class GoodbyeChatbot extends LuckyNoSlacky {
        /** Keeps the test window free of startup warnings. */
        @Override
        public boolean hasLoadError() {
            return false;
        }

        /** Returns the production goodbye response for every command. */
        @Override
        public ChatResponse getResponse(String userInput) {
            return new ChatResponse(LuckyNoMessages.goodbye(), true);
        }
    }

    /** Supplies deterministic chatbot responses to GUI presentation tests. */
    private static final class StubChatbot extends LuckyNoSlacky {
        private final boolean loadError;
        private final ChatResponse response;

        StubChatbot(boolean loadError, ChatResponse response) {
            super();
            this.loadError = loadError;
            this.response = response;
        }

        /** Returns the configured startup-load result. */
        @Override
        public boolean hasLoadError() {
            return loadError;
        }

        /** Returns the configured response for any submitted command. */
        @Override
        public ChatResponse getResponse(String userInput) {
            return response;
        }
    }

    /** Records delayed exit scheduling so tests can trigger it deterministically. */
    private static final class RecordingExitScheduler
            implements MainWindow.ExitScheduler {
        private Duration delay;
        private Runnable exitAction;
        private int scheduleCount;

        /** Records a scheduled exit action and its delay. */
        @Override
        public void schedule(Duration delay, Runnable exitAction) {
            this.delay = delay;
            this.exitAction = exitAction;
            scheduleCount++;
        }

        /** Runs the recorded action to simulate the completed delay. */
        private void trigger() {
            exitAction.run();
        }
    }
}
