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
  the message text, and applies the user label colour.

## Test Case: DialogueBox displays chatbot messages

- Aim: Verify that chatbot messages use the green speaker label and left-side
  alignment.
- Test: `DialogueBoxTest.dialogueBox_chatbotMessage_displaysGreenLabelOnLeft`
- Expected result: The row is left-aligned, displays `LuckyNoSlacky said:`,
  preserves the message text, and applies the chatbot label colour.

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

## Test Case: Main window ignores blank input

- Aim: Verify that submitting an empty command does not add dialogue rows.
- Test: `MainWindowTest.mainWindow_blankInput_doesNotAddDialogue`
- Actions: Click `Send` without entering a command.
- Expected result: The greeting remains the only dialogue row.

## Test Case: JavaFX application starts correctly

- Aim: Verify that the production JavaFX application creates the expected
  LuckyNoSlacky window.
- Test: `LuckyNoGuiTest.luckyNoGui_startApplication_createsConfiguredWindow`
- Actions: Launch the JavaFX application through its production startup code.
- Expected result: The window title is `LuckyNoSlacky`, its scene is 400 by
  600 pixels, and its root node is present.

## Test Case: GUI resources are packaged

- Aim: Verify that the FXML layout and both avatar images are available from
  the application classpath.
- Test: `LuckyNoGuiTest.luckyNoGui_resourcePaths_areAvailable`
- Expected result: The layout, chatbot avatar, and user avatar resources are
  all found.

## Acceptance checks beyond `guiTest`

These checks are intentionally broader than the deterministic Gradle task and
may require a desktop automation capability or manual verification:

- Enter `bye` and verify that the goodbye message is visible before the window
  closes after the configured delay.
- Enter enough commands to verify that the conversation scrolls to the latest
  message.
- Resize the window and verify that message text and avatars remain readable.
- Add a task, close the GUI, relaunch it, and verify that the task persists.
