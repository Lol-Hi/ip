# LuckyNoSlacky Task-List Rendering Specification

## Purpose

This specification corrects two visual issues in multiline chatbot responses:

1. A single LuckyNoSlacky reply must remain a single chatbot message bubble.
2. Task-list output must use Roboto Mono and be visually distinguished from
   ordinary conversational text.

The change affects presentation only. Command behaviour, response wording,
task ordering, persistence, and CLI output must remain unchanged.

## Branch ownership boundary

This specification is a visual contract for the personality resource package.
`branch-personality` may provide the task-panel tokens, typography, scoped CSS
selectors, mockups, and acceptance criteria. It must not change task content,
response construction, `DialogueBox`, `MainWindow`, response-tone semantics,
functional tests, or `test/gui-test-plan.md`. The BetterGUI integration owner
will implement the response-level grouping and node structure while preserving
the behaviour described here.

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

## Visual integration contract

The following requirements describe the result that BetterGUI must integrate;
they are not permission for `branch-personality` to modify shared Java code.

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

## Suggested integration structure

The BetterGUI dialogue renderer should parse the complete response into a small
ordered content model before constructing JavaFX nodes. Personality only
specifies the visual nodes and selectors expected by that integration. A
conceptual representation is:

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
success, and other applicable responses. The Java implementation and its
functional tests belong to `branch-BetterGui`.

### Personality-owned selectors

The resource package should expose scoped selectors such as
`.personality-task-block` and `.personality-task-content`. These selectors
must describe the dark panel, warm pale-yellow text, Roboto Mono typography,
padding, wrapping, and continuation-line indentation. They must not redefine
BetterGUI semantic selectors directly. BetterGUI may apply them to the nodes
it creates during controlled integration.

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

- [x] Approve the one-outer-bubble and grouped-task-panel visual direction.
- [x] Approve Roboto Mono and warm pale-yellow `#FFF3B0` for task content.
- [x] Define the dark task-panel appearance and wrapping requirements.
- [ ] Add `.personality-task-block` and `.personality-task-content` to the
  personality-owned stylesheet.
- [ ] Document the required node structure and selector handoff for BetterGUI.
- [ ] Let BetterGUI implement response-level grouping and task-line parsing on
  `branch-BetterGui`.
- [ ] Let BetterGUI update functional/GUI coverage and reconcile
  `test/gui-test-plan.md` during controlled integration.
- [ ] Manually verify long tasks, completed tasks, and a list containing at
  least ten items after integration.
