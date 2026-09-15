# Personality Resource Integration Specification

## Purpose

Integrate the cleaned visual resource package from `branch-personality` into
`branch-BetterGui` while keeping BetterGUI as the owner of shared GUI
behaviour, semantic response roles, accessibility, and interaction logic.

This document is the implementation handoff for the BetterGUI integration
agent. The resource package has already been cleaned so that it can be
transferred selectively; do not cherry-pick the historical personality
implementation commits wholesale.

## Source package and approved transfer set

Transfer only the following paths from the cleaned `branch-personality`
commit:

| Source path | Destination | Purpose |
| --- | --- | --- |
| `src/main/resources/css/personality.css` | same path | Scoped visual selectors and palette tokens |
| `src/main/resources/fonts/PatrickHand-Regular.ttf` | same path | Ordinary interface typography |
| `src/main/resources/fonts/OFL-PatrickHand.txt` | same path | Font licence |
| `src/main/resources/fonts/RobotoMono-Regular.ttf` | same path | Task-list typography |
| `src/main/resources/fonts/OFL-RobotoMono.txt` | same path | Font licence |
| `src/main/resources/images/clover-pattern.png` | same path | Repeating conversation background |
| `src/main/resources/view/BrandHeader.fxml` | same path | Standalone branded header component |
| `docs/avatar-treatment-specification.md` | same path | Avatar visual requirements |
| `docs/brand-palette-specification.md` | same path | Palette and tone-token requirements |
| `docs/branded-header-specification.md` | same path | Header layout requirements |
| `docs/clover-pattern-background-specification.md` | same path | Background requirements |
| `docs/task-list-rendering-specification.md` | same path | Task-block visual requirements |
| `docs/images/brand-palette-mockup.png` | same path | Visual reference |

Do not transfer the personality branch-cleanup specification, personality
checklist, or deferred response-message review as implementation files. The
existing BetterGUI integration specification and checklist remain the
authoritative branch-level documents.

## Ownership boundary

BetterGUI must continue to own:

- command processing, persistence, and response wording;
- `ResponseTone` and any structured response-kind classification;
- `LuckyNoSlacky.java`, command classes, and response models;
- `DialogueBox` and `MainWindow` behaviour;
- accessibility labels, keyboard traversal, focus restoration, scrolling,
  and exit timing;
- the canonical `test/gui-test-plan.md` and functional GUI tests.

Personality resources may supply appearance only. Do not reintroduce
`You said:`, `LuckyNoSlacky said:`, text-based tone detection, alternate
warning alignment, or replacement avatar assets.

## Integration steps

### 1. Install and load the resources

1. Copy the approved files listed above into the BetterGUI worktree.
2. Confirm both font binaries and their licences are present.
3. Load the embedded fonts once during GUI startup with JavaFX
   `Font.loadFont(...)`, before any styled controls are displayed. Keep this
   bootstrap side-effect limited to font registration; it must not alter
   command or focus behaviour.
4. Add `personality.css` after the existing shared stylesheets so the
   personality selectors can supply the approved visual treatment.
5. Verify that the stylesheet and asset URLs resolve from both the IDE run and
   the packaged application.

### 2. Integrate the branded header

Use the existing `#brandHeaderSlot` in `MainWindow.fxml` as the integration
point:

1. Include `BrandHeader.fxml` inside `#brandHeaderSlot`.
2. Set the slot to visible and managed after the header has been included.
3. Keep the header above and outside `#scrollPane`, so it remains fixed while
   the conversation scrolls.
4. Preserve the current `#scrollPane`, `#dialogContainer`, `#commandRow`,
   `#userInput`, and `#sendButton` IDs.
5. Keep the header non-clickable and mouse-transparent. Do not add it to the
   command-field focus cycle or change the 320 x 480 minimum window size.
6. Preserve the exact title `LuckyNoSlacky` and tagline `Your lucky task
   buddy`.

The component uses the existing LuckyNoSlacky avatar resource. Do not replace
or regenerate the supplied avatar files.

### 3. Apply the palette and typography selectors

Connect the scoped selectors from `personality.css` to the existing BetterGUI
nodes without changing their semantic roles:

| BetterGUI node or role | Personality selector(s) |
| --- | --- |
| Header root/title/tagline | `.brand-header`, `.brand-title`, `.brand-tagline` |
| Ordinary interface labels and controls | `.personality-interface-text` or `.personality-prose` |
| Neutral response | `.personality-response-neutral` and its label selector |
| Success response | `.personality-response-success` and its label selector |
| Information response | `.personality-response-information` and its label selector |
| Warning response | `.personality-response-warning` and its label selector |
| System-error response | `.personality-response-system-error` and its label selector |
| User message bubble | `.personality-user-message` and `.personality-user-label` |
| Input field and Send button | `.personality-input-field`, `.personality-send-button` |

The existing `ResponseTone` mapping is the source of truth. Apply the visual
classes from the existing enum/role path; never inspect message text to infer
the tone. If CSS specificity prevents a personality class from overriding a
shared declaration, add a small, reviewed BetterGUI bridge rule or adjust the
BetterGUI node structure. Do not duplicate tone classification in CSS or
personality code.

