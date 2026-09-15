# LuckyNoSlacky Response Personality Handoff Review

## Ownership status

This document is a deferred review draft for the response-content owner on
`branch-BetterGui`. Under Specification B, response wording, grammar,
teasing, error messages, response-tone classification, command mapping, and
functional tests are outside the ownership of `branch-personality`. The draft
is retained for manual review and handoff only; it is not a personality
implementation deliverable and must not be merged as part of the visual
resource package.

This document is a review draft for the combined response-personality
increment. It covers response wording consistency and friendly teasing/error
messages without changing command parsing, task behaviour, persistence, GUI
styling, or response-tone classifications.

The proposed voice is deliberately strong in Singlish, but playful rather than
insulting. Where practical, an error explains what happened and suggests what
to try next.

## Review conventions

The placeholders below represent the existing runtime values:

- `<command>` is the command name, such as `list` or `bye`.
- `<format>` is the existing command syntax and must remain unchanged.
- `<task>` is the existing formatted task text.
- `<count>` is the current task count.
- `<duration>` is the duration text supplied by the user.

## Proposed response changes

### 1. Invalid command format

**Context:** A supported command is missing arguments, has malformed markers,
or uses the wrong argument structure. This same response is used for ToDo,
deadline, event, find, snooze, and reschedule format errors.

Current:

```text
Eh HELLO you know how to type command one anot?
Lai lai let me teach you: `<command> <format>`
```

Proposed:

```text
Eh, this command format abit off leh.
Lai, try this one instead: `<command> <format>`
```

The command syntax inside the code block remains unchanged and actionable.

### 2. Missing task number

**Context:** `mark`, `unmark`, `delete`, `snooze`, or `resched` is entered
without a task number, or the parser receives a task-number section containing
whitespace.

Current:

```text
Eh which task you talking about har? Can say clearly anot.
```

Proposed:

```text
Eh, which task number you referring to ah? Give me one number can already.
```

### 3. Invalid or unavailable task number

**Context:** The task number is non-numeric, non-positive, or outside the
current task list.

Current:

```text
You siao ah how to spin this task from thin air?
```

Proposed:

```text
Aiyo, I cannot find that task number leh. Pick one from your current task list ah.
```

### 4. Extra arguments for a standalone command

**Context:** `list` or `bye` receives extra words or unsupported options.

Current:

```text
Why you so losor! Leave the <command> command to do its own thing lah
```

Proposed:

```text
Eh, the <command> command does not need extra words lah. Try `<command>` on its own.
```

### 5. Blank command input

**Context:** The user submits a blank or whitespace-only command.

Current:

```text
Eh you mute issit?? Just say what you want lah!
```

Proposed:

```text
Eh, I did not catch a command ah. Tell me what you need, I help you settle.
```

### 6. Missing task description

**Context:** `todo` is entered without a task description.

Current:

```text
You don't tell me what to do how I know what to do???
```

Proposed:

```text
Eh, what task should I help you remember ah? Add a description after the command.
```

### 7. Unknown command

**Context:** The first command word is not one of the supported commands.

Current:

```text
What talking you? I only understand todo, deadline, event, list, mark, unmark, delete, find, snooze, resched, or bye, ok?
```

Proposed:

```text
Aiyo, I do not recognise that command leh. I can help with todo, deadline, event, list, mark, unmark, delete, find, snooze, resched, or bye ah.
```

### 8. Task-load failure

**Context:** The application cannot load the saved task data during startup.

Current:

```text
Eh you so free ah, no tasks were loaded! If you think this is salah, check your task data file.
```

Proposed:

```text
Aiyo, I could not load your saved tasks leh.
Check the task data file and try again ah.
```

### 9. Task-save failure

**Context:** A task mutation cannot be persisted to the task data file.

Current:

```text
Honggan la your system abit rabs ah, I cannot save your task
```

Proposed:

```text
Aiyo, something abit rabak leh — I could not save that task.
Check the task data file permissions and try again ah.
```

### 10. Invalid date or time

**Context:** A date/time value cannot be parsed from a deadline, event,
reschedule, find, or snooze command.

Current:

```text
Eh mr smart alec you tell me your calendar and clock got tell you time like this one meh?
```

Proposed:

```text
Eh, that date or time looks off leh. Check the format and try again ah.
```

### 11. Decimal year or month duration

**Context:** A duration uses a decimal value for a calendar unit such as years
or months, which the current duration rules do not support.

Current:

```text
Paiseh bro... i cannot settle decimal values for years and months yet...
```

Proposed:

```text
Paiseh ah, I cannot handle decimal years or months yet. Use a whole number for those, can?
```

### 12. Unrecognised duration

**Context:** The duration contains an unsupported unit, malformed number, or
otherwise cannot be parsed.

Current:

```text
Eh can you be more specific anot, what do you mean by "<duration>" sia?
```

Proposed:

```text
Eh, that duration "<duration>" abit unclear leh. Try a number with a unit, like "2 hours" or "1.5h" ah.
```

### 13. Negative duration

**Context:** The user supplies a negative snooze duration. This message is
currently defined directly in `DurationParser` rather than
`LuckyNoMessages`.

