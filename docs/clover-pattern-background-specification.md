# LuckyNoSlacky Clover-Pattern Background Specification

## Purpose

Introduce a custom green-based conversation background with a subtle repeating
`🍀` pattern. This increment extends LuckyNoSlacky's lucky visual identity
while preserving the approved yellow branded header and the existing message
colour system.

This is a GUI-only change. It must not alter command parsing, response text,
task behaviour, persistence, CLI output, or the layout of message content.

## Scope boundary

The background increment covers:

- the main application background behind the conversation;
- the visible conversation viewport;
- a reusable clover pattern asset or equivalent JavaFX rendering;
- contrast and readability adjustments required by the new background.

The increment does not redesign:

- the primarily pineapple-yellow branded header;
- the light-blue user message bubbles;
- semantic chatbot response tones;
- dark Roboto Mono task-list panels;
- avatars, fonts, or input controls.

## Recommended visual direction

Use a pale green base with low-contrast clover marks:

```text
┌──────────────────────────────────────────────┐
│        pineapple-yellow branded header        │
├──────────────────────────────────────────────┤
│  ·  🍀       ·        🍀       ·              │
│       conversation messages                  │
│  🍀       ·       🍀       ·        🍀        │
│       dark task panels remain prominent      │
│  ·       🍀       ·       🍀       ·          │
├──────────────────────────────────────────────┤
│        existing input and Send controls       │
└──────────────────────────────────────────────┘
```

The pattern should feel playful and lucky, not like a high-contrast wallpaper.
The clovers should sit behind the conversation rather than compete with text,
avatars, or semantic response colours.

### Mockup interpretation

The approved mockup is a reference for the conversation background treatment
only. It does not replace the current LuckyNoSlacky UI design. In particular,
the implementation must retain:

- the supplied LuckyNoSlacky and user avatar assets;
- the existing Patrick Hand and Roboto Mono typography;
- the current semantic response bubbles;
- the existing single-bubble task-list layout;
- the approved yellow branded header;
- the current input field and dark-blue Send button.

Do not copy the mockup's alternate avatars, sample wording, window chrome,
oversized decorative clovers, or redesigned input controls.

## Proposed colour tokens

These are recommended starting values for visual review:

| Token | Proposed value | Use |
| --- | --- | --- |
| Conversation green background | `#EAF7E7` | Main conversation viewport |
| Pattern clover green | `#B7DDB5` | Repeating clover marks |
| Pattern opacity | 35% to 50% visual strength | Keeps the pattern subordinate |
| Pattern fallback background | `#FFFBE7` | Safe fallback if the asset cannot load |
| Header background | `#F6D365` | Remains unchanged from the header increment |
| Task-panel background | `#26352C` | Remains unchanged for task readability |

The pattern must be tested against all existing outer bubble tones: neutral,
success, information, warning, system error, and light-blue user messages.

## Functional requirements

### Repeating pattern

- Repeat the clover motif horizontally and vertically across the conversation
  background.
- Keep the motif tile small enough to create a continuous pattern, but large
  enough that individual clovers do not look like visual noise.
- Keep the pattern stationary relative to the viewport; scrolling messages
  should move over the background without exposing gaps or seams.
- Prevent the pattern from appearing inside message bubbles or task panels.
- Ensure the pattern scales consistently when the window is resized.

### Layout preservation

- Keep the yellow branded header fixed above the scrolling conversation.
- Keep the existing bottom input row and dark-blue Send button unchanged.
- Preserve the current message spacing, avatar placement, and outer-bubble
  widths.
- Do not reduce the conversation viewport below the current minimum usable
  dimensions.

### Failure handling

If the custom pattern asset cannot be loaded, the application should still
start with the plain green background or the existing cream background. A
missing decorative asset must never prevent the chatbot from opening.

## Pattern asset and implementation options

The selected implementation is a small bundled transparent illustration asset
containing the approved clover motif, applied to the conversation background.
This keeps the pattern deterministic and avoids relying on platform-specific
emoji rendering. The asset is stored at
`src/main/resources/images/clover-pattern.png`.

Before implementation, choose one of these approaches:

1. **Clover illustration asset** — a custom green clover drawing that evokes
   `🍀` while looking consistent on every machine.
2. **Emoji-derived asset** — a transparent tile based on the literal `🍀`
   emoji, preserving the requested playful appearance but potentially varying
   slightly by operating system.
3. **JavaFX-rendered motif** — draw the pattern in a dedicated background
   layer, offering responsive control but adding more layout code.

Whichever option is selected, keep the decorative layer separate from the
conversation nodes so it cannot intercept input or interfere with scrolling.

## Detailed implementation plan

### Phase 1: Confirm the visual asset

1. Use the approved pale-green base and low-contrast repeating clover
   direction as the visual baseline.
2. Produce one small transparent tile containing the selected clover motif.
3. Keep the tile free of text, borders, avatars, or other UI elements.
4. Inspect the tile at its intended repeat size and at 2x display scale to
   catch jagged edges, visible seams, or excessive contrast.
5. Add the final asset under the project's existing resource directory with a
   descriptive name such as `clover-pattern.png`.

