# Accessibility and Keyboard Navigation Specification

Status: Implemented with provisional wording for the three labels that were
not manually drafted.

## Goal

The GUI must be usable without a mouse and must expose meaningful context to a
screen reader. It must not rely on bubble colour alone to communicate an error.

## Accessible labels

Use JavaFX `accessibleText`, `accessibleHelp`, and role descriptions where
appropriate.

| Element | Accessible text | Accessible help |
|---|---|---|
| Conversation history | `Here you see the whole chat history! Just use your arrow or page keys to scroll up scroll down can liao` | None required; the text already describes how to review the conversation. |
| Command field | `Lai tell me what you want me to do, then click Send` | `You can also press Enter to send the command without using the mouse.` |
| Send button | `Send command` | `Sends the command currently entered in the command field.` |
| User message bubble | `You: <message>` | None required. |
| Normal LuckyNoSlacky message bubble | `LuckyNoSlacky: <message>` | None required. |
| Warning message bubble | `Bodoh sia like that also can kena warning <actual warning message>` | None required. |
| Avatar images | Decorative; no accessible text and no keyboard focus | None required. |

The warning bubble must include both the supplied warning label and the actual
warning message. This preserves the reason for the warning for screen-reader
users.

Visually, a warning must also have a non-colour cue, such as a small `⚠`
marker. The original chatbot error text must remain unchanged.

## Keyboard behaviour

- Initial focus goes to the command field when the window opens.
- Pressing `Enter` in the command field sends the command.
- Focus order is command field, Send button, conversation history, then back
  to the command field. `Shift+Tab` moves through the same order in reverse.
- Pressing `Space` or `Enter` when Send has focus sends the command.
- Individual bubbles and avatars are not tab stops.
- When conversation history has focus, arrow keys, Page Up/Down, Home, and End
  must remain available to review it.
- After a normal command, warning, or Send-button submission, focus returns to
  the command field.
- After `bye`, both command input and Send become unavailable while the
  farewell remains visible before the window closes.
- The focused control has a clear, high-contrast outline.

## Automated checks

- Verify initial focus on the command field.
- Verify Tab and Shift+Tab traversal in the stated order.
- Verify keyboard activation of Send.
- Verify focus returns to command input after successful and invalid commands.
- Verify role-aware accessible text for user, chatbot, and warning bubbles.
- Verify avatars and individual bubbles cannot receive keyboard focus.

## Manual checks

- Use a screen reader to confirm each control and message role is announced
  meaningfully.
- Confirm warning information is understandable without relying on colour.
- Confirm focus remains easy to see at the 320 x 480 minimum window size.
- Enter `bye` and confirm that both interactive controls become unavailable
  before the farewell delay completes.

## Deferred behaviour

Announcing every new message automatically is deferred. JavaFX does not offer
reliable, cross-platform live-region behaviour, so it should only be added if
it can be demonstrated on the target macOS setup.
