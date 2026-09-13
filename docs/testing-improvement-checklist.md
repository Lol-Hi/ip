# Testing improvement checklist

## Improvement 1: Automated CLI regression suite

- [x] Create Java CLI fixture representation.
- [x] Link fixtures to the corresponding Markdown test-plan cases.
- [x] Run cases in isolated temporary working directories.
- [x] Apply Java 25 and deterministic clock settings.
- [x] Normalize platform-dependent date and time names.
- [x] Compare output strictly apart from documented normalization.
- [x] Stop at the first failed CLI case.
- [x] Print the console input and output record.
- [x] Add the `clitest` Gradle task.
- [x] Make `check` depend on `clitest`.
- [x] Update `test/ui-test-plan.md`.
- [x] Run all 25 documented CLI cases successfully.
- [x] Run unit tests, GUI tests, Checkstyle, and Javadoc successfully.
- [ ] Review and commit the completed improvement.

## Improvement 2: Parser and duration edge cases

- [ ] Add parser edge-case coverage.
- [ ] Add duration validation and overflow coverage.
- [ ] Add date/time boundary coverage.

## Improvement 3: Task-model invariants

- [ ] Expand `TaskTimes` invariant coverage.
- [ ] Expand task status-transition coverage.
- [ ] Expand capacity and invalid-index coverage.

## Improvement 4: Persistence and rollback

- [ ] Expand mutation rollback coverage for save failures.
- [ ] Expand reload-after-save coverage.
- [ ] Expand malformed and oversized CSV coverage.

## Improvement 5: Chatbot startup and error paths

- [ ] Test invalid fixed-clock configuration.
- [ ] Test startup load failures.
- [ ] Test CLI exhaustion versus explicit `bye`.
- [ ] Test GUI-facing save and parse error responses.

## Improvement 6: Deterministic GUI automation

- [ ] Automate conversation scrolling behavior.
- [ ] Automate goodbye visibility and delayed exit behavior.
- [ ] Automate responsive resizing behavior where reliable.
- [ ] Automate GUI persistence across relaunches.
- [ ] Keep cross-platform, resolution, and OS-locale checks documented as manual acceptance checks.

## Improvement 7: Shared test infrastructure and coverage

- [ ] Generalize date/time normalization across other tests.
- [ ] Add JaCoCo measurement after expanding the test suite.
- [ ] Record updated line, branch, and method coverage.
- [ ] Decide whether coverage thresholds are appropriate.
