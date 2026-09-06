# LuckyNoSlacky

LuckyNoSlacky is a command-line task manager for keeping track of ToDos,
deadlines, and events. Tasks are stored in memory while the application is
running and can be marked as done or not done.

## Features

- Add ToDo tasks without a date or time.
- Add Deadline tasks with flexible date/time input.
- Add Event tasks with flexible start and end date/time input.
- List tasks in the order they were added.
- Find tasks by description, date, or both.
- Mark tasks as done or explicitly mark them as not done.
- Delete tasks by their task number.
- Store up to 100 tasks.
- Save tasks automatically to disk whenever the task list changes.
- Load previously saved tasks automatically when the chatbot starts.

## Getting started

### Prerequisites

- Java Development Kit (JDK) 25.
- IntelliJ IDEA, if you want to run the application using the IDE.
- Gradle is not required separately because the project includes the Gradle
  wrapper.

### Run using IntelliJ IDEA

1. Open IntelliJ IDEA. 
2. Select **Open** and choose the project directory.
3. Configure the project SDK and language level to use JDK 25. See the
   [IntelliJ IDEA JDK instructions](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).
4. Open `src/main/java/LuckyNoSlacky.java`.
5. Right-click the file and select **Run `LuckyNoSlacky.main()`**.

The application starts with the following banner:

```text
     .--"""""--.
   /  /^\   /^\  \
  |  .---------.  |
  |  | | | | | |  |
   \ '---------' /
     '-._____.-'
    [NO SLACKING]
  LuckyNoSlacky is here to help!
```

After displaying the banner, LuckyNoSlacky greets the user:

```text
Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
```

### Run using the command line

From the project root, compile the project and run the chatbot with:

```bash
./gradlew build
java -cp build/classes/java/main luckynoslacky.LuckyNoSlacky
```

On Windows, use `gradlew.bat build` instead of `./gradlew build`.

### Run the JavaFX GUI

From the project root, launch the graphical interface with:

```bash
./gradlew runGui
```

On Windows, use `gradlew.bat runGui` instead. Ensure that Java 25 is
configured before launching the application.

### Run using the released JAR

LuckyNoSlacky is also distributed as a fat JAR containing the application and
its runtime dependencies. Save the JAR under
at:

```text
build/libs/luckyNoSlacky.jar
```

To run the JAR, use Java 25 from the project root:

```bash
java -jar build/libs/luckyNoSlacky.jar
```

No additional dependency or classpath setup is needed.
The relative `data/` folder and `data/luckyNoSlacky.csv` file will then be 
created in the same `build/libs/` library.

### Task data persistence

LuckyNoSlacky saves the task list automatically after a task is added, marked,
unmarked, or deleted. The data is stored in the relative path
`data/luckyNoSlacky.csv` using the following columns:

```text
Task type, isCompleted, Description, startTime, finishTime
```

The data directory and CSV file are created automatically when the first task
change is saved. If the data file does not exist when the chatbot starts, the
chatbot starts with an empty task list. 

The time fields are used as follows:

- ToDos leave both time fields empty.
- Deadlines store the deadline in `finishTime`.
- Events store their start time in `startTime` and end time in `finishTime`.

Date/time values are saved using `yyyy-MM-dd HH:mm` (for example,
`2030-10-15 14:15`).

Do not edit the data file while LuckyNoSlacky is running.

If the file is malformed or cannot be read,
LuckyNoSlacky displays the following while starting with an empty task list.

```text
Eh you so free ah, no tasks were loaded! If you think this is salah, check your task data file.
```

If a task change cannot be saved, LuckyNoSlacky displays:

```text
Honggan la your system abit rabs ah, I cannot save your task
```

## User guide

Enter one command per line. Commands are case-insensitive, and leading or
trailing spaces are ignored.

