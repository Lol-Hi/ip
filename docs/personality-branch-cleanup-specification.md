# `branch-personality` Ownership Cleanup Specification

## Increment name

**Personality Branch Ownership Cleanup and BetterGUI Integration Handoff**

## Objective

Make `branch-personality` independently mergeable under Specification B by
retaining only personality-owned visual resources and documentation. Shared
GUI behavior, response content, semantic tone handling, and functional test
changes must be returned to the BetterGUI integration workstream instead of
being merged wholesale from this branch.

This is a branch-hygiene increment. It does not introduce new user-facing
behavior.

## Audit finding

The current branch is not yet compliant with Specification B. Its diff from
`master` contains 23 files, including shared production Java, shared CSS and
FXML, functional tests, and the canonical GUI test plan. Those changes overlap
with BetterGUI ownership and must be isolated before integration.

The existing history is useful as design and implementation reference, but a
completed specification does not make the already-committed shared-file hunks
compliant. The cleanup must resolve the file ownership in the branch itself.

## Repository relationship and sequencing decision

At the time of this specification:

- `master` and both workstreams share `d9c5ebb` as their common base;
- `branch-personality` is at `59e73eb`;
- `branch-BetterGui` is at `4d3fccc` and already contains stable personality
  integration hooks.

The branches must not be synchronised by merging the current
`branch-personality` wholesale into `branch-BetterGui`, because that would
also import the personality branch's forbidden shared-file changes. They also
should not wait until every future personality idea is complete before any
integration; that would preserve a large, conflict-prone batch.

The recommended sequencing is:

1. Complete this cleanup increment on `branch-personality`.
2. Verify that the cleaned branch contains only approved resources and
   documentation.
3. Selectively incorporate the clean resource package into
   `branch-BetterGui`, using the existing integration hooks as the destination.
4. Let BetterGUI apply the shared Java, FXML, semantic CSS, and test changes,
   then run full validation there.
5. Continue later personality work only as resource-only increments. Each new
   resource package should be selectively incorporated into BetterGUI rather
   than merged wholesale.
6. Merge only the fully validated `branch-BetterGui` into `master`.

In other words, clean first, integrate the current package early, and then
deliver later visual resources incrementally. There is no need to merge
`branch-BetterGui` back into `branch-personality` merely to synchronise the
branches.

## Allowed personality deliverables

After cleanup, the branch may contain only approved visual resources and
documentation, such as:

- `src/main/resources/css/personality.css`;
- `src/main/resources/view/BrandHeader.fxml`;
- `src/main/resources/fonts/*`, including required license files;
- `src/main/resources/images/clover-pattern.png` and other approved visual
  assets;
- mockups, visual specifications, and integration handoff documents under
  `docs/`;
- a dedicated resource-availability test that does not construct or alter
  shared GUI behavior, if such a test is necessary.

The personality stylesheet must use scoped selectors such as
`.brand-header`, `.brand-title`, `.brand-tagline`,
`.personality-background`, `.personality-task-block`, and
`.personality-task-content`. It must not redefine BetterGUI semantic classes
directly.

## Changes to remove from this branch's ownership

The following files are shared or behavior-owning and must not remain changed
by the personality workstream:

- `src/main/java/luckynoslacky/LuckyNoSlacky.java`;
- `src/main/java/luckynoslacky/luckyui/gui/DialogueBox.java`;
- `src/main/java/luckynoslacky/luckyui/gui/LuckyNoGui.java`;
- `src/main/java/luckynoslacky/luckyui/gui/MainWindow.java`;
- `src/main/resources/css/dialogue-box.css`;
- `src/main/resources/css/main.css`;
- `src/main/resources/view/MainWindow.fxml`;
- `src/test/java/luckynoslacky/LuckyNoSlackyTest.java`;
- `src/test/java/luckynoslacky/luckyui/gui/DialogueBoxTest.java`;
- `src/test/java/luckynoslacky/luckyui/gui/LuckyNoGuiTest.java`;
- `src/test/java/luckynoslacky/luckyui/gui/MainWindowTest.java`;
- `test/gui-test-plan.md`.

The response wording draft at
`docs/response-personality-message-review.md` is retained as a manual review
handoff only. It must not be used to modify response-producing classes on this
branch.

## Cleanup approach

### 1. Classify the existing diff

Compare `branch-personality` with its intended integration base and classify
every changed path as one of:

1. retain as a personality resource or documentation file;
2. extract into a new personality-owned resource file;
3. revert or remove from this branch and hand off the behavior requirement to
   `branch-BetterGui`.

Do not resolve this by merging the whole branch into `master`.

### 2. Isolate visual resources

Retain or create only new, independently consumable resource files:

- move the approved palette, typography, background, task-panel, and avatar
  selectors into `src/main/resources/css/personality.css`;
- retain the bundled font files and licenses;
- retain the supplied-avatar references and clover-pattern asset without
  replacing or editing the source avatar images;
- add `BrandHeader.fxml` as a standalone component rather than editing
  `MainWindow.fxml`;
- keep the approved mockups and specifications under `docs/`.

The resource package must be usable by BetterGUI without requiring personality
to own response parsing, JavaFX node creation, scrolling, input handling, or
semantic response mapping.

The cleanup should create the standalone `BrandHeader.fxml` component if it is
not already present. It should also consolidate the approved avatar, palette,
task-panel, typography, and background selectors in `personality.css`, while
keeping the selectors scoped and leaving BetterGUI's semantic stylesheets
unchanged.

### 3. Prepare explicit handoffs

For each shared behavior that was explored in the earlier increments, record a
handoff requirement rather than retaining the code change:

