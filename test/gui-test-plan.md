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
  text, applies the user dialogue CSS style, clips the avatar circularly, and
  exposes the accessible text `You: <message>` without making the avatar
  keyboard-focusable.

## Test Case: DialogueBox displays chatbot messages

- Aim: Verify that application messages use a wider left-aligned bubble.
- Test: `DialogueBoxTest.dialogueBox_chatbotMessage_displaysBubbleOnLeft`
- Expected result: The row is left-aligned, preserves the application message
  text, applies the chatbot dialogue CSS style, and exposes the accessible text
  `LuckyNoSlacky: <message>`.

## Test Case: DialogueBox displays warning messages

- Aim: Verify that application warnings use a distinct visual style.
- Test: `DialogueBoxTest.dialogueBox_warningMessage_appliesWarningStyle`
- Expected result: The warning is left-aligned and applies the warning
  dialogue CSS style, exposes the drafted warning accessible text, and shows a
  visible warning marker.

## Test Case: DialogueBox displays successful responses

- Aim: Verify that successful task changes use a clover-green chatbot bubble.
- Test: `DialogueBoxTest.dialogueBox_successMessage_displaysCloverMarker`
- Expected result: The response remains left-aligned, applies the success
  dialogue CSS style, and shows a visible `🍀` marker without changing the
  response text.

## Test Case: DialogueBox displays information responses

- Aim: Verify that task lists and search results use the information tone.
- Test: `DialogueBoxTest.dialogueBox_infoMessage_appliesInfoStyle`
- Expected result: The response remains left-aligned, applies the information
  dialogue CSS style, and does not add a tone prefix or marker to the message.

## Test Case: DialogueBox displays system errors

- Aim: Verify that storage failures use a stronger non-warning error style.
- Test: `DialogueBoxTest.dialogueBox_systemErrorMessage_displaysErrorMarker`
- Expected result: The response remains left-aligned, applies the system-error
  dialogue CSS style, and shows a visible `⛔` marker without changing the
  response text.

## Test Case: Main window exposes accessible labels

- Aim: Verify that the conversation history, command field, and Send button
  expose the drafted labels from the accessibility specification.
- Test: `MainWindowTest.mainWindow_controls_exposeAccessibleLabels`
- Expected result: Each control exposes its configured accessible text and the
  command field and Send button expose their keyboard-use help text.

## Test Case: Main window focuses the command field on startup

- Aim: Verify that keyboard users can start typing immediately.
- Test: `MainWindowTest.mainWindow_startApplication_focusesCommandInput`
- Expected result: The command field has focus after the window opens.

## Test Case: Main window supports forward keyboard traversal

- Aim: Verify that Tab follows the specified focus order.
- Test: `MainWindowTest.mainWindow_tabTraversal_movesForwardThroughControls`
- Expected result: Focus moves from command input to Send, then conversation
  history, then back to command input.

## Test Case: Main window supports reverse keyboard traversal

- Aim: Verify that Shift+Tab reverses the specified focus order.
- Test: `MainWindowTest.mainWindow_tabTraversal_movesBackwardThroughControls`
- Expected result: Focus moves from conversation history to Send, then back to
  command input.

## Test Case: Send-button submission restores focus

- Aim: Verify that keyboard users can continue typing after using Send.
- Test: `MainWindowTest.mainWindow_sendButtonSubmission_returnsFocusToCommandInput`
- Expected result: Focus returns to the command field after a Send-button
  submission.

## Test Case: Main window displays a conversation

- Aim: Verify that entering an unrecognised command creates a user row and a
  warning response row.
- Test: `MainWindowTest.mainWindow_unknownCommand_displaysUserAndWarningMessages`
- Actions: Enter `unknown` in the command field and submit it.
- Expected result: The conversation contains the greeting, a right-aligned
  user row containing `unknown`, and a left-aligned warning row containing the
  actual unknown-command response. The user row does not contain `You said:`.

## Test Case: Main window assigns response tones

- Aim: Verify that the GUI receives semantic response tones from the chatbot.
- Test: `MainWindowTest.mainWindow_unknownCommand_displaysUserAndWarningMessages`
- Expected result: Invalid input uses the warning style while task changes,
  lists, and searches use their corresponding success or information styles.

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
- Use a screen reader to confirm that the drafted labels and message roles are
  announced meaningfully.
- Enter `bye` and verify that both command controls become unavailable while
  the farewell remains visible before the delayed exit.
