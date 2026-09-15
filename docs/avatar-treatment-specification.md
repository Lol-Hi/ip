# LuckyNoSlacky Avatar Treatment Specification

## Purpose

Improve speaker recognition and brand personality by giving the two existing
avatar assets distinct, consistent visual treatments. The chatbot and user
should be identifiable at a glance without changing the approved conversation
layout, typography, colour palette, clover background, or message rendering.

## Current state

The GUI already uses the supplied project assets:

| Role | Source asset | Current conversation size | Current treatment |
| --- | --- | ---: | --- |
| LuckyNoSlacky | `src/main/resources/images/luckynoslacky.jpg` | 45px | Circular crop and shared drop shadow |
| User | `src/main/resources/images/user.png` | 45px | Circular crop and shared drop shadow |
| LuckyNoSlacky header | Same chatbot asset | 56px | Circular crop and header drop shadow |

The current implementation creates an `ImageView` directly inside each
`DialogueBox`. The same `.avatar` style class is applied to both roles, so
there is no visual distinction beyond the speaker label and message alignment.

## Goals

- Make chatbot and user messages easier to distinguish visually.
- Use the two supplied image assets exactly as the avatar content.
- Keep avatar sizing, clipping, alignment, and spacing consistent.
- Make the chatbot treatment reinforce the pineapple-yellow and clover-green
  brand palette.
- Make the user treatment reinforce the light-blue user-message palette.
- Keep the header avatar visually related to the conversation chatbot avatar.
- Keep the treatment decorative and non-interactive.

## Non-goals

This increment must not:

- generate replacement avatars or alter the supplied PNG/JPEG files;
- change the Patrick Hand or Roboto Mono typography;
- change message bubble colours, task-list panels, or response wording;
- change the clover background pattern;
- alter message alignment, scrolling, input behaviour, or persistence;
- add hover actions, profile menus, status indicators, or user controls.

## Branch ownership boundary

This is a visual resource and integration-handoff specification. Personality
may define the supplied asset paths, ring tokens, scoped selectors, mockups,
and desired node structure. It must not modify `DialogueBox`, `MainWindow`,
shared semantic CSS, accessibility or input behaviour, functional tests, or
the canonical `test/gui-test-plan.md`. The BetterGUI integration owner will
wrap the existing images and apply the selectors on `branch-BetterGui`.

## Recommended visual direction

Use a role-specific avatar frame around each existing image:

```text
Chatbot dialogue                         User dialogue

  [ orange / green frame ]                 [ light-blue frame ]
  [ supplied chatbot image ]               [ supplied user image ]
  LuckyNoSlacky said:                      You said:
  [ pineapple response bubble ]             [ light-blue bubble ]
```

The avatar image remains the focal point. The frame should be thin enough to
avoid looking like a second message bubble, while the role colours provide a
quick visual cue when scanning a long conversation.

## Approved creative direction

The selected treatment for this increment is:

- User conversation avatars use a light-blue ring.
- LuckyNoSlacky conversation avatars use a two-tone ring with a pineapple-
  orange outer ring and clover-green inner accent.
- Both conversation roles use ring-only treatment; no clover badge is added.
- Conversation avatars grow from 45px to 48px.
- The 56px header avatar retains its current standalone appearance without
  the conversation frame treatment.

The header continues to use the same supplied chatbot asset, but its current
yellow-header presentation remains intentionally separate from the dialogue
avatar treatment.

## Proposed visual tokens

These are the approved starting values for implementation and visual review:

| Token | Proposed value | Use |
| --- | --- | --- |
| Conversation avatar size | `48px` | Slightly larger approved dialogue avatar |
| Header avatar size | `56px` | Preserve the current branded header |
| Chatbot outer ring | `#D38B08` pineapple orange | Primary LuckyNoSlacky ring |
| Chatbot inner accent | `#2F855A` clover green | Second ring; no badge |
| User frame | `#93C5FD` light blue | Matches the user bubble border |
| User accent | Existing soft shadow | Keeps the treatment visually light |
| Ring thickness | `2px` outer, `1px` inner accent | Role-specific outline |
| Shared frame radius | `50%` | Circular treatment |
| Shared shadow | Existing soft shadow | Retain current depth and separation |

No frame should reduce the visible image area enough to make either supplied
asset difficult to recognize.

## Creative decisions recorded

The requested choices have been confirmed:

1. Use a light-blue ring for the supplied user avatar.
2. Use a two-colour pineapple-orange and clover-green treatment for the
   supplied LuckyNoSlacky avatar.
3. Keep the treatment ring-only, without a clover badge.
4. Increase conversation avatars to 48px.
5. Retain the header avatar's current standalone appearance.

## Functional requirements

### Asset integrity

- Load the chatbot avatar only from
  `/images/luckynoslacky.jpg`.
- Load the user avatar only from `/images/user.png`.
- Do not edit, recolour, redraw, or replace either source asset.
- Keep the existing transparent areas and image details visible as supplied.
- Keep the normal resource-path tests for both assets.

### Consistent geometry

- Use one shared circular clipping rule for conversation avatars.
- Keep the conversation avatar's effective outer size stable across roles.
- Keep the standalone header avatar at its existing 56px size and fixed header
  position.
- Preserve the current 8px gap between an avatar and its message container.
- Keep left/right avatar placement tied to the existing speaker alignment.
- Ensure the frame does not intercept mouse or keyboard input.

### Role-specific appearance

- Apply a chatbot-specific CSS class to LuckyNoSlacky dialogue avatars.
- Apply a user-specific CSS class to user dialogue avatars.
- Keep the avatar role class independent from semantic response-tone classes.
- Ensure success, information, warning, and error responses do not remove the
  chatbot identity treatment.
