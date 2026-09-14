# LuckyNoSlacky Personality Improvements

This checklist tracks the planned GUI and response-personality improvements.

## Progress

- [x] Replace the generic interface font with bundled Patrick Hand.
- [x] Apply the unified handwritten font to chatbot messages, user messages,
  speaker labels, the input field, and the Send button.
- [x] Render task lines such as `[T] [ ] read book` with bundled Roboto Mono.
- [x] Display task lines in dark code-style blocks.
- [x] Include the selected font licenses with the bundled resources.
- [x] Add GUI regression coverage and update `test/gui-test-plan.md`.
- [x] Run Java 25 tests, GUI tests, UI tests, Checkstyle, and Javadoc.

## Future improvements

- [x] Strengthen the brand palette with pineapple yellow, clover green, and
  blue accents for the user and Send button.
- [x] Add semantic response tones for neutral, success, information, warning,
  and system-error chatbot messages.
- [x] Keep each LuckyNoSlacky response in one outer message bubble while
  rendering contiguous task output in a single dark Roboto Mono panel with
  warm pale-yellow text. See `docs/task-list-rendering-specification.md`.
- [x] Add a branded header or title area with the avatar and a tagline. See
  `docs/branded-header-specification.md`.
- [x] Add a custom green-based background with a repeating `🍀` pattern.
  See `docs/clover-pattern-background-specification.md`. The approved mockup
  covers the background treatment only; the rest of the current chatbot UI
  remains unchanged. Manual visual acceptance remains after automated checks.
- [ ] Improve the user avatar and add stronger chatbot avatar treatment.
- [ ] Review response wording for grammar and voice consistency.
- [ ] Keep teasing and error messages friendly and non-hostile.
- [ ] Update relevant tests and test-plan documentation after each future
  behavior change.

## Notes

Task-line styling currently recognizes the existing `[T]`, `[D]`, and `[E]`
display markers. The next task-list rendering improvement must extend this to
optional numeric prefixes and group adjacent task lines before applying the
Roboto Mono styling.
