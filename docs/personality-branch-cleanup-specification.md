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

## Checklist

- [ ] Audit the complete diff against `master`.
- [ ] Separate approved assets, standalone FXML, scoped CSS, and documents
  from shared behavior changes.
- [ ] Create or retain `personality.css` with only scoped selectors.
- [ ] Create or retain `BrandHeader.fxml` without editing `MainWindow.fxml`.
- [ ] Retain font files, licenses, supplied avatar references, and the clover
  pattern asset.
- [ ] Convert response wording and shared behavior notes into explicit
  BetterGUI handoffs.
- [ ] Remove personality hunks from shared Java, CSS, FXML, functional tests,
  and `test/gui-test-plan.md`.
- [ ] Add only non-behavioral resource availability checks, if needed.
- [ ] Confirm that no untracked implementation files remain.
- [ ] Confirm that the branch contains only approved ownership paths.
- [ ] Perform controlled integration into `branch-BetterGui`.
- [ ] Run full validation on the integrated BetterGUI branch before merging to
  `master`.
