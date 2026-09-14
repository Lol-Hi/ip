# LuckyNoSlacky Task-List Rendering Specification

## Purpose

This specification corrects two visual issues in multiline chatbot responses:

1. A single LuckyNoSlacky reply must remain a single chatbot message bubble.
2. Task-list output must use Roboto Mono and be visually distinguished from
   ordinary conversational text.

The change affects presentation only. Command behaviour, response wording,
task ordering, persistence, and CLI output must remain unchanged.

## Design decision

The task-list text will use a **warm pale-yellow** colour. This keeps the
existing Pineapple Pop identity while remaining readable against the dark
task panel and avoiding an overly cold or technical appearance.

The final visual hierarchy is:

- Patrick Hand for speaker labels and conversational prose;
- Roboto Mono for task-list content;
- one semantic outer chatbot bubble for the complete response;
- one dark inner panel for each contiguous task-list section.

## Current problem

The current rendering splits a multiline response into several independent
message blocks. For example, a `list` response containing an introduction and
three tasks appears as four separate chatbot bubbles.

The task detector also expects a space between the task type and checkbox.
LuckyNoSlacky actually produces numbered output such as
`1.[T][ ] read book`, so those lines remain in Patrick Hand instead of
receiving the agreed Roboto Mono and dark-panel treatment.

## Target behaviour

For this response:

```text
Nah, all these things you need to do:
1.[T][ ] read book
2.[T][ ] read / book
3.[D][ ] slash date (by: Wed Aug 26 2026, 11:59pm)
```

the GUI should produce one chatbot dialogue entry:

```text
LuckyNoSlacky said:
┌─────────────────────────────────────────────────┐
│ Nah, all these things you need to do:            │
│                                                   │
│ ┌─────────────────────────────────────────────┐ │
│ │ 1.[T][ ] read book                            │ │
│ │ 2.[T][ ] read / book                          │ │
│ │ 3.[D][ ] slash date (by: Wed Aug 26 2026,    │ │
│ │    11:59pm)                                  │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

The diagram is structural rather than pixel-perfect. The outer bubble keeps
the response's semantic tone, while the task panel uses the shared dark task
style regardless of whether the outer response is neutral, successful, or
informational.

## Functional requirements

### One bubble per response

Every `ChatResponse` must create exactly one `DialogueBox`, even when its text
contains multiple lines. Newline characters must not be used as a reason to
create multiple avatar rows, speaker labels, or outer bubbles.

The single bubble must retain:

- one chatbot avatar;
- one speaker label;
- one semantic response-tone class;
- one outer-bubble boundary and spacing context.

### Mixed content within one bubble

The content inside the outer bubble may contain ordered segments:

```text
prose segment
task-list segment
prose segment, if present
```

Prose segments remain Patrick Hand and preserve meaningful line breaks. Task
segments are rendered as task-list panels inside the same outer bubble.

### Task-line recognition

The renderer must recognise both numbered and unnumbered task lines. It must
support the existing display markers:

- `[T]` for todos;
- `[D]` for deadlines;
- `[E]` for events;
- `[ ]` for incomplete tasks;
- `[X]` for completed tasks.

The optional task number must support one or more digits. These examples must
all be recognised:

```text
[T][ ] read book
1.[T][ ] read book
12.[E][X] submit report
```

### Task-list grouping

Consecutive recognised task lines must be grouped into one task-list panel.
The renderer must:

- preserve task order and visible numbering;
- preserve the task marker and completion state;
- keep each task on its own logical line;
- allow long descriptions to wrap within the panel;
- visually indent wrapped continuation lines;
- support a response containing only task lines;
- support prose before or after the task list.

If separate task-list sections are separated by prose, each section may have
its own inner panel, but the complete response must still have one outer
chatbot bubble.

## Typography and colour requirements

| Element | Typeface | Colour / treatment |
| --- | --- | --- |
| Speaker label | Patrick Hand | Existing semantic tone label colour |
| Ordinary chatbot prose | Patrick Hand | Existing semantic tone text colour |
| Outer chatbot bubble | N/A | Existing neutral, success, information, warning, or system-error tone |
| Task-list panel | N/A | Dark code-style background `#26352C` |
| Task-list content | Roboto Mono | Warm pale yellow `#FFF3B0` |
| Task-list panel border | N/A | Subtle green/charcoal border with readable contrast |
| Chatbot avatar | Existing supplied asset | Display once for the complete response |

The task-list panel should have internal padding, a modest gap from the prose,
and enough width for normal task output. It must remain legible at the minimum
window size and when the application is resized.

## Suggested implementation structure

The dialogue renderer should parse the complete response into a small ordered
content model before constructing JavaFX nodes. A conceptual representation is:

```text
DialogueBox
└── outer semantic bubble
    └── message-content container
        ├── prose node, Patrick Hand
        └── task-list panel
            └── task text node, Roboto Mono and warm pale yellow
```

The existing response-tone mechanism should remain responsible for the outer
bubble. Task detection and task-panel styling should remain independent of
the semantic response tone so task lists are treated consistently in list,
success, and other applicable responses.

## Acceptance criteria

- A multiline LuckyNoSlacky response renders as one outer chatbot bubble.
- The avatar and speaker label appear once per response.
- A prose introduction and its task list share the same outer bubble.
- Numbered task lines receive Roboto Mono.
- Unnumbered task lines continue to receive Roboto Mono.
- Consecutive task lines appear together in one dark panel.
- Task-panel text uses warm pale yellow `#FFF3B0`.
- Task-panel text remains readable when a task wraps onto another line.
- Task numbering, markers, completion state, and ordering are unchanged.
- Non-task multiline responses remain one bubble without an unnecessary task panel.
- Existing semantic outer-bubble tones continue to work.
- CLI output and task behaviour remain unchanged.

## Implementation checklist

- [x] Replace newline-based multiple-bubble rendering with one response-level
  `DialogueBox`.
- [x] Parse a response into prose and contiguous task-list segments.
- [x] Recognise numbered and unnumbered `[T]`, `[D]`, and `[E]` lines.
- [x] Group adjacent task lines into one inner dark task-list panel.
- [x] Apply Roboto Mono only to task-list content.
- [x] Apply warm pale-yellow `#FFF3B0` to task-list content.
- [x] Preserve Patrick Hand for prose, labels, input text, and Send-button text.
- [x] Add tests for numbered task detection and multi-item grouping.
- [x] Add tests for prose-plus-task-list rendering in one outer bubble.
- [x] Review and update `test/gui-test-plan.md`.
- [x] Run Java 25 tests, GUI tests, UI tests, Checkstyle, Javadoc, and code-quality review.
- [ ] Manually verify long tasks, completed tasks, and a list containing at
  least ten items.
