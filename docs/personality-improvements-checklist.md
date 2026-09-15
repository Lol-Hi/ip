# LuckyNoSlacky Personality Improvements

This checklist tracks the approved personality visual resources and the
handoffs required for controlled integration. It follows Specification B:
`branch-personality` owns visual resources and documentation, while
`branch-BetterGui` owns shared GUI behaviour and response content.

## Contract status

- [ ] Complete the branch cleanup before integration. The existing historical
  increment contains shared GUI, response, and test changes that must be
  extracted or reverted on this branch. See
  `docs/personality-branch-cleanup-specification.md`.
- [ ] Keep response wording, tone classification, command mapping, JavaFX
  behaviour, and canonical GUI-test-plan changes on `branch-BetterGui`.
- [ ] Integrate only approved personality resources into BetterGUI; do not
  merge this branch wholesale into `master`.

## Progress

- [x] Replace the generic interface font with bundled Patrick Hand.
- [x] Apply the unified handwritten font to chatbot messages, user messages,
  speaker labels, the input field, and the Send button.
- [x] Render task lines such as `[T] [ ] read book` with bundled Roboto Mono.
- [x] Display task lines in dark code-style blocks.
- [x] Include the selected font licenses with the bundled resources.
- [x] Define the visual acceptance criteria and resource requirements.
- [ ] Add resource-only availability checks, if required.

## Future improvements

- [x] Define the brand palette with pineapple yellow, clover green, and blue
  accents for the user and Send button. See
  `docs/brand-palette-specification.md`.
- [x] Define visual tokens for neutral, success, information, warning, and
  system-error response roles. BetterGUI owns the semantic mapping.
- [x] Specify one outer message bubble with contiguous task output in a dark
  Roboto Mono panel and warm pale-yellow text. See
  `docs/task-list-rendering-specification.md`.
- [x] Specify the branded header with the supplied avatar and tagline. See
  `docs/branded-header-specification.md`.
- [x] Specify the custom green-based background with a repeating `🍀` pattern.
  See `docs/clover-pattern-background-specification.md`.
- [ ] Package the approved visual resources in new personality-owned files,
  including scoped `personality.css` selectors and any standalone FXML/assets.
- [x] Define and package the user/avatar treatment visual contract as a
  resource-only increment. See `docs/avatar-treatment-specification.md`.
- [ ] Apply the avatar treatment in BetterGUI's shared dialogue structure
  after controlled integration.
- [ ] Handoff the reviewed response wording to the BetterGUI/content owner;
  it is not a `branch-personality` implementation increment. See
  `docs/response-personality-message-review.md`.
- [ ] Execute the branch cleanup and prepare the controlled integration
  handoff. See `docs/personality-branch-cleanup-specification.md`.
- [ ] Let BetterGUI update functional tests and the canonical GUI test plan
  after shared integration.

## Notes

Task-list grouping, response construction, semantic tone mapping, and
functional GUI tests are BetterGUI responsibilities. Personality supplies the
approved visual selectors and requirements only. The response wording review
is likewise a handoff draft and must be implemented outside this branch.
