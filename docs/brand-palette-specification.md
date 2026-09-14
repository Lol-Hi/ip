# LuckyNoSlacky Brand Palette Specification

## Purpose

This specification defines the next personality improvement for LuckyNoSlacky:
a cohesive colour system that reflects the chatbot's playful, lucky character.
It builds on the completed Patrick Hand interface font and Roboto Mono task
blocks without changing command behaviour or response wording.

## Implementation status

- [x] Apply the Pineapple Pop background, dark-blue Send button, and light-blue
  user-message palette.
- [x] Apply neutral, success, information, warning, and system-error chatbot
  response tones.
- [x] Use a clover-marked lucky-green treatment for successful task changes.
- [x] Preserve the supplied circular chatbot and user avatar images.

## Visual direction

The visual direction is **Pineapple Pop with lucky-clover green**:

- Pineapple yellow provides LuckyNoSlacky's warm, energetic base.
- Leaf green communicates successful task progress and the lucky theme.
- Light blue preserves the familiar visual identity of user messages and the
  Send button.
- Cream and gentle coral keep information and warnings readable without making
  the conversation look overly colourful.

![Brand-palette mockup](images/brand-palette-mockup.png)

The mockup is a design reference. The implemented interface must continue to
use the supplied LuckyNoSlacky and user avatar assets, rather than generated
or replacement avatar imagery.

## Typography and avatars

- Use bundled Patrick Hand for ordinary interface text, chatbot responses,
  speaker labels, the input field, and the Send button.
- Use bundled Roboto Mono only for task-display lines such as
  `[T] [ ] read book`.
- Display task lines in dark code-style blocks.
- Use the supplied chatbot avatar image for chatbot rows and the supplied user
  avatar image for user rows.
- Keep avatars circular with the existing subtle shadow. Do not add a coloured
  outline unless a later visual review identifies a need for one.

## Colour tokens

| Token | Colour | Use |
| --- | --- | --- |
| App background | `#FFFBE7` | Main window and conversation background |
| Main text | `#3D2B1F` | Default readable text |
| Primary pineapple | `#D38B08` | Normal chatbot labels, focus accents, decorative emphasis |
| Chatbot neutral | `#FFF4B7` | Greeting, instructions, and general chatbot replies |
| Chatbot neutral border | `#E3BA38` | Border for neutral chatbot bubbles |
| Lucky green | `#2F855A` | Success labels, success borders, and clover emphasis |
| Success bubble | `#DCF4DF` | Successful task changes |
| Informational bubble | `#FFF9DE` | Lists, search results, and empty states |
| Warning bubble | `#FFE2D8` | Recoverable input and validation problems |
| Warning accent | `#B94A3F` | Warning labels and borders |
| User bubble | `#E3F2FD` | User messages |
| User accent | `#2563A8` | User speaker label and user-bubble border |
| Send button | `#174EA6` | Default Send-button background |
| Send button hover | `#123B82` | Send-button hover and pressed states |
| Task block | `#26352C` | Monospace task-display block |
| Task-block text | `#FFF3B0` | Text inside task-display blocks |

## Response colour model

The GUI should assign a semantic tone to each chatbot response. This prevents
the presentation layer from trying to determine a message's meaning by reading
its text.

| Tone | Covered responses | Bubble treatment |
| --- | --- | --- |
| Neutral | Greeting, help, command-format guidance, farewell | Pineapple-yellow bubble with a golden label |
| Success | Add, mark, unmark, delete, snooze, and reschedule confirmations | Mint-green bubble with leaf-green label; success labels may include a small clover accent |
| Information | `list`, `find`, and empty-task-list results | Soft-cream bubble with a golden label |
| Warning | Missing input, invalid command format, invalid dates, invalid task number, and other recoverable validation feedback | Gentle-coral bubble with a muted-red label |
| System error | Task-load and task-save failures | The warning palette with stronger muted-red label/border emphasis |

User messages are not chatbot tones. They always use the light-blue user
bubble and blue speaker label.

## Interaction states

- The focused input field uses the primary pineapple colour for its border.
- The Send button remains dark blue with white text and uses darker blue for
  hover and pressed states.
- Disabled controls remain visibly disabled through reduced contrast while
  retaining readable text.
- Chatbot, user, success, information, and warning bubbles must remain
  visually distinguishable in the same conversation.

## Implementation outline

1. Add CSS style classes for neutral, success, information, warning, system
   error, and user dialogues.
2. Apply the documented palette tokens consistently to labels, bubbles,
   borders, task blocks, input controls, and button states.
3. Extend the response path to carry a semantic tone alongside the response
   text and exit flag.
4. Map each command result and handled exception to its appropriate tone.
5. Keep task-line detection independent from the dialogue tone so dark
   monospace task blocks appear consistently within success and information
   messages.
6. Update GUI tests and `test/gui-test-plan.md` to cover representative tone
   classes and the selected visual resources.
7. Verify the Java 25 JUnit suite, GUI tests, UI tests, Checkstyle, Javadoc,
   and the code-quality review.

## Acceptance criteria

- The app has a recognisable pineapple-yellow, clover-green, and blue visual
  identity instead of a generic blue-and-green Material palette.
- Each chatbot response category uses the documented semantic tone.
- Users can distinguish their light-blue messages from all chatbot responses.
- Success responses visibly communicate a lucky, positive outcome through
  green accents without making all chatbot messages green.
- Task content remains easy to scan in a dark monospace block.
- Supplied avatars remain in use and keep their current circular presentation.
- All text, borders, and controls remain legible at the minimum window size.
- CLI output, commands, persistence, and task behaviour remain unchanged.
