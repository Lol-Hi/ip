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

- [x] Add and update unit tests for response tones and GUI behavior.
- [x] Run the Java 25 JUnit suite.
- [x] Run a fresh JavaFX GUI regression suite (18 of 18 tests passed).
- [x] Run Checkstyle and Javadoc validation.
- [x] Run all 25 documented CLI UI test cases with isolated task data.
- [x] Build and launch a fresh GUI fat JAR successfully.
- [x] Verify task persistence across packaged application restarts in an
  isolated data directory.
- [x] Implement the accessibility labels and keyboard-navigation specification.
- [x] Separate chatbot speaker roles from semantic response tones.
- [x] Add neutral, success, information, warning, and system-error GUI tones.

## Manual acceptance checks passed

- [x] Verify visually that the GUI remains readable at 320 x 480.
- [x] Resize the window and confirm that bubbles, text, and avatars remain readable.
- [x] Confirm that the conversation scrolls to the newest message after many commands.
- [x] Enter `bye` and confirm that the GUI farewell remains visible before delayed exit.
- [x] Add a task through the GUI, close it, relaunch it, and confirm persistence.

## Possible future improvements

- [ ] Perform a screen-reader review of the implemented accessible labels.
