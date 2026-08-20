# LuckyNoSlacky

LuckyNoSlacky is a command-line task manager for keeping track of ToDos,
deadlines, and events. Tasks are stored in memory while the application is
running and can be marked as done or not done.

## Features

- Add ToDo tasks without a date or time.
- Add Deadline tasks with a user-provided deadline description.
- Add Event tasks with user-provided start and end time descriptions.
- List tasks in the order they were added.
- Mark tasks as done and reverse their done status.
- Store up to 100 tasks during one application run.

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

### Run using the command line

From the project root, compile the project and run the chatbot with:

```bash
./gradlew build
java -cp build/classes/java/main LuckyNoSlacky
```

On Windows, use `gradlew.bat build` instead of `./gradlew build`.

## User guide

Enter one command per line. Commands are case-insensitive, and leading or
trailing spaces are ignored.

| Command               | Format | Description |
|-----------------------| --- | --- |
| Create Todo Task      | `todo <description>` | Adds a task without date or time information. |
| Create Deadline Task  | `deadline <description> /by <date/time>` | Adds a task with a deadline. |
| Create Event Task     | `event <description> /from <start> /to <end>` | Adds a task with a start and end time. |
| List Tasks            | `list` | Displays all tasks and their numbers. |
| Mark Task as Done     | `mark <number>` | Marks the specified task as done. |
| Unmark Task as Undone | `unmark <number>` | Marks the specified task as not done. |
| Exit                  | `bye` | Exits the chatbot. |

### Adding tasks

To add a ToDo task:

```text
todo borrow book
```

To add a Deadline task:

```text
deadline return book /by Sunday
```

To add an Event task:

```text
event project meeting /from Mon 2pm /to 4pm
```

### Listing tasks

Use `list` to display all tasks:

```text
list
```

Tasks are displayed using a type marker and a completion marker:

```text
1.[T][ ] borrow book
2.[D][X] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
```
Type Markers:
- `[T]` represents a ToDo.
- `[D]` represents a Deadline.
- `[E]` represents an Event.

Completion Markers:
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

### Example session

```text
todo borrow book
  ____________________________________________________________
  Got it. I've added this task:
    [T][ ] borrow book
  ____________________________________________________________
deadline return book /by Sunday
  ____________________________________________________________
  Got it. I've added this task:
    [D][ ] return book (by: Sunday)
  ____________________________________________________________
list
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][ ] borrow book
  2.[D][ ] return book (by: Sunday)
  ____________________________________________________________
mark 1
  ____________________________________________________________
  Nice! I've marked this task as done:
    [T][X] borrow book
  ____________________________________________________________

bye
  Bye, hope to see you again soon!
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
This project was developed with AI assistance at AI-4 level. I provide the AI
with the level requirements and my general plan, review a more detailed action
plan and code snippets, and approve the changes before implementation.