### Phase 2: Integrate the background into the current layout

1. Keep the existing fixed yellow header and bottom input row unchanged.
2. Apply the green base and repeating tile to the `ScrollPane` viewport, not
   to individual dialogue rows.
3. Keep `dialogContainer` transparent so the viewport background remains
   visible behind messages.
4. Ensure the pattern belongs to the viewport layer, so it stays stationary
   while the conversation content scrolls over it.
5. Keep the background layer non-interactive and ensure it does not consume
   mouse or keyboard events.
6. Use CSS classes and resource URLs rather than embedding a large image in
   Java code.

### Phase 3: Protect existing visual hierarchy

1. Keep all outer chatbot bubbles opaque enough that the pattern does not
   bleed through their semantic colours.
2. Keep user bubbles light blue and success bubbles clover green; do not
   recolour them to match the new background.
3. Keep task panels dark charcoal with warm pale-yellow Roboto Mono text.
4. Reduce pattern visibility before changing any established message colour
   if contrast is insufficient.
5. Verify that speaker labels and avatars remain easy to distinguish from the
   green background.

### Phase 4: Verify responsive and scroll behaviour

1. Inspect the preferred 400 by 600 window size.
2. Inspect the minimum 320 by 480 window size.
3. Resize horizontally and vertically to confirm that the pattern has no
   stretching artifacts or exposed seams.
4. Populate enough messages to activate scrolling and confirm that the
   pattern remains continuous and viewport-relative.
5. Check lists containing short tasks, long wrapped tasks, completed tasks,
   and mixed task types.
6. Check neutral, success, information, warning, system-error, and user
   bubbles against the pattern.

### Phase 5: Add regression coverage and documentation

1. Add a stable background ID or style class to the viewport/background node.
2. Verify through GUI tests that the background resource is packaged and the
   background layer is present behind the conversation.
3. Keep assertions structural rather than pixel-based, since exact rendering
   can vary between JavaFX environments.
4. Review `test/gui-test-plan.md` and add the new background test case.
5. Run the Java 25 unit tests, GUI tests, CLI UI tests, Checkstyle, Javadocs,
   and code-quality review.
6. Perform the manual resize, scrolling, contrast, and fallback checks before
   marking the increment complete.

### Phase 6: Handle asset failure safely

1. Make the plain green base colour the first fallback.
2. If the tile cannot be loaded, allow the application to start without the
   pattern rather than throwing an exception.
3. Keep a diagnostic message available for developers without exposing an
   alarming error to normal users.
4. Add a test or resource-level check for the normal packaged-asset path.

The normal packaged asset path is covered by
`LuckyNoGuiTest.luckyNoGui_resourcePaths_areAvailable`; the CSS base colour
remains visible if the decorative image is unavailable.

## Typography and contrast

- Keep Patrick Hand for ordinary interface and conversation text.
- Keep Roboto Mono and warm pale-yellow text for task-list panels.
- Do not place clovers directly beneath small speaker labels or dense task
  text if the pattern reduces readability.
- If contrast testing identifies a problem, reduce pattern opacity before
  changing established message colours.
- Preserve the existing semantic colour distinctions between response types.

## Suggested JavaFX structure

The conversation region should conceptually become:

```text
Conversation viewport
├── repeating clover background layer
└── existing dialogContainer
    └── dialogue rows and message bubbles
```

The background layer must be non-interactive and remain behind
`dialogContainer`. Stable CSS classes or IDs should be used for the background
container so GUI tests can verify its presence without pixel-coordinate
assertions.

Suggested additions:

- `.conversation-background`
- `.clover-pattern`
- optional `#conversationBackground`

## Acceptance criteria

- The conversation area has a visibly green-based background.
- A subtle repeating clover pattern is visible across the conversation area.
- The pattern is continuous when the window is resized or the conversation is
  scrolled.
- The yellow branded header remains unchanged and fixed above the conversation.
- User bubbles, semantic chatbot bubbles, and dark task panels remain clearly
  readable and visually distinct.
- The pattern does not appear inside message bubbles or task panels.
- The pattern layer does not intercept user input or affect scrolling.
- The app remains usable at both preferred and minimum window sizes.
- A missing decorative asset falls back safely without preventing startup.
- CLI output and chatbot behaviour remain unchanged.
- GUI tests verify the background layer's structure and configured resources.
- The GUI test plan records the new background behaviour.

## Implementation checklist

- [x] Confirm the base green and pattern-green colour values.
- [x] Confirm the pattern density and visual opacity.
- [x] Choose the deterministic clover illustration asset.
- [x] Add the transparent tile to the bundled image resources.
- [x] Apply the green base and tile to the conversation viewport.
- [x] Keep the header, message bubbles, task panels, avatars, fonts, and
  controls unchanged.
- [x] Add structural GUI and resource regression coverage.
- [x] Update the GUI test plan.
- [x] Run Java 25 tests, GUI tests, CLI UI tests, Checkstyle, Javadocs, and
  the code-quality review.
- [ ] Perform manual visual checks while scrolling and resizing.
