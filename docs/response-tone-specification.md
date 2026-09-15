# Response Tone and Dialogue Role Specification

## Purpose

Separate the speaker of a conversation message from the semantic tone used to
present a LuckyNoSlacky response. This keeps user alignment independent from
success, information, warning, and system-error styling.

## Response tones

| Tone | Usage | Visual cue |
| --- | --- | --- |
| `NEUTRAL` | Greeting and `bye` | Standard chatbot bubble |
| `SUCCESS` | Successful task-changing command | Clover-green bubble with `🍀` |
| `INFO` | `list` and `find` results | Soft cream information bubble |
| `WARNING` | Invalid or incomplete user input | Existing warning style with `⚠` |
| `SYSTEM_ERROR` | Storage loading or saving failure | Muted-red bubble with `⛔` |

The response message text remains unchanged. No visible tone prefixes are
added.

## Command mapping

- `bye` returns `NEUTRAL`.
- `list` and `find` return `INFO`.
- `todo`, `deadline`, `event`, `mark`, `unmark`, `delete`, `snooze`, and
  `resched` return `SUCCESS` when execution succeeds.
- Invalid input returns `WARNING`.
- Storage failures return `SYSTEM_ERROR`.

## Dialogue roles

`DialogueType` represents only the speaker:

- `USER`: right-aligned user message with the existing user avatar.
- `CHATBOT`: left-aligned LuckyNoSlacky response with the existing chatbot
  avatar.

The response tone is passed separately to chatbot dialogue rows. Warnings and
system errors remain left-aligned with other LuckyNoSlacky responses.

## Accessibility

- User accessible text remains `You: <message>`.
- Normal, successful, informational, and system-error chatbot messages retain
  the existing `LuckyNoSlacky: <message>` accessible text.
- Warning accessible text remains
  `Bodoh sia like that also can kena warning <message>`.
- Tone-specific prefixes are not added.
- The `🍀`, `⚠`, and `⛔` markers are decorative because the containing row
  already exposes its accessible message text.

## Verification

- Test each command's declared tone.
- Test all five GUI response treatments.
- Confirm user and chatbot alignment is independent of response tone.
- Confirm CLI response wording remains unchanged.
- Run Java 25 JUnit, GUI, CLI UI, Checkstyle, Javadocs, and code-quality
  checks.
