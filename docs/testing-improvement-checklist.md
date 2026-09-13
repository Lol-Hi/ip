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

- [x] Review command-parser, task-number, and parser-to-command coverage against the specification.
- [x] Add exact-message coverage for malformed duration inputs and parser-level numeric overflow.
- [x] Add fixed-clock date/time boundary coverage with named parameterized cases.
- [x] Verify duration arithmetic's calendar-first behavior at month-end and leap-year boundaries.
- [x] Run JUnit, `clitest`, GUI tests, Checkstyle, Javadoc, `check`, and the 25-case UI plan.
- [x] Keep arbitrary repeated `next` modifiers supported, subject to date-time limits.
- [x] Define the approved dedicated message for arithmetic snooze overflow.
- [ ] Implement and test the arithmetic snooze-overflow message in a separate production commit.
- [ ] Review and commit the completed improvement after the production follow-up.

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
