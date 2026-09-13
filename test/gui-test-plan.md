# LuckyNoSlacky GUI test plan

## Test execution information

- Framework: TestFX with JUnit 5
- Java version: Java 25
- Automated test command: `./gradlew guiTest`
- Comparison: assertions on observable JavaFX state and displayed controls
- Failure policy: stop at the first failed GUI test

## Test Case: DialogueBox displays user messages

- Aim: Verify that user messages use a compact right-aligned bubble and
  circular user avatar.
- Test: `DialogueBoxTest.dialogueBox_userMessage_displaysCircularAvatarOnRight`
- Expected result: The row is right-aligned, displays only the raw message
  text, applies the user dialogue CSS style, and clips the avatar circularly.

## Test Case: DialogueBox displays chatbot messages

- Aim: Verify that application messages use a wider left-aligned bubble.
- Test: `DialogueBoxTest.dialogueBox_chatbotMessage_displaysBubbleOnLeft`
- Expected result: The row is left-aligned, preserves the application message
  text, and applies the chatbot dialogue CSS style.

## Test Case: DialogueBox displays warning messages

- Aim: Verify that application warnings use a distinct visual style.
- Test: `DialogueBoxTest.dialogueBox_warningMessage_appliesWarningStyle`
- Expected result: The warning is left-aligned and applies the warning
  dialogue CSS style.

## Test Case: Main window displays a conversation

- Aim: Verify that entering an unrecognised command creates a user row and a
  warning response row.
- Test: `MainWindowTest.mainWindow_unknownCommand_displaysUserAndWarningMessages`
- Actions: Enter `unknown` in the command field and submit it.
- Expected result: The conversation contains the greeting, a right-aligned
  user row containing `unknown`, and a left-aligned warning row containing the
  actual unknown-command response. The user row does not contain `You said:`.

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
- Test: Manual acceptance check
- Actions: Resize the window horizontally and vertically.
- Expected result: The input field expands with the window, the Send button
  remains at the bottom-right with a stable width, and message bubbles wrap
  without clipping or horizontal scrolling.

## Test Case: Sample task conversation uses actual chatbot responses

- Aim: Verify that the GUI presents real LuckyNoSlacky task-management
  responses rather than invented assistant copy.
- Test: Manual acceptance check
- Actions: Enter the following commands in order:

  ```text
  todo buy groceries
  deadline submit report /by 20 Sep 2026 12pm
  list
  snooze 1 /by 2 days
  ```

- Expected result: The GUI displays the actual task confirmations, task list,
  and the warning `Eh mr blur sotong this task don't even have time for you to
  snooze la`. It does not display `Got it — I'll help you stay on track` or
  `You said:`.

## Acceptance checks beyond `guiTest`

These checks are intentionally broader than the deterministic Gradle task and
may require a desktop automation capability or manual verification:

- Enter `bye` and verify that the goodbye message is visible before the window
  closes after the configured delay.
- Enter enough commands to verify that the conversation scrolls to the latest
  message.
- Resize the window and verify that message text and avatars remain readable.
- Verify that both existing avatar assets are displayed as circular images.
- Add a task, close the GUI, relaunch it, and verify that the task persists.
