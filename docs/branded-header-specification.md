# LuckyNoSlacky Branded Header Specification

## Purpose

Add a persistent branded header to the JavaFX interface so that LuckyNoSlacky
is identifiable before the first message is read. The header should reinforce
the existing Pineapple Pop, clover-green, and blue-accent palette without
competing with the conversation area.

This increment is presentational only. It must not change command parsing,
response wording, task behaviour, persistence, or CLI output.

## Approved design direction

Use a compact fixed header above the conversation area:

```text
┌──────────────────────────────────────────────┐
│  (LuckyNoSlacky avatar)  LuckyNoSlacky       │
│                           Your lucky task     │
│                           buddy 🍀            │
├──────────────────────────────────────────────┤
│              scrolling conversation           │
│                                              │
├──────────────────────────────────────────────┤
│  Enter command...                    [Send]   │
└──────────────────────────────────────────────┘
```

The approved default is a primarily pineapple-yellow header with subtle cream
and clover-green details. This keeps the header recognisable while leaving the
conversation background and semantic message colours visually distinct.

The proposed header background token is `#F6D365`, a light pineapple yellow
that can carry the header area without becoming too visually heavy. This is a
header-specific shade; the existing `#D38B08` primary pineapple remains an
accent colour for text and focus states.

A future background-focused increment may introduce a custom green-based
background with a repeating `🍀` pattern. See
`docs/clover-pattern-background-specification.md`. That pattern is explicitly
out of scope for this header increment.

## Functional requirements

### Persistent placement

- Place the header outside the `ScrollPane` so it remains visible while the
  conversation scrolls.
- Keep the command input row outside the `ScrollPane` at the bottom.
- Allow only the conversation area to expand and scroll vertically.
- Preserve the existing 400 by 600 preferred window size and 320 by 480
  minimum window size.

### Header content

The header must contain:

- the supplied `luckynoslacky.jpg` avatar asset;
- the title `LuckyNoSlacky`;
- the tagline `Your lucky task buddy`;
- a visible clover or clover-colour accent that connects the header to the
  success-response treatment.

The header is informational and must not introduce a new command, clickable
action, or navigation behaviour.

### Avatar usage

- Reuse the supplied chatbot PNG/JPG asset already used by conversation rows.
- Keep the avatar circular and preserve its aspect ratio.
- Use a compact header size, proposed as 52 to 56 pixels.
- Retain the existing subtle shadow.
- Do not add an outline unless visual review shows that the avatar needs more
  separation from the header background.

## Visual requirements

### Typography

- Use Patrick Hand for the title and tagline.
- Keep the title visually stronger through size and weight rather than a
  different typeface.
- Do not use Roboto Mono in the header; that typeface remains reserved for
  task-list content.
- The title and tagline must remain readable at the minimum window width.

### Colour treatment

Use the existing palette tokens wherever possible:

| Element | Recommended treatment |
| --- | --- |
| Header background | Pineapple yellow `#F6D365` |
| Header title | Dark pineapple/brown for contrast, proposed `#8A5300` |
| Header tagline | Main text `#3D2B1F` or muted clover green |
| Header accent | Lucky green `#2F855A` |
| Divider or border | Muted pineapple-yellow or light clover green |
| Avatar | Supplied asset with existing shadow |

Use cream and green for contrast so the primarily yellow header remains
readable and does not merge visually with the neutral chatbot bubble.

### Spacing and layout

- Provide enough horizontal padding for the avatar and text to breathe.
- Keep the avatar and text vertically centred.
- Use a small, consistent gap between the avatar, title, and tagline.
- Keep the header height compact so it does not reduce the conversation area
  excessively.
- If the tagline wraps at narrow widths, it must remain inside the header and
  must not overlap the avatar or input controls.

## Suggested JavaFX structure

Restructure the root layout into three vertical regions:

```text
AnchorPane or VBox root
├── header container
├── conversation ScrollPane
└── input controls
```

The existing `dialogContainer` must remain the content of the `ScrollPane`.
The header should use stable IDs and CSS classes so GUI tests can locate the
title, tagline, and avatar without coordinate-based assertions.

Suggested IDs/classes:

- `#brandHeader`
- `#brandAvatar`
- `#brandTitle`
- `#brandTagline`
- `.brand-header`
- `.brand-title`
- `.brand-tagline`

## Acceptance criteria

- A persistent header is visible when the application starts.
- The header shows the supplied LuckyNoSlacky avatar, title, and tagline.
- The header remains visible while conversation messages scroll.
- The title and tagline use Patrick Hand.
- Roboto Mono remains limited to task-list content.
- The header fits at the minimum window dimensions without clipping or overlap.
- The existing conversation, semantic response tones, task panels, input field,
  and Send button retain their current appearance and behaviour.
- The header does not change any CLI output or command behaviour.
- GUI regression tests verify the header's observable nodes and content.
- The GUI test plan documents the new header behaviour.

## Implementation checklist

- [x] Confirm the final title and tagline copy: `Your lucky task buddy`.
- [x] Confirm the primarily pineapple-yellow header background and accent
  treatment.
- [x] Confirm that the header stays fixed above the scrolling conversation.
- [ ] Confirm the exact header yellow `#F6D365` and whether the clover accent
  is a literal `🍀` or a green decorative detail.
- [x] Add the fixed header region above the conversation `ScrollPane`.
- [x] Add the supplied chatbot avatar to the header.
- [x] Add title, tagline, stable IDs, and CSS classes.
- [x] Apply Patrick Hand and the existing brand palette.
- [ ] Verify responsive behaviour at preferred and minimum window sizes.
- [x] Add GUI regression tests for header presence, text, avatar, and layout
  structure.
- [x] Update `test/gui-test-plan.md`.
- [x] Run Java 25 tests, GUI tests, UI tests, Checkstyle, Javadocs, and the
  code-quality review.
- [ ] Perform a manual visual review while scrolling and resizing the window.
