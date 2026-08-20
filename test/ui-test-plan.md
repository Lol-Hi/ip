# LuckyNoSlacky UI test plan

## Test execution information

- Program: `java -cp build/classes/java/main LuckyNoSlacky`
- Working directory: project root
- Comparison: exact output, ignoring only line-ending differences and one final newline
- Failure policy: stop immediately after the first failed test case

## Test Case: Start and exit

- Aim: Verify that the chatbot displays its banner and greeting, then exits when the user enters `bye`.

### Input

```text
bye
```

### Expected output

```text
  ____________________________________________________________
     .--"""""--.
   /  /^\   /^\  \
  |  .---------.  |
  |  | | | | | |  |
   \ '---------' /
     '-._____.-'
    [NO SLACKING]
  LuckyNoSlacky is here to help!
  ____________________________________________________________
  Hello, I'm LuckyNoSlacky!
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Bye, hope to see you again soon!
  ____________________________________________________________
```

## Test Case: Add and list all task types

- Aim: Verify that ToDos, deadlines, and events are added and displayed with the correct type markers and date/time text.

### Input

```text
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
list
bye
```

### Expected output

```text
  ____________________________________________________________
     .--"""""--.
   /  /^\   /^\  \
  |  .---------.  |
  |  | | | | | |  |
   \ '---------' /
     '-._____.-'
    [NO SLACKING]
  LuckyNoSlacky is here to help!
  ____________________________________________________________
  Hello, I'm LuckyNoSlacky!
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
    [T][ ] borrow book
  Now you have 1 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
    [D][ ] return book (by: Sunday)
  Now you have 2 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
    [E][ ] project meeting (from: Mon 2pm to: 4pm)
  Now you have 3 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][ ] borrow book
  2.[D][ ] return book (by: Sunday)
  3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
  Bye, hope to see you again soon!
  ____________________________________________________________
```

## Test Case: Mark and unmark a task

- Aim: Verify that a task can be marked done, returned to not done, and listed with the final status.

### Input

```text
todo read book
mark 1
unmark 1
list
bye
```

### Expected output

```text
  ____________________________________________________________
     .--"""""--.
   /  /^\   /^\  \
  |  .---------.  |
  |  | | | | | |  |
   \ '---------' /
     '-._____.-'
    [NO SLACKING]
  LuckyNoSlacky is here to help!
  ____________________________________________________________
  Hello, I'm LuckyNoSlacky!
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  Got it. I've added this task:
    [T][ ] read book
  Now you have 1 tasks in the list.
  ____________________________________________________________
  ____________________________________________________________
  Nice! I've marked this task as done:
    [T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  OK, I've marked this task as not done yet:
    [T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Here are the tasks in your list:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Bye, hope to see you again soon!
  ____________________________________________________________
```

## Test Case: Invalid commands and arguments

- Aim: Verify that unknown commands and malformed task commands produce helpful errors without terminating the session.

### Input

```text
unknown
deadline return book
event project /from Mon
mark 1
todo
bye now
bye
```

### Expected output

```text
  ____________________________________________________________
     .--"""""--.
   /  /^\   /^\  \
  |  .---------.  |
  |  | | | | | |  |
   \ '---------' /
     '-._____.-'
    [NO SLACKING]
  LuckyNoSlacky is here to help!
  ____________________________________________________________
  Hello, I'm LuckyNoSlacky!
  What can I do for you?
  ____________________________________________________________
  ____________________________________________________________
  I don't recognize that command. Please use todo, deadline, event, list, mark, unmark, or bye.
  ____________________________________________________________
  ____________________________________________________________
  Please use: deadline <description> /by <date/time>.
  ____________________________________________________________
  ____________________________________________________________
  Please use: event <description> /from <start> /to <end>.
  ____________________________________________________________
  ____________________________________________________________
  That task number is invalid.
  ____________________________________________________________
  ____________________________________________________________
  Please provide a task description.
  ____________________________________________________________
  ____________________________________________________________
  The bye command does not take arguments.
  ____________________________________________________________
  ____________________________________________________________
  Bye, hope to see you again soon!
  ____________________________________________________________
```
