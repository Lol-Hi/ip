# LuckyNoSlacky User Guide

LuckyNoSlacky is a task manager for ToDos, deadlines, and events. It accepts
commands through the CLI or graphical interface and saves changes automatically
to `data/luckyNoSlacky.csv`.

## Creating tasks

```text
todo <description>
deadline <description> /by <date/time>
event <description> /from <start date/time> /to <end date/time>
```

## Managing tasks

```text
list
mark <task number>
unmark <task number>
delete <task number>
find [description] [/on <date>]
```

Task numbers are one-based and are shown by `list`. Date searches include
deadlines on that date and events spanning that date.

## Snoozing tasks

Snoozing extends or replaces the ending time of a timed task. ToDos cannot be
snoozed because they do not have a time.

```text
snooze <task number>
snooze <task number> /by <duration>
snooze <task number> /to <end date/time>
```

The command without an option adds one hour. Supported duration forms are
non-negative values followed by `minute(s)`, `hour(s)`, `day(s)`, `month(s)`,
or `year(s)`. Components must be written in the order years, months, days,
hours, then minutes, with each unit used at most once:

```text
snooze 2
snooze 2 /by 3 hours
snooze 2 /by 1 month
snooze 2 /by 1.5 hours
snooze 2 /by 1 hour 30 minutes
snooze 2 /by 1 month 2 days
snooze 2 /to tomorrow 5pm
```

For deadlines, snoozing changes `byTime`. For events, it changes only
`endTime`; the event's start time is preserved. A zero duration is accepted as
a no-op. Decimal minutes, hours, and days are accepted, while decimal months
and years, abbreviations, repeated units, and non-canonical unit ordering are
rejected. Negative and unsupported durations are rejected.

## Rescheduling tasks

Deadlines use a replacement ending time:

```text
resched <task number> /to <date/time>
```

Events replace both times:

```text
resched <task number> /from <start date/time> /to <end date/time>
```

Examples:

```text
resched 2 /to Friday 6pm
resched 3 /from next Monday 2pm /to 4pm
```

The event end time must not be before its new start time. Past event start
times remain allowed, while deadline times must not be in the past. Both event
times are validated before either one is changed.

## Date and time input

The parser accepts the date and time formats documented in the main
[`README.md`](../README.md), including relative values such as `today`,
`tomorrow`, `yesterday`, `tmr`, and `ytd`. A time-only event end is resolved
relative to its event start and moves to the next day when necessary.

Trailing commentary after a valid snooze or rescheduling value is ignored, but
an additional `/` is treated as malformed syntax. Natural-language durations,
abbreviations, reversed `/from` and `/to` order, and partial event
rescheduling are reserved for later increments.

## Saving tasks

Tasks are saved automatically whenever the task list changes, including after
adding, marking, unmarking, deleting, snoozing, or rescheduling a task. The
existing CSV format is preserved:

```text
Task type,isCompleted,Description,startTime,endTime
```

Snoozing and rescheduling overwrite the relevant time columns while retaining
the task type and completion status.

## Exiting

```text
bye
```

The chatbot displays a goodbye message and then exits.
