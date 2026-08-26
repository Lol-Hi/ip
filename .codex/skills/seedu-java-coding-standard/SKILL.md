---
name: seedu-java-coding-standard
description: Review and update this project's Java code against SE-EDU intermediate coding conventions.
---

# SE-EDU Java Coding Standard

Use this skill when creating, reviewing, or modifying Java code in this
project. Apply the [SE-EDU Java coding standard](https://se-education.org/guides/conventions/java/intermediate.html)
and use the Google Java Style Guide for topics that the SE-EDU guide does not
cover.

## Required checks

- Keep every class in a package. Use lowercase package names.
- Use PascalCase nouns for classes and enums, camelCase for variables and
  verb-based methods, and `SCREAMING_SNAKE_CASE` for constants.
- Use the three-part test naming form
  `featureUnderTest_testScenario_expectedBehavior` for JUnit test methods.
- Write abbreviations and acronyms in mixed case when they appear in names,
  such as `CsvSaver` and `LuckyNoCli`, rather than `CSVSaver` and `LuckyNoCLI`.
- Use explicit imports; never use wildcard imports. Keep imports grouped and
  ordered consistently: static imports, `java`/`javax`, third-party imports,
  then project imports.
- Use four spaces for indentation, K&R braces, braces around all conditional
  and loop bodies, and spaces around operators and after commas.
- Keep lines at or below 120 characters. Wrap long lines at readable,
  higher-level boundaries and indent continuation lines by eight spaces.
- Initialize variables at declaration when practical and keep declarations in
  the smallest scope needed. Keep fields private to preserve encapsulation.
- Add descriptive Javadocs to public classes and public methods. Javadocs for
  overridden methods and test methods may be omitted when the inherited or
  test context is clear. Write comments in English using American spelling.

## Review workflow

1. Inspect the changed Java files and identify applicable naming, layout,
   import, visibility, variable-scope, and documentation issues.
2. Make the smallest coherent fixes that preserve behavior.
3. Check line lengths, wildcard imports, package declarations, public fields,
   braces, and missing public API Javadocs.
4. Run the project's Java tests after the review. For user-visible changes,
   also review and run the documented UI tests.

Do not change behavior merely to satisfy a stylistic preference, and do not
rename externally required API names unless the project change explicitly
allows that rename.