- response-tone classification and mapping remain BetterGUI-owned;
- one-bubble response construction and task-list grouping remain
  BetterGUI-owned;
- header inclusion into `MainWindow.fxml` remains BetterGUI-owned;
- applying the background selector to the scrolling viewport remains
  BetterGUI-owned;
- avatar frame creation around `DialogueBox` remains BetterGUI-owned;
- response wording and friendly errors remain with the BetterGUI/content
  owner;
- GUI regression coverage and canonical GUI-plan reconciliation occur during
  controlled integration.

### 4. Restore shared files on the personality branch

Remove personality-owned hunks from shared Java, CSS, FXML, functional-test,
and canonical-plan files. The resulting branch may still contain documentation
that describes the desired integration result, but it must not contain the
implementation of that result in shared files.

This cleanup must preserve unrelated user work. Before changing a shared file,
inspect its current diff and preserve any change that is not attributable to
the personality workstream rather than using a destructive whole-file reset.

### 5. Verify branch contents

Before the cleanup increment is considered complete:

- the working tree is clean after the cleanup commit;
- there are no untracked implementation files;
- `git diff master...branch-personality --name-status` contains only approved
  personality resources and documentation;
- no changed path is a shared Java class, shared semantic stylesheet, shared
  root FXML file, functional test, or `test/gui-test-plan.md`;
- all documentation clearly labels BetterGUI integration work as a handoff;
- the response wording document is clearly marked deferred and non-implementing.

## Validation boundary

Personality-side validation is limited to resource availability and document
consistency. A resource test may verify that fonts, images, FXML, and
`personality.css` are packaged, but it must not instantiate or modify shared
GUI behavior.

The full Java 25 unit suite, GUI suite, CLI UI tests, Checkstyle, Javadocs,
code-quality review, and canonical GUI test-plan reconciliation are required
after the resources are integrated into `branch-BetterGui`. Those checks must
be run by the integration owner against the combined implementation.

## Controlled integration procedure

After the cleanup branch passes its ownership audit:

1. Confirm that both worktrees are clean and record the exact source commit.
2. Confirm that `branch-BetterGui` is clean and still contains its stable
   integration hooks.
3. Transfer only the approved resource paths, such as fonts, licenses,
   `clover-pattern.png`, `personality.css`, `BrandHeader.fxml`, and approved
   documentation. Use selective path transfer or equivalent focused commits;
   do not cherry-pick historical commits that also modify shared files.
4. On `branch-BetterGui`, connect the resources to the stable integration
   regions and apply shared behavior changes under BetterGUI ownership.
5. Resolve any shared-file conflict on `branch-BetterGui` using the conflict
   rules in Specification B: BetterGUI wins for behavior, accessibility,
   response text, and semantics; personality wins for visual tokens and
   decorative layout.
6. Run Java 25 tests, GUI tests, UI tests, Checkstyle, Javadocs, and the
   code-quality review on the integrated branch.
7. Reconcile the canonical `test/gui-test-plan.md` on `branch-BetterGui` and
   complete the visual acceptance checks.
8. Keep `branch-personality` as the source of future visual resources, but
   integrate each approved resource increment into BetterGUI before the final
   merge to `master`.

## Integration outcome

The intended flow is:

```text
clean personality resources and documentation
        ↓
controlled resource integration into branch-BetterGui
        ↓
BetterGUI applies shared Java/FXML/CSS behavior and tests
        ↓
full validation and documentation reconciliation
        ↓
branch-BetterGui → master
```

`branch-personality` must not be merged separately into `master` after its
resources have been integrated.

## Clarification questions

The following choices affect the cleanup and integration mechanics. The
recommended answers preserve the contract and minimise future conflicts:

1. **Cleanup scope:** Should the cleanup create `BrandHeader.fxml` and move
   all currently approved visual selectors into `personality.css` now?
   Recommended: yes, so the first controlled transfer is a complete resource
   package rather than a partial extraction.
2. **Integration granularity:** Should the resource package be transferred to
   `branch-BetterGui` as one focused resource-package change, with later
   visual assets transferred separately? Recommended: yes; this keeps review
   and rollback boundaries clear.
3. **Documentation destination:** Should the full visual specifications and
   mockups be transferred to BetterGUI alongside the resources, while keeping
   the response wording document explicitly marked as a deferred content
   handoff? Recommended: yes, with the response draft remaining non-
   implementing.
4. **Remaining increments:** Should future avatar application, response
   wording, and shared GUI behavior be treated as BetterGUI increments after
   this handoff? Recommended: yes; personality should continue only with
   standalone visual resources.

## Checklist

- [x] Audit the complete diff against `master`.
- [x] Separate approved assets, standalone FXML, scoped CSS, and documents
  from shared behavior changes.
- [x] Create or retain `personality.css` with only scoped selectors.
- [x] Create or retain `BrandHeader.fxml` without editing `MainWindow.fxml`.
- [x] Retain font files, licenses, supplied avatar references, and the clover
  pattern asset.
- [x] Convert response wording and shared behavior notes into explicit
  BetterGUI handoffs.
- [x] Remove personality hunks from shared Java, CSS, FXML, functional tests,
  and `test/gui-test-plan.md`.
- [x] Add non-behavioral resource and syntax checks.
- [x] Commit the cleanup so no untracked implementation files remain.
- [x] Confirm that the current worktree diff against `master` contains only
  approved ownership paths.
- [ ] Perform controlled integration into `branch-BetterGui`.
- [ ] Run full validation on the integrated BetterGUI branch before merging to
  `master`.
