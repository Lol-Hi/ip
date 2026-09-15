# LuckyNoSlacky GUI test plan

## Test execution information

- Framework: TestFX with JUnit 5
- Java version: Java 25
- Automated test command: `./gradlew guiTest`
- Comparison: assertions on observable JavaFX state and displayed controls
- Failure policy: stop at the first failed GUI test

## Test Case: DialogueBox displays user messages

- Aim: Verify that user messages use the blue speaker label and right-side
  alignment.
- Test: `DialogueBoxTest.dialogueBox_userMessage_displaysBlueLabelOnRight`
- Expected result: The row is right-aligned, displays `You said:`, preserves
  the message text, and applies the user dialogue CSS style.

## Test Case: DialogueBox displays chatbot messages

- Aim: Verify that chatbot messages use the green speaker label and left-side
  alignment.
- Test: `DialogueBoxTest.dialogueBox_chatbotMessage_displaysGreenLabelOnLeft`
- Expected result: The row is left-aligned, displays `LuckyNoSlacky said:`,
  preserves the message text, and applies the chatbot dialogue CSS style.

## Test Case: Main window displays a conversation

- Aim: Verify that entering a command creates both a user dialogue row and a
  chatbot response row.
- Test: `MainWindowTest.mainWindow_unknownCommand_displaysBothSpeakerMessages`
- Actions: Enter `unknown` in the command field and submit it.
- Expected result: The conversation contains the greeting, a `You said:` row,
  and a `LuckyNoSlacky said:` row.

## Test Case: Main window remains usable after a normal command

- Aim: Verify that a non-exit command clears the input field without disabling
  it.
- Test: `MainWindowTest.mainWindow_unknownCommand_keepsInputEnabled`
- Actions: Enter `unknown` in the command field and submit it.
- Expected result: The input field is empty and remains enabled.

## Test Case: Main window submits with the Send button

- Aim: Verify that clicking the Send button submits a command through the same
  handler as pressing Enter.
- Test: `MainWindowTest.mainWindow_sendButton_submitsCommand`
- Actions: Enter `unknown` and click `Send`.
- Expected result: A user dialogue row and chatbot response row are added.

## Test Case: Main window scrolls to the latest dialogue

- Aim: Verify that a long conversation automatically reveals its newest
  dialogue.
- Test: `MainWindowTest.mainWindow_manyMessages_scrollsToLatestDialogue`
- Actions: Submit 20 deterministic commands through the input field.
- Expected result: All dialogue rows are retained and the scroll pane is at
  its maximum vertical position.

## Test Case: Main window ignores blank input

- Aim: Verify that submitting an empty command does not add dialogue rows.
- Test: `MainWindowTest.mainWindow_blankInput_doesNotAddDialogue`
- Actions: Click `Send` without entering a command.
- Expected result: The greeting remains the only dialogue row.

## Test Case: Main window remains usable after a load failure

- Aim: Verify that the GUI enters degraded empty-list mode when task data
  cannot be loaded.
- Test: `MainWindowTest.mainWindow_loadFailure_remainsInteractive`
- Actions: Launch the main window with a chatbot reporting a startup load
  failure, then enter `list` and submit it.
- Expected result: The greeting and loading-error message are displayed once,
  the input field and Send button remain enabled, and the `list` command adds
  the normal user and chatbot dialogue rows.

## Test Case: Main window remains usable after a capacity error

- Aim: Verify that rejecting a task because the task list is full does not
  disable the GUI.
- Test: `MainWindowTest.mainWindow_taskLimit_keepsControlsEnabled`
- Actions: Launch the main window with a full-list chatbot, then submit
  `todo overflow`.
- Expected result: The approved task-limit message is displayed, the rejected
  task is not added, and the input field and Send button remain enabled.

## Test Case: Main window displays goodbye before delayed exit

- Aim: Verify that the goodbye message remains visible while the application
  waits before exiting.
- Test: `MainWindowTest.mainWindow_byeCommand_displaysGoodbyeBeforeDelayedExit`
- Actions: Enter `bye` in the command field and submit it.
- Expected result: The goodbye message is displayed, both the input field and
  Send button are disabled, the window remains visible, and the exit callback
  runs only after the configured 1.5-second delay.

## Test Case: JavaFX application starts correctly

- Aim: Verify that the production JavaFX application creates the expected
  LuckyNoSlacky window.
- Test: `LuckyNoGuiTest.luckyNoGui_startApplication_createsConfiguredWindow`
- Actions: Launch the JavaFX application through its production startup code.
- Expected result: The window title is `LuckyNoSlacky`, its scene is 400 by
  600 pixels, and its root node is present.

## Test Case: GUI resources are packaged

- Aim: Verify that the FXML layout, CSS stylesheets, and both avatar images are
  available from the application classpath.
- Test: `LuckyNoGuiTest.luckyNoGui_resourcePaths_areAvailable`
- Expected result: The layout, both CSS stylesheets, chatbot avatar, and user
  avatar resources are all found.

## Test Case: Main window preserves usable minimum dimensions

- Aim: Verify that the application prevents the window from becoming too small
  for the conversation and input controls.
- Test: `LuckyNoGuiTest.luckyNoGui_startApplication_createsConfiguredWindow`
- Actions: Inspect the primary stage minimum width and height.
- Expected result: The minimum dimensions are 320 by 480 pixels.

## Test Case: Main window provides responsive input controls

- Aim: Verify that the input field and Send button are placed in a responsive
  bottom control row.
- Test: `MainWindowTest.mainWindow_widenedWindow_expandsInputAndPreservesButtonWidth`
- Actions: Resize the window from 400 by 600 to 800 by 600.
- Expected result: The input field expands with the window, while the Send
  button remains at the bottom-right with a stable width.

## Test Case: Main window keeps controls usable at minimum size

- Aim: Verify that the input controls remain inside the scene at the minimum
  supported dimensions.
- Test: `MainWindowTest.mainWindow_minimumWindow_keepsControlsWithinScene`
- Actions: Resize the window to 320 by 480.
- Expected result: The input field and Send button remain visible, usable, and
  inside the scene bounds.

## Test Case: Main window preserves tasks across relaunch

- Aim: Verify that a task added through the GUI is restored after closing and
  relaunching the application.
- Test: `GuiPersistenceTest.luckyNoGui_taskSavedThenRelaunched_restoresTask`
- Actions: Add `todo persisted gui task`, close the stage, relaunch the GUI
  with the same isolated data file, then submit `list`.
- Expected result: The task appears in the relaunched conversation.

## Acceptance checks beyond `guiTest`

These checks are intentionally broader than the deterministic Gradle task and
may require a desktop automation capability or manual verification:

- Enter enough commands to verify that the conversation scrolls to the latest
  message.
- Resize the window and verify that message text and avatars remain readable.
- Add a task, close the GUI, relaunch it, and verify that the task persists.
