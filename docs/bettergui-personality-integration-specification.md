# BetterGUI–Personality Integration Specification

## Purpose

This document defines the integration boundary between `branch-BetterGui` and
`branch-personality`. BetterGUI owns application behaviour and the shared GUI
contract. Personality owns visual resources and decorative styling.

The final implementation must be integrated on `branch-BetterGui` before it is
merged into `master`. `branch-personality` must not be merged independently
after its visual work has been incorporated.

## BetterGUI-owned contract

BetterGUI owns the following behaviour:

- command processing, persistence, and response wording;
- `ResponseTone` classification;
- raw user-message rendering without speaker prefixes;
- left-aligned chatbot messages and right-aligned user messages;
- warning, success, information, and system-error semantics;
- accessibility labels and keyboard navigation;
- focus restoration, automatic scrolling, and farewell timing;
- the canonical functional GUI test plan.

The following strings and semantics must not be changed by personality work:

- `Here you see the whole chat history! Just use your arrow or page keys to
  scroll up scroll down can liao`;
- `Lai tell me what you want me to do, then click Send`;
- `Bodoh sia like that also can kena warning`;
- the absence of `You said:` and `LuckyNoSlacky said:` prefixes.

## Personality-owned contract

Personality owns only visual resources and visual specifications, including:

- fonts and colour tokens;
- the branded header appearance;
- clover-pattern artwork;
- task-list typography and decorative grouping;
- branding mockups and visual acceptance criteria.

Personality selectors should use scoped names such as `brand-*` and
`personality-*`. They must not redefine the meaning of BetterGUI semantic tone
classes.

## Stable integration regions

`MainWindow.fxml` provides these stable regions:

1. `#brandHeaderSlot` — an optional header area, hidden and unmanaged until
   personality content is deliberately inserted;
2. `#scrollPane` — the conversation region, which remains vertically
   scrollable and retains its accessibility text;
3. `#commandRow` — the command-entry region, which retains the command field,
   Send button, and keyboard focus order.

The following IDs and accessibility attributes must remain stable:

- `#brandHeaderSlot`;
- `#scrollPane`;
- `#dialogContainer`;
- `#commandRow`;
- `#userInput`;
- `#sendButton`.

## Integration rules

- Add personality resources before changing shared layout files.
- Insert the personality header through `#brandHeaderSlot`.
- Keep the header outside the scrolling conversation.
- Preserve all BetterGUI accessibility and focus behaviour.
- Add personality styles rather than replacing response-tone styles.
- Do not alter command output or task content for visual presentation.
- Resolve all shared-file changes on `branch-BetterGui`.
- Keep `test/gui-test-plan.md` as the single canonical GUI test plan.

## Validation requirements

Before integration is considered complete:

- both worktrees must be clean;
- the branch diff must contain only approved files;
- `git diff --check` must pass;
- the Java 25 JUnit suite must pass;
- Checkstyle and Javadoc must pass;
- `guiTest` and the documented UI tests must pass;
- the accessibility, keyboard, responsive layout, and persistence checks must
  be completed.
