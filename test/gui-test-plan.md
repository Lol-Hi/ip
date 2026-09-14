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
  the message text, applies the user dialogue CSS style, and uses the unified
  handwritten interface font.

## Test Case: DialogueBox displays chatbot messages

- Aim: Verify that neutral chatbot messages use the pineapple-yellow palette
  and left-side alignment.
- Test: `DialogueBoxTest.dialogueBox_neutralChatbotMessage_displaysPineappleLabelOnLeft`
- Expected result: The row is left-aligned, displays `LuckyNoSlacky said:`,
  preserves the message text, applies the neutral chatbot dialogue CSS style,
  and uses the unified handwritten interface font.

## Test Case: DialogueBox applies semantic response tones

- Aim: Verify that a successful task response can use the lucky green visual
  treatment independently from its text.
- Test: `DialogueBoxTest.dialogueBox_successTone_addsSuccessDialogueStyle`
- Actions: Create a chatbot dialogue with the `success-dialogue` style.
- Expected result: The dialogue retains its chatbot layout and includes the
  `success-dialogue` CSS class for the green success palette.

## Test Case: DialogueBox styles task lines as code

- Aim: Verify that task display lines remain easy to scan using monospace text
  inside a shared dark task-list panel.
- Test: `DialogueBoxTest.dialogueBox_taskLine_usesMonospaceTaskStyle`
- Actions: Create a chatbot dialogue containing `[T][ ] read book`.
- Expected result: The task line receives the `task-content` style, uses the
  bundled monospace font, and appears in a dark code-style block. The task
  panel is contained within one outer dialogue bubble.

## Test Case: DialogueBox groups numbered task lists

- Aim: Verify that numbered task lines and their introduction remain in one
  chatbot bubble while the task lines share one dark panel.
- Test: `DialogueBoxTest.dialogueBox_numberedTaskList_groupsAdjacentLinesInOnePanel`
- Actions: Create a chatbot dialogue containing prose followed by numbered
  `[T]` and `[D]` task lines.
- Expected result: The prose uses the handwritten content style, all adjacent
  task lines are grouped into one `task-list` container, and the complete
  response remains one outer dialogue row.

## Test Case: Main window displays a conversation

- Aim: Verify that entering a command creates both a user dialogue row and a
  chatbot response row.
- Test: `MainWindowTest.mainWindow_unknownCommand_displaysBothSpeakerMessages`
- Actions: Enter `unknown` in the command field and submit it.
- Expected result: The conversation contains the greeting, a `You said:` row,
  and a `LuckyNoSlacky said:` row. User messages use light blue, while chatbot
  response tones use their configured neutral, success, information, warning,
  or system-error palettes.

## Test Case: Main window displays the branded header

- Aim: Verify that the fixed header displays the approved LuckyNoSlacky
  branding before the conversation.
- Test: `MainWindowTest.mainWindow_brandHeader_displaysConfiguredBranding`
- Actions: Launch the main window and inspect the branded header nodes.
- Expected result: The header displays the supplied chatbot avatar,
  `LuckyNoSlacky` as its title, and `Your lucky task buddy` as its tagline.
  The header has the `brand-header` style and is outside the conversation
  container.

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

- Aim: Verify that the FXML layout, CSS stylesheets, avatar images, and bundled
  fonts are available from the application classpath.
- Test: `LuckyNoGuiTest.luckyNoGui_resourcePaths_areAvailable`
- Expected result: The layout, both CSS stylesheets, both avatar resources, and
  both font resources are all found.

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
- Expected result: The input field expands with the window, while the Send
  button remains at the bottom-right with a stable width.

## Acceptance checks beyond `guiTest`

These checks are intentionally broader than the deterministic Gradle task and
may require a desktop automation capability or manual verification:

- Enter `bye` and verify that the goodbye message is visible before the window
  closes after the configured delay.
- Enter enough commands to verify that the conversation scrolls to the latest
  message.
- Resize the window and verify that message text and avatars remain readable.
- Verify that the branded header remains visible while the conversation
  scrolls.
- Add a task, close the GUI, relaunch it, and verify that the task persists.