- Ensure the light-blue user frame remains distinct from the blue user bubble.

### Header relationship

- Continue using the supplied chatbot image in the branded header.
- Keep the yellow header, title, tagline, and clover accent unchanged.
- Retain the header avatar's current standalone circular presentation and
  shadow without adding the conversation rings.
- Do not move the header or make it part of the scrolling conversation.

## Suggested integration structure

Because JavaFX `ImageView` is not a `Region`, a CSS border is not a reliable
way to draw the frame directly on the image. The BetterGUI integration should
use a small non-interactive container around the existing `ImageView`:

```text
DialogueBox
├── avatarFrame (StackPane, role-specific class)
│   └── avatarAccent (StackPane, chatbot only)
│       └── avatarImage (ImageView, supplied asset, circular clip)
└── messageContainer
```

The frame should own the outer ring, background, padding, and shadow. The
chatbot-only accent container should own the inner green ring. The child image
should own the fixed dimensions, preserve-ratio setting, and circular clip.
This keeps the role treatment reliable across JavaFX platforms and avoids
embedding styling decisions in image files. The structure is a handoff
contract; it is not a request to change `DialogueBox` on this branch.

Suggested classes:

- `.personality-avatar-frame`
- `.personality-chatbot-avatar-frame`
- `.personality-chatbot-avatar-accent`
- `.personality-user-avatar-frame`
- `.personality-avatar-image`
- The existing `.brand-avatar` class remains unchanged because the header
  treatment is intentionally standalone.

## Resource-package and integration plan

### Phase 1: Confirm creative direction

1. Keep the five recorded creative decisions as the implementation baseline.
2. Keep the two existing supplied assets as immutable inputs.
3. Preserve the standalone header avatar and its current header styling.
4. Keep the selected frame colours recorded in this specification.

### Phase 2: Package the visual contract

1. Add the approved ring tokens and role-specific selectors to the
   personality-owned stylesheet.
2. Document the frame-and-image hierarchy for the BetterGUI integration owner.
3. Preserve the existing constructor contract, speaker alignment, and
   circular clipping as integration constraints.
4. Keep the branded header avatar structure unchanged.
5. Do not edit `DialogueBox` or any shared layout file on
   `branch-personality`.

### Phase 3: Describe integration verification

1. Add shared frame geometry and the existing soft shadow to the scoped
   personality CSS.
2. Define chatbot frame colours using the approved orange/green decision.
3. Define user frame colours using the approved blue decision.
4. Document that the integration must preserve message wrapping and row
   spacing.
5. Document the 320px minimum-width visual check for the integration owner.

### Phase 4: Resource checks and BetterGUI handoff

1. Add only resource-level checks on this branch, such as the availability of
   the two supplied avatar files and the personality stylesheet.
2. Hand the frame hierarchy and role-selector requirements to the BetterGUI
   owner.
3. The BetterGUI owner updates `DialogueBoxTest` and any GUI regression tests
   after integrating the shared Java node structure.
4. Keep integrated assertions structural rather than pixel-based.

### Phase 5: Verify after controlled integration

1. After integration, the BetterGUI owner runs the Java 25 unit suite and
   dedicated GUI suite.
2. The BetterGUI owner runs Checkstyle, Javadocs, and the documented CLI UI
   plan when shared Java code changes.
3. The BetterGUI owner reconciles `test/gui-test-plan.md` during integration.
4. Inspect short and long messages, task lists, all semantic response tones,
   and both conversation alignments.
5. Resize to 320×480 and 400×600 and verify that the layout remains usable.
6. Scroll a long conversation and verify that avatar frames remain aligned.

## Acceptance criteria

- The supplied chatbot and user assets remain the displayed avatar images.
- Chatbot and user avatars have clearly distinguishable role treatments.
- The selected frame style is consistent between equivalent avatar instances.
- Circular clipping, spacing, alignment, and message wrapping remain usable.
- The header remains fixed, yellow, and visually consistent with its approved
  branding, including its standalone avatar appearance.
- Avatar decoration does not appear inside message bubbles or task panels.
- Avatar frames do not intercept input or affect scrolling.
- All semantic chatbot response tones retain the chatbot avatar treatment.
- The app remains usable at the preferred and minimum window sizes.
- Resource checks verify the packaged assets and scoped selectors; BetterGUI
  GUI tests verify the integrated frame hierarchy and role classes.
- CLI output, commands, persistence, and chatbot behavior remain unchanged.

## Implementation checklist

- [x] Decide the user avatar frame treatment: light-blue ring.
- [x] Decide the chatbot avatar frame treatment: two-colour orange/green ring.
- [x] Decide to keep the treatment ring-only without a clover badge.
- [x] Increase conversation avatars from 45px to 48px.
- [x] Retain the header avatar's current standalone appearance.
- [x] Record the selected colour tokens and decisions in this specification.
- [x] Add the approved avatar tokens and scoped role selectors to the
  personality stylesheet.
- [ ] Document the non-interactive frame hierarchy for the BetterGUI owner.
- [ ] Verify that the supplied avatar assets remain available as resources.
- [ ] Let BetterGUI wrap the images and apply the selectors on
  `branch-BetterGui`.
- [ ] Let BetterGUI update GUI regression tests and reconcile
  `test/gui-test-plan.md` during controlled integration.
- [ ] Run full validation after integration, including Java 25 tests, GUI
  tests, UI tests, Checkstyle, Javadocs, and the code-quality review.
- [ ] Perform manual visual checks at preferred/minimum sizes and while
  scrolling after integration.
