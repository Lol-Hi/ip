---
name: seedu-java-coding-standard
description: Review and maintain this project's Java Javadocs using SE-EDU conventions.
---

# SE-EDU Java Javadocs

Use this skill when creating, reviewing, or modifying Java code in this
project. Apply the [SE-EDU Javadoc guidance](https://nus-cs2103-ay2627-s1.github.io/website/se-book-adapted/chapters/documentation.html#javadoc)
as the source of truth. This branch intentionally contains only the
documentation-related portion of the project Java standard; the other branch
contains the naming, layout, import, and other code-formatting rules.

## Required Javadocs

- Add a descriptive Javadoc comment to every non-private class, record, enum,
  constructor, and method.
- Add a Javadoc comment to every non-trivial private method. A private
  constructor used only to prevent instantiation and trivial record accessors
  do not need redundant comments.
- Describe what a declaration does, its externally relevant behavior, and
  important constraints. Explain the contract rather than repeating obvious
  implementation details.
- Include `@param` for parameters, `@return` for returned values, and `@throws`
  for exceptions that callers may need to handle. Include these tags only
  where they apply.
- Document overridden methods when the implementation has behavior that is
  important to the subtype or is not clear from the inherited contract.
- Use concise, grammatical descriptions that begin with a verb such as
  “Creates”, “Returns”, or “Checks”. Use `{@code ...}` for code identifiers and
  `<p>` to separate paragraphs where needed.
- Update the Javadoc whenever a method's behavior, parameters, return value,
  exceptions, or class responsibility changes.

## Documentation review workflow

1. Inspect changed Java declarations and identify missing or stale Javadocs.
2. Check that class comments explain the class responsibility and method
   comments explain the caller-visible contract.
3. Check that applicable `@param`, `@return`, and `@throws` tags match the
   declaration.
4. Run `./gradlew javadoc` with Java 25 and resolve all Javadoc warnings.
5. Keep this documentation-focused guidance separate from the code-formatting
   rules when the two branch versions are merged.

Do not change application behavior merely to satisfy a documentation rule.
