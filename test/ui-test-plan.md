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
