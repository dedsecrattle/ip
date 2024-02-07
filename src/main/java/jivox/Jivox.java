
package jivox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jivox.exception.DataHandlerException;
import jivox.exception.JivoxException;
import jivox.task.Deadline;
import jivox.task.Event;
import jivox.task.Task;
import jivox.task.TaskList;
import jivox.task.Todo;

/**
 * Jivox handles the core functionality of the to-do list application.
 */
public class Jivox {

    private final DatabaseHandler dbHandler;
    private final Ui ui;
    private final TaskList tasks;
    private final Parser parser;
    private boolean isRunning = true;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy HH:mm");

    /**
     * Creates a new Jivox instance with the given file path.
     *
     * @param filePath The path to the database file.
     */
    public Jivox(String filePath) {
        this.dbHandler = new DatabaseHandler(filePath);
        this.tasks = new TaskList(dbHandler.load());
        this.ui = new Ui();
        this.parser = new Parser();
    }

    /**
     * Marks the task at the given index as completed.
     *
     * @param i The index of the task to mark.
     * @throws JivoxException If index is invalid.
     */
    private String mark(int i) throws JivoxException {
        if (i > this.tasks.getLength() || i < 0) {
            throw new JivoxException("Oops! There are only " + this.tasks.getLength() + " Tasks!");
        }
        Task t = this.tasks.getTask(i - 1);
        t.mark();
        dbHandler.save(this.tasks);
        return this.ui.showMark(t);
    }

    /**
     * Unmarks the task at the given index as completed.
     *
     * @param i The index of the task to unmark.
     * @throws JivoxException If index is invalid.
     */
    private String unmark(int i) throws JivoxException {
        if (i > this.tasks.getLength() || i < 0) {
            throw new JivoxException("Oops! There are only " + this.tasks.getLength() + " Tasks!");
        }
        Task t = this.tasks.getTask(i - 1);
        t.unmark();
        dbHandler.save(this.tasks);
        return this.ui.showUnmark(t);
    }

    private void addEvent(String content) throws JivoxException {
        String[] first = this.parser.split(content, " /from ");
        //    content.split(" /from ");
        if (first.length == 1) {
            throw new JivoxException("No time interval (from) received  for the event , Please try again!");
        }
        String[] second = this.parser.split(first[1], " /to ", 2);
        //first[1].split(" /to ",2);
        if (second.length == 1) {
            throw new JivoxException("No time interval received (to) for the event , Please try again!");
        }
        LocalDateTime from = LocalDateTime.parse(second[0], formatter);
        LocalDateTime to = LocalDateTime.parse(second[1], formatter);
        if (to.isBefore(from)) {
            throw new JivoxException("Invalid event ! To is before From");
        }
        this.tasks.add(new Event(first[0].trim(), from, to));
        dbHandler.save(this.tasks);
    }

    private void addTodo(String content) throws DataHandlerException {
        this.tasks.add(new Todo(content));
        dbHandler.save(this.tasks);
    }

    private void addDeadline(String content) throws JivoxException {
        String[] in = this.parser.split(content, " /by ", 2);
        // content.split(" /by ",2);
        if (in.length == 1) {
            throw new JivoxException("Oooops! Please provide a deadline");
        }
        LocalDateTime deadline = LocalDateTime.parse(in[1], formatter);
        this.tasks.add(new Deadline(in[0].trim(), deadline));
        dbHandler.save(this.tasks);
    }

    /**
     * Adds a new task of the given type and description.
     *
     * @param type        The type of task (todo, deadline, event).
     * @param description The task description.
     * @throws JivoxException If unable to add the task.
     */
    public String add(String type, String description) throws JivoxException {
        switch (type) {
        case "todo":
            addTodo(description);
            break;
        case "deadline":
            addDeadline(description);
            break;
        case "event":
            addEvent(description);
            break;
        default:
            throw new JivoxException("Can't identify the Task type!");
        }
        return this.ui.showAdd(this.tasks.getTask(this.tasks.getLength() - 1), this.tasks.getLength());
    }

    /**
     * Deletes the task at the given index.
     *
     * @param i The index of the task to delete.
     * @throws JivoxException If index is invalid.
     */
    public String delete(int i) throws JivoxException {
        if (i > this.tasks.getLength() || i < 0) {
            throw new JivoxException("Oops! There are only " + this.tasks.getLength() + " Tasks!");
        }
        Task t = this.tasks.getTask(i - 1);
        this.tasks.delete(i - 1);
        this.dbHandler.save(this.tasks);
        return this.ui.showDelete(t, this.tasks.getLength());
    }

    /**
     * Shows tasks due on the given date.
     *
     * @param input The date to show tasks for.
     */
    public String show(String input) {
        String[] split = this.parser.split(input, "/on ");
        LocalDate time = LocalDate.parse(split[1].replaceFirst(" ", ""), DateTimeFormatter.ofPattern("d/MM/yyyy"));
        return this.ui.showDeadline(this.tasks, time);
    }

    public String find(String input) {
        return this.ui.showFind(this.tasks, input);
    }


    public String getResponse(String rawInput) {
        Commands type = null;
        String[] input;
        try {
            type = parser.parseCommand(rawInput);
            input = parser.parseInput(rawInput);
            switch (type) {
            case BYE:
                this.isRunning = false;
                this.ui.close();
                return this.ui.exit();
            case DEADLINE:
                return this.add("deadline", input[1]);
            case EVENT:
                if (input.length == 1) {
                    throw new JivoxException("Ooops! Please provide a description!");
                }
                return this.add("event", input[1]);
            case TODO:
                if (input.length == 1) {
                    throw new JivoxException("Ooops! Please provide a description!");
                }
                return this.add("todo", input[1]);
            case MARK:
                if (input.length == 1) {
                    throw new JivoxException("Please, provide a task number to mark");
                }
                return this.mark(Integer.parseInt(input[1]));
            case UNMARK:
                if (input.length == 1) {
                    throw new JivoxException("Please, provide a task number to mark");
                }
                return this.unmark(Integer.parseInt(input[1]));
            case DELETE:
                if (input.length == 1) {
                    throw new JivoxException("Please, provide a task number to delete");
                }
                return this.delete(Integer.parseInt(input[1]));
            case LIST:
                return this.ui.showTasks(this.tasks);
            case SHOW:
                return this.show(input[1]);
            case FIND:
                return this.find(input[1]);
            default:
                throw new JivoxException("Sorry ! , I can't Understand your Command");
            }
        } catch (JivoxException e) {
            return this.ui.showException(e);
        }
    }

    /**
     * Starts the application.
     */
    public void run() {
        this.ui.greet();
        while (isRunning) {
            String rawInput = this.ui.input();
            System.out.println(this.getResponse(rawInput));
        }
    }

    public static void main(String[] args) {
        Jivox jivox = new Jivox("./data/jivox.txt");
        jivox.run();
    }

}