| Command               | Format                                                            | Description |
|-----------------------|-------------------------------------------------------------------| --- |
| Create Todo Task      | `todo <description>`                                              | Adds a task without date or time information. |
| Create Deadline Task  | `deadline <description> /by <date/time>`                          | Adds a task with a deadline. |
| Create Event Task     | `event <description> /from <start date/time> /to <end date/time>` | Adds a task with a start and end time. |
| List Tasks            | `list`                                                            | Displays all tasks and their numbers. |
| Mark Task as Done     | `mark <number>`                                                   | Marks the specified task as done. |
| Unmark Task as Undone | `unmark <number>`                                                 | Marks the specified task as not done. |
| Delete Task           | `delete <number>`                                                 | Removes the specified task from the list. |
| Find Tasks             | `find [<description>] [/on <date>]`                               | Finds tasks by description, date, or both. |
| Exit                  | `bye`                                                             | Exits the chatbot. |

### Adding tasks

To add a ToDo task:

```text
todo borrow book
```

To add a Deadline task:

```text
deadline return book /by 2030-10-15 14:15
```

To add an Event task:

```text
event project meeting /from 2030/10/16 2pm /to 2030-10-16 16:00
```

### Listing tasks

Use `list` to display all tasks:

```text
list
```

Tasks are displayed using a type marker and a completion marker:

```text
1.[T][ ] borrow book
2.[D][X] return book (by: Tue Oct 15 2030, 2.15pm)
3.[E][ ] project meeting (from: Wed Oct 16 2030, 2.00pm to: Wed Oct 16 2030, 4.00pm)
```

#### Type markers

- `[T]` represents a ToDo.
- `[D]` represents a Deadline.
- `[E]` represents an Event.

#### Completion markers

- `[ ]` means the task is not done.
- `[X]` means the task is done.

### Marking and Unmarking Tasks

Mark and unmark tasks by writing the corresponding command,
followed by the task number based on the output of the `list` command:

```text
mark 1
unmark 1
```

Both commands require exactly one valid task number.

Marking an already completed task keeps it completed. Unmarking an incomplete
task keeps it incomplete. These commands explicitly set the desired status
instead of toggling the current status.

### Deleting tasks

Delete a task by using its number from the `list` output:

```text
delete 2
```

The deleted task is removed from memory, and later tasks are renumbered. The
command requires a valid task number. Failed deletion commands do not
change the task list.

### Finding tasks

Use `find` with a description, a date, or both:

```text
find book
find /on 26 Aug 2026
find book /on 26 Aug 2026
```

Description queries use case-insensitive substring matching. For example,
`find book` matches descriptions such as `read book` and `return book`.

The `/on` tag limits the search to a specified date. It can be used without a
description:

```text
find /on 26 Aug 2026
```

When both filters are provided, a task must satisfy both the description and
date conditions:

```text
find book /on tmr
```

Date-only searches include:

- Deadlines whose due date is the queried date.
- Events whose start and end dates include the queried date.

Description-only searches can match ToDos, Deadlines, and Events,
using the regular task-list header.

If there are no tasks found, LuckyNoSlacky replies:

```text
Wah, you very free hor, got nothing to do sia!
```

### Invalid commands

Invalid input produces an explanatory message and does not terminate the
chatbot. Examples include:

- Empty input.
- Unknown commands.
- Missing task descriptions.
- Missing or invalid task numbers.
- Extra arguments after `list` or `bye`.
- Incorrect `/by`, `/from`, or `/to` formats.
- Missing or invalid `/on` formats for `find`.
- Invalid date/time values.
- Deadlines in the past or events whose end is before their start.

### Accepted date/time formats

The chatbot accepts the following date forms:

- ISO-style dates: `2030-10-15`, `2030/10/15`.
- Day-first numeric dates: `15/10/2030`, `15-10-2030`.
- Text dates: `15 Oct 2030`, `15 October 2030`, `Oct 15 2030`,
  `October 15 2030`.