Current:

```text
Siao ah time where got negative one
```

Proposed:

```text
Aiyo, duration cannot go negative one leh. Use a positive number and try again ah.
```

### 14. Impossible time ordering

**Context:** A deadline is in the past, an event ends before it starts, or a
rescheduled/snoozed timing would violate the existing time rules.

Current:

```text
you think you time travelling issit? check your date and time properly hor!
```

Proposed:

```text
Aiyo, that timing goes backwards leh. Make sure the end comes after the start, then try again hor.
```

### 15. Timeless ToDo used with a timed command

**Context:** The user attempts to snooze or reschedule a ToDo, which has no
deadline or event timing.

Current:

```text
Eh mr blur sotong this task don't even have time for you to <command> la
```

Proposed:

```text
Eh, this task has no timing to <command> leh. Try it on a deadline or event instead ah.
```

### 16. Snooze success

**Context:** A deadline or event is successfully snoozed.

Current:

```text
Nah here's your snooze you lazy bum, don't slack too much hor!
  <task>
```

Proposed:

```text
Okay lah, snooze settled! Here is the updated task:
  <task>
```

### 17. Reschedule success

**Context:** A deadline or event is successfully rescheduled.

Current:

```text
Nah here's your resched you lazy bum, don't slack too much hor!
  <task>
```

Proposed:

```text
Okay lah, reschedule settled! Here is the updated task:
  <task>
```

### 18. Greeting

**Context:** The application starts and introduces LuckyNoSlacky.

Current:

```text
Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
```

Proposed:

```text
Limpeh here, your lucky task buddy. I make sure you stay lucky and not slacky, can?
```

The existing “Limpeh” persona name is retained.

### 19. Goodbye

**Context:** The user enters `bye`, or input ends without an explicit exit.

Current:

```text
Huh so fast zao ah, rest well ah!
```

Proposed:

```text
So fast zao already ah? Rest well, and come back when you are ready to settle more tasks hor!
```

### 20. Task added

**Context:** A ToDo, deadline, or event is successfully added.

Current:

```text
Got one more thing to remember ah:
  <task>
Now you got <count> tasks to settle.
```

Proposed:

```text
Steady lah, I added this task for you:
  <task>
You now have <count> task(s) to settle ah.
```

The implementation should use the singular form “task” when `<count>` is 1
and “tasks” otherwise.

### 21. Task marked complete

**Context:** A task is successfully marked as done.

Current:

```text
Swee lah you're done with this task:
  <task>
```

Proposed:

```text
Swee lah, task settled! Here is the completed task:
  <task>
```

### 22. Task marked incomplete

**Context:** A completed task is successfully marked as not done.

Current:

```text
Eh salah you're not done with this task ah, must remember to do ah!
  <task>
```

Proposed:

```text
No worries lah, I put this task back on your list:
  <task>
```

### 23. Task deleted

**Context:** A task is successfully deleted.

Current:

```text
Solid man can don't care about this one already:
  <task>
But you still got <count> tasks to settle.
```

Proposed:

```text
Can lah, I cleared this task from your list:
  <task>
You now have <count> task(s) left to settle ah.
```

The implementation should use the singular form “task” when `<count>` is 1
and “tasks” otherwise.

### 24. Empty task list

**Context:** `list` or `find` returns no matching tasks.

Current:

```text
Chill lah bro got nothing yet lah!
```

Proposed:

```text
Your task list is empty for now lah. Add something when you are ready ah!
```

### 25. Task-list heading without a date filter

**Context:** `list` returns one or more tasks.

Current:

```text
Nah, all these things you need to do:
```

Proposed:

```text
Okay lah, here are the tasks to settle:
```

### 26. Task-list heading with a date filter

**Context:** `find /on <date>` or a date-filtered search returns one or more
tasks.

Current:

```text
Nah, all these things you need to do on: <date>
```

Proposed:

```text
Okay lah, here are the tasks to settle on: <date>
```

## Intentionally unchanged responses

These responses are not part of the proposed wording change:

- The ASCII banner art and `[NO SLACKING]` branding.
- The command-format strings inside code blocks, because they describe the
  actual input contract.
- Task display text such as `[T][ ] read book`, including its numbering and
  date/time formatting.
- Internal Java exception messages that are not shown to users.

## Manual review checklist

- [ ] Strong Singlish level feels intentional rather than confusing.
- [ ] Every joke is playful and contains no insult or shaming language.
- [ ] Error responses tell the user what to try next where practical.
- [ ] “Limpeh” and “LuckyNoSlacky” remain the preferred persona names.
- [ ] Success responses are celebratory enough without becoming noisy.
- [ ] The revised task-count wording handles singular and plural correctly.
- [ ] The revised list headings still sound natural with task panels below them.
- [ ] The banner and command syntax should remain unchanged.

## Integration rule

After manual approval, the content owner should transfer the selected wording
changes to `branch-BetterGui` for implementation and functional-test review.
The personality branch may reference this document as a handoff, but must not
modify response-producing classes or the canonical GUI test plan to implement
it.
