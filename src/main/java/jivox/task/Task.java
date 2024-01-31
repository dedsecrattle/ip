package jivox.task;
import java.time.LocalDateTime;

/**
 * Task is an abstract class representing a generic task.
 * Specific task types extend Task.
 */
public abstract class Task {
    private boolean isDone;
    private String content;

    /**
     * Creates a new Task with the given content/description.
     *
     * @param content The task description.
     */
    public Task(String content) {
        this.content = content;
        this.isDone = false;
    }

    /**
     * Marks this task as completed.
     */
    public void mark() {
        this.isDone = true;
    }

    /**
     * Gets the deadline for this task.
     * The base implementation returns null.
     *
     * @return The deadline, or null if none.
     */
    public LocalDateTime getDeadline() {
        return null;
    }

    /**
     * Unmarks this task as completed.
     */
    public void unmark() {
        this.isDone = false;
    }

    /**
     * Gets the description of this task.
     *
     * @return The description.
     */
    public String getDescription() {
        return this.content;
    }

    /**
     * Gets the type identifier for this task.
     *
     * @return The type identifier.
     */
    abstract public String getType();

    /**
     * Checks if this task is marked as completed.
     *
     * @return true if completed, false otherwise.
     */
    public boolean getStatus() {
        return this.isDone;
    }

    /**
     * Returns a string representation of this task.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        String mark = isDone ? "X" : " ";
        return "[" + mark + "] " + this.content;
    }
}
