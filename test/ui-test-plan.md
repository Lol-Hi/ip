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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Test Case: Task-number input matrix

- Aim: Verify missing, non-numeric, negative, zero, out-of-range, and extra task-number inputs, while confirming valid status changes still update the task.

### Input

```text
todo read book
mark
mark abc
mark 0
mark 2
mark 1 extra
list
mark 1
unmark
unmark abc
unmark -1
unmark 2
unmark 1 extra
list
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] read book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Eh which task you talking about har? Can say clearly anot.
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  Eh which task you talking about har? Can say clearly anot.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Swee lah you're done with this task:
    [T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh which task you talking about har? Can say clearly anot.
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  Eh which task you talking about har? Can say clearly anot.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh salah you're not done with this task ah, must remember to do ah!
    [T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Test Case: Command casing, whitespace, and argument boundaries

- Aim: Verify handling of blank input, case-insensitive commands, surrounding whitespace, extra arguments, and invalid task-type formats.

### Input

```text
   
TODO   read book   
LIST extra
list
DEADLINE return book /by Sunday
deadline return book /by
list
EVENT meeting /from Mon 2pm /to 4pm
event meeting /to 4pm /from Mon 2pm
list
bye now
BYE
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Eh you mute issit?? Just say what you want lah!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] read book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Why you so losor! Leave the list command to do its own thing lah
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [D][ ] return book (by: Sunday)
  Now you got 2 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Eh HELLO you know how to type command one anot? 
  Lai lai let me teach you: deadline <description> /by <date/time>.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  2.[D][ ] return book (by: Sunday)
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [E][ ] meeting (from: Mon 2pm to: 4pm)
  Now you got 3 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Eh HELLO you know how to type command one anot? 
  Lai lai let me teach you: event <description> /from <start> /to <end>.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  2.[D][ ] return book (by: Sunday)
  3.[E][ ] meeting (from: Mon 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
  Why you so losor! Leave the bye command to do its own thing lah
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] borrow book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [D][ ] return book (by: Sunday)
  Now you got 2 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [E][ ] project meeting (from: Mon 2pm to: 4pm)
  Now you got 3 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] borrow book
  2.[D][ ] return book (by: Sunday)
  3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] read book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Swee lah you're done with this task:
    [T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh salah you're not done with this task ah, must remember to do ah!
    [T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  What talking you? I only understand todo, deadline, event, list, mark, unmark, or bye, ok?
  ____________________________________________________________
  ____________________________________________________________
  Eh HELLO you know how to type command one anot? 
  Lai lai let me teach you: deadline <description> /by <date/time>.
  ____________________________________________________________
  ____________________________________________________________
  Eh HELLO you know how to type command one anot? 
  Lai lai let me teach you: event <description> /from <start> /to <end>.
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  You don't tell me what to do how I know what to do???
  ____________________________________________________________
  ____________________________________________________________
  Why you so losor! Leave the bye command to do its own thing lah
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Test Case: Repeated mark and unmark commands

- Aim: Verify that repeating `mark` keeps a task done and repeating `unmark` keeps it not done, instead of toggling it unexpectedly.

### Input

```text
todo read book
mark 1
mark 1
unmark 1
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] read book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Swee lah you're done with this task:
    [T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  Swee lah you're done with this task:
    [T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh salah you're not done with this task ah, must remember to do ah!
    [T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh salah you're not done with this task ah, must remember to do ah!
    [T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Test Case: Interleaved task creation edge cases

- Aim: Verify that invalid ToDo, Deadline, and Event commands do not add tasks, while valid commands before and after them are preserved.

### Input

```text
todo read book
todo
list
deadline return book /by Sunday
deadline return book
list
event project meeting /from Mon 2pm /to 4pm
event project meeting /from Mon 2pm
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] read book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  You don't tell me what to do how I know what to do???
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [D][ ] return book (by: Sunday)
  Now you got 2 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Eh HELLO you know how to type command one anot? 
  Lai lai let me teach you: deadline <description> /by <date/time>.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  2.[D][ ] return book (by: Sunday)
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [E][ ] project meeting (from: Mon 2pm to: 4pm)
  Now you got 3 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Eh HELLO you know how to type command one anot? 
  Lai lai let me teach you: event <description> /from <start> /to <end>.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  2.[D][ ] return book (by: Sunday)
  3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Test Case: Free-form task text and date/time strings

- Aim: Verify that task descriptions and date/time values are retained as entered, including punctuation and command-like text inside a ToDo description.

### Input

```text
todo /by /from /to !@#
list
deadline do homework /by no idea :-p
event project meeting /from ?? /to forever
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] /by /from /to !@#
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] /by /from /to !@#
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [D][ ] do homework (by: no idea :-p)
  Now you got 2 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [E][ ] project meeting (from: ?? to: forever)
  Now you got 3 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] /by /from /to !@#
  2.[D][ ] do homework (by: no idea :-p)
  3.[E][ ] project meeting (from: ?? to: forever)
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```

## Test Case: Interleaved task status edge cases

- Aim: Verify that invalid mark and unmark commands do not change a task's status when valid status changes occur between them.

### Input

```text
todo read book
mark 2
list
mark 1
unmark 2
list
unmark 1
mark
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
  Limpeh is LuckyNoSlacky, and I will confirm make sure you're lucky and not slacky!
  ____________________________________________________________
  ____________________________________________________________
  Got one more thing to remember ah: 
    [T][ ] read book
  Now you got 1 tasks to settle.
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Swee lah you're done with this task:
    [T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  You siao ah how to spin this task from thin air?
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][X] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh salah you're not done with this task ah, must remember to do ah!
    [T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Eh which task you talking about har? Can say clearly anot.
  ____________________________________________________________
  ____________________________________________________________
  Nah all these stuff you need to do:
  1.[T][ ] read book
  ____________________________________________________________
  ____________________________________________________________
  Huh so fast zao ah, rest well ah!
  ____________________________________________________________
```
