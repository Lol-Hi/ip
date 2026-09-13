# LuckyNoSlacky Improvements Checklist

Last updated: 2026-09-14

## GUI conversation increment

- [x] Use actual LuckyNoSlacky responses in the GUI conversation.
- [x] Display application responses as left-aligned conversational bubbles.
- [x] Display user commands as right-aligned conversational bubbles.
- [x] Remove the `You said:` label from user messages.
- [x] Keep the existing chatbot and user avatar assets.
- [x] Clip both avatars into circular profile images.
- [x] Add a separate warning style for invalid commands and storage errors.
- [x] Preserve the 320 x 480 minimum window size.
- [x] Make message bubbles wrap responsively as the window changes size.
- [x] Document the sample task conversation using real chatbot responses.

## Verification completed

- [x] Add and update unit tests for response severity and GUI behavior.
- [x] Run the Java 25 JUnit suite.
- [x] Run the JavaFX GUI regression suite.
- [x] Run Checkstyle and Javadoc validation.
- [x] Run all 25 documented CLI UI test cases.
- [x] Build and launch the GUI fat JAR successfully.

## Remaining manual acceptance checks

- [ ] Verify the GUI visually at the 320 x 480 minimum size.
- [ ] Resize the window and confirm that bubbles, text, and avatars remain readable.
- [ ] Confirm that the conversation scrolls to the newest message after many commands.
- [ ] Enter `bye` and confirm that the farewell remains visible before delayed exit.
- [ ] Add a task, close the GUI, relaunch it, and confirm persistence.

## Possible future improvements

- [ ] Review accessibility labels and keyboard navigation for the final GUI.
- [ ] Consider whether the backend response severity and GUI dialogue type should
  be consolidated after the interface requirements stabilize.
