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
- [x] Review and commit the completed improvement.

## Improvement 2: Parser and duration edge cases

- [x] Review command-parser, task-number, and parser-to-command coverage against the specification.
- [x] Add exact-message coverage for malformed duration inputs and parser-level numeric overflow.
- [x] Add fixed-clock date/time boundary coverage with named parameterized cases.
- [x] Verify duration arithmetic's calendar-first behavior at month-end and leap-year boundaries.
- [x] Run JUnit, `clitest`, GUI tests, Checkstyle, Javadoc, `check`, and the 25-case UI plan.
- [x] Keep arbitrary repeated `next` modifiers supported, subject to date-time limits.
- [x] Define the approved dedicated message for arithmetic snooze overflow.
- [x] Implement and test the arithmetic snooze-overflow message in a separate production commit.
- [x] Review and commit the completed improvement after the production follow-up.

## Improvement 3: Task-model invariants

- [x] Expand `TaskTimes` invariant coverage.
- [x] Expand task status-transition coverage.
- [x] Expand capacity and invalid-index coverage.
- [x] Assert exact exception messages for task-model validation failures.
- [x] Apply the clarified implicit-trimming policy outside whitespace-focused tests.
- [x] Run JUnit, `clitest`, GUI tests, Checkstyle, Javadoc, `check`, and the 25-case UI plan.

## Improvement 4: Persistence and rollback

- [x] Expand mutation rollback coverage for save failures.
- [x] Expand reload-after-save coverage.
- [x] Expand malformed and oversized CSV coverage.
- [x] Include syntactically malformed CSV and semantically invalid records.
- [x] Compare persisted task records rather than raw file bytes.
- [x] Add a bounded physically large CSV stress test.
- [ ] Run JUnit, `clitest`, GUI tests, Checkstyle, Javadoc, `check`, and the 25-case UI plan.
- [ ] Review and commit the completed improvement separately.

## Improvement 5: Chatbot startup and error paths

- [x] Test invalid fixed-clock configuration.
- [x] Test startup load failures.
- [x] Test CLI exhaustion versus explicit `bye`.
- [x] Test GUI-facing save and parse error responses.
- [x] Use an isolated subprocess probe for GUI save failures.
- [x] Add the CLI end-of-file scenario to `test/ui-test-plan.md`.
- [ ] Run JUnit, `clitest`, GUI tests, Checkstyle, Javadoc, `check`, and the UI plan.
- [x] Draft and review the Increment 5 commit message.

## Improvement 6: Deterministic GUI automation

- [x] Automate conversation scrolling behavior.
- [x] Automate goodbye visibility and delayed exit behavior.
- [x] Automate responsive resizing behavior where reliable.
- [x] Automate GUI persistence across relaunches.
- [x] Keep cross-platform, resolution, and OS-locale checks documented as manual acceptance checks.
- [ ] Run the full verification pipeline and resolve or report any discrepancies.

## Improvement 7: Shared test infrastructure and coverage

- [x] Generalize date/time normalization across other tests without weakening exact assertions.
- [x] Add a standalone aggregate JaCoCo measurement task.
- [x] Record provisional line, branch, and method coverage in `docs/testing-coverage.md`.
- [x] Keep the coverage report independent from `check`.
- [x] Keep coverage report-only without enforcing thresholds.
- [ ] Resolve the known Increment 4 discrepancies and refresh the coverage baseline.

## Improvement 9: Final coverage baseline and policy

- [x] Set the optional line, branch, and method thresholds to 90%, 80%, and 95%.
- [x] Add the separate aggregate `coverageVerification` task.
- [x] Keep `coverageVerification` independent from `check`.
- [x] Keep the GitHub Actions workflow unchanged.
- [ ] Resolve Increment 8 and generate the final coverage baseline normally.
- [ ] Update coverage documentation with the final baseline commit and figures.
