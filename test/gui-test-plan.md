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

## Test Case: Main window ignores blank input

- Aim: Verify that submitting an empty command does not add dialogue rows.
- Test: `MainWindowTest.mainWindow_blankInput_doesNotAddDialogue`
- Actions: Click `Send` without entering a command.
- Expected result: The greeting remains the only dialogue row.

## Test Case: Main window scrolls to the latest dialogue

- Aim: Verify that a conversation exceeding the visible area automatically
  scrolls to its newest response.
- Test: `MainWindowTest.mainWindow_manyMessages_scrollsToLatestDialogue`
- Actions: Enter `unknown` eight times.
- Expected result: All generated dialogue rows remain present, the latest
  response is displayed, and the scroll position is at the bottom.

## Test Case: Main window preserves responsive layout invariants

- Aim: Verify that resizing does not make the command controls unusable.
- Test: `MainWindowTest.mainWindow_resizeWindow_preservesLayoutInvariants`
- Actions: Resize the window larger, then attempt to resize it below its
  supported bounds.
- Expected result: The controls remain visible within the scene, have positive
  bounds, and the window and scene remain non-zero after the small resize.

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
- Test: Covered by
  `MainWindowTest.mainWindow_resizeWindow_preservesLayoutInvariants`
- Actions: Review the automated layout-invariant test, then inspect the visual
  arrangement manually when a desktop is available.
- Expected result: The input field expands with the window, while the Send
  button remains at the bottom-right with a stable width.

## Test Case: GUI goodbye is visible before delayed exit

- Aim: Verify that the goodbye response and disabled-input state are observable
  before the GUI process exits.
- Test: `LuckyNoGuiSubprocessTest.guiProcess_byeCommand_delaysExitAfterShowingGoodbye`
- Actions: Launch an isolated GUI subprocess and submit `bye`.
- Expected result: The exact goodbye message is shown, the input is empty and
  disabled, the process remains alive for the configured delay, and then exits
  successfully.

## Test Case: GUI task persists across relaunches

- Aim: Verify persistence through two complete GUI application launches.
- Test: `LuckyNoGuiSubprocessTest.guiProcess_taskAcrossRelaunches_loadsPersistedTask`
- Actions: Launch the GUI in an isolated directory, create a task, terminate
  it, relaunch it in the same directory, and submit `list`.
- Expected result: The second process reports no startup load error and lists
  the task created by the first process.

## Acceptance checks beyond `guiTest`

These checks are intentionally broader than the deterministic Gradle task and
may require a desktop automation capability or manual verification:

- Enter `bye` and verify that the goodbye message is visible before the window
  closes after the configured delay.
- Resize the window and verify that message text and avatars remain readable.
- Verify the automated persistence scenario against a desktop build when
  visual confirmation is available.