The ordinary interface uses Patrick Hand. Task content uses Roboto Mono. The
task font must not be applied to the header, input placeholder, Send button,
or ordinary prose.

### 4. Integrate supplied avatars

Preserve the existing image resources and use them in the existing speaker
paths:

- chatbot: `/images/luckynoslacky.jpg`;
- user: `/images/user.png`.

For conversation rows, retain the current circular clipping and add the
personality frame treatment through a non-interactive wrapper where necessary:

```text
DialogueBox
├── avatar frame (personality-avatar-frame + speaker-specific frame class)
│   └── existing ImageView (personality-avatar-image)
└── existing message container
```

Apply the orange/green treatment to the chatbot frame and the light-blue ring
to the user frame. Keep the treatment ring-only: do not add a coloured avatar
background or replace the supplied image. Conversation avatars may grow
slightly from the current size, but the row must remain responsive at the
minimum window size and the avatar must remain mouse-transparent and outside
keyboard traversal.

The header may use the same chatbot asset at its specified 56 px size. Do not
introduce a second avatar source.

### 5. Integrate task-list styling

Task-list rendering is a BetterGUI-owned structural integration because the
personality branch may not change task content, numbering, or command
semantics.

1. Preserve the exact task text, numbering, checkbox markers, line breaks,
   and date formatting produced by the command layer.
2. Ensure task content is rendered with the embedded Roboto Mono font.
3. Group adjacent task lines belonging to the same response in one dark
   `.personality-task-block` container with warm pale-yellow text.
4. Keep non-task prose in the ordinary Patrick Hand treatment.
5. Use an existing structured response/command result signal if available. If
   the current model cannot distinguish task-list output without inspecting
   text, add the smallest BetterGUI-owned response-kind signal and test it;
   do not implement text-based classification in `personality.css` or in the
   personality resource package.
6. Keep one chatbot response as one accessible message. The task block is a
   visual child/grouping, not four separate dialogue messages or four
   separately announced response boxes.

The target visual relationship is:

```text
chatbot avatar | one response container
               | ordinary prose (Patrick Hand)
               | one dark task block (Roboto Mono)
               |   [T] [ ] read book
               |   [D] [ ] submit report (by: ...)
```

Do not change task ordering, response wording, or accessibility ownership.

### 6. Apply the clover background

1. Apply `.personality-background` to the conversation viewport/content area,
   not to the fixed header or command row.
2. Keep the existing transparent ScrollPane viewport behavior so the cream
   green-tinted background and smaller, denser repeating clover pattern are
   visible.
3. Keep the primarily pineapple-yellow header background distinct from the
   patterned conversation area.
4. Verify that the pattern does not obscure text, break scrolling, or become
   a focusable/clickable control.

## Tests and acceptance criteria

### Automated checks

Run from the BetterGUI worktree with Java 25:

```text
./gradlew test guiTest checkstyleMain checkstyleTest javadoc
```

Also run all documented cases in `test/ui-test-plan.md` using isolated task
data, and run `git diff --check`.

Add or update only BetterGUI-owned tests for observable integration behavior.
Use three underscore-separated parts in JUnit test names:
`unitBeingTested_descriptionOfTestInputs_expectedOutcome`.

### Manual checks

- Header remains visible while many messages scroll.
- Header is not clickable and does not enter keyboard focus traversal.
- Header title/tagline, ordinary prose, placeholder, and Send button use
  Patrick Hand.
- Task text visibly uses Roboto Mono and remains in one dark grouped block.
- Chatbot avatar uses the supplied asset with the orange/green ring.
- User avatar uses the supplied asset with the light-blue ring.
- All response tones retain their BetterGUI semantic mapping while adopting
  the palette tokens.
- The clover pattern is small, dense, repeating, and does not affect layout.
- The application remains readable and usable at 320 x 480 and after resize.
- Focus order, accessibility text, scrolling, persistence, and farewell
  behavior are unchanged.
- The packaged application resolves fonts, FXML, CSS, and image resources.

## Completion checklist

- [ ] Transfer only the approved resources and visual documentation.
- [ ] Load Patrick Hand and Roboto Mono from packaged resources.
- [ ] Load `personality.css` after the shared stylesheets.
- [ ] Include `BrandHeader.fxml` through `#brandHeaderSlot`.
- [ ] Keep the header fixed, non-clickable, and outside the ScrollPane.
- [ ] Apply the scoped palette and typography selectors through BetterGUI
      semantic paths.
- [ ] Apply the supplied avatar assets with the approved frame treatments.
- [ ] Group task lines visually without changing their text or semantics.
- [ ] Apply the clover background only to the conversation area.
- [ ] Update BetterGUI tests and the canonical GUI test plan if observable
      behavior changed.
- [ ] Run the full automated and manual validation checklist.
- [ ] Commit the controlled integration on `branch-BetterGui`.
- [ ] Only then merge `branch-BetterGui` into `master`.
