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

- [ ] Strengthen the brand palette using warmer yellow/orange accents.
- [ ] Add a branded header or title area with the avatar and a tagline.
- [ ] Improve the user avatar and add stronger chatbot avatar treatment.
- [ ] Review response wording for grammar and voice consistency.
- [ ] Keep teasing and error messages friendly and non-hostile.
- [ ] Update relevant tests and test-plan documentation after each future
  behavior change.

## Notes

Task-line styling currently recognizes the existing `[T]`, `[D]`, and `[E]`
display markers. If task formatting changes later, centralize that display
format before updating the GUI detector.