- Dates with weekdays: `Tue Oct 15 2030`, `Tuesday, October 15 2030`.
- Dates without a year: `June 6th`; the current year is used unless that date
  has passed, in which case the next year is used.
- Named relative dates: `today`, `tomorrow`/`tmr`, and `yesterday`/`ytd`; these
  resolve relative to the current date.
- Days of a month without a month: `the 15th`; the current month is used unless
  that date has passed, in which case the next month is used.
- A weekday alone: `Monday`; this resolves to the next occurrence of Monday.
- Current-week weekdays: `this Monday` through `this Sunday` refer to the
  Monday-to-Sunday week containing today. Past dates are allowed for event
  starts, but not for deadlines or event ends.
- Following-week weekdays: `next Wednesday` refers to the Wednesday in the
  week beginning with the following Sunday. `next next Wednesday` and `the
  following Wednesday` refer to the week after that.
- Upcoming weekdays: `this coming Wednesday` and `the coming Tuesday` refer to
  the next occurrence strictly after today.
- Relative months and years use the same offsets: `this month`/`this year`
  means the current period, `next` means the following period, and `next next`
  or `the following` means the period after that.
- A bare day of the month such as `the 15th` resolves to the next available
  15th; `next next 15th` and `the following 15th` skip one additional month.

The chatbot accepts these time forms:

- 24-hour time: `14:15`, `14:15:30`.
- 12-hour time: `2pm`, `2 pm`, `2:15pm`, `2:15 pm`, `2.15pm`, `2.15 pm`.
- Compact `HHMM` time when it appears where an invalid year would otherwise be
  expected: `25 Aug 0000` means 25 August of the current year at `00:00`.

Four-digit values are interpreted as years first when both interpretations are
valid. Therefore `25 Aug 2030` means the year 2030; use `25 Aug 20:30` when
you mean 8:30pm.

When a time is given without a date, it uses today if that time has not passed,
or tomorrow if it has passed. A date without a time uses `00:00` for an event
start and `23:59` for an event end or deadline. Listed task times use the
format `Tue Oct 15 2030, 2.15pm`.

For events, a time-only end uses the event start as its reference. If the end
time is later than the start time, it uses the start date; otherwise, it uses
the next date. For example, an event from `25 Aug 2026 11pm` to `1am` ends on
`26 Aug 2026` at `1.00am`. An explicitly supplied end date always takes priority.

### Example session

```text
todo borrow book
  ____________________________________________________________
  Got one more thing to remember ah:
    [T][ ] borrow book
  Now you got 1 tasks to settle.
  ____________________________________________________________
deadline return book /by 2030-10-15 14:15
  ____________________________________________________________
  Got one more thing to remember ah:
    [D][ ] return book (by: Tue Oct 15 2030, 2.15pm)
  Now you got 2 tasks to settle.
  ____________________________________________________________
list
  ____________________________________________________________
  Nah, all these things you need to do:
  1.[T][ ] borrow book
  2.[D][ ] return book (by: Tue Oct 15 2030, 2.15pm)
  ____________________________________________________________
mark 1
  ____________________________________________________________
  Swee lah you're done with this task:
    [T][X] borrow book
  ____________________________________________________________
unmark 1
  ____________________________________________________________
  Eh salah you're not done with this task ah, must remember to do ah!
    [T][ ] borrow book
  ____________________________________________________________

bye
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Running the tests

Run the automated tests from the project root with:

```bash
./gradlew test
```

The tests cover task storage, completion state, task subclasses, task parsing,
and invalid input.

## Development notes

### AI declaration
This project was developed with AI assistance at AI-4 level. 
General ways the AI has been used include:
- I provide the AI with the level requirements and my general action plan, 
  review a more detailed action plan and code snippets, and approve the changes before implementation.
- I detail my intended end result for the AI, and ask it to suggest multiple ways for me to achieve this task.
  I then give further details and ask for a more detailed action plan and code snippets to review,
  for me to approve of the changes before implementation.
