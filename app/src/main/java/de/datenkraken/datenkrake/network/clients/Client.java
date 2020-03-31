package de.datenkraken.datenkrake.network.clients;

import de.datenkraken.datenkrake.network.ITask;

/**
 * Represents a network client, able to perform network tasks.
 *
 * @author  Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 *
 * @param <T> Class implementing {@link ITask} this client can perform.
 */
public abstract class Client<T extends ITask> {

    private Class<T> type;

    /**
     * Constructor for class, initializing it.
     */
    private Client() {

    }

    /**
     * Constructor for class, initializing it. <br>
     * Sets type to given value.
     *
     * @param t Class of T to be used as type.
     */
    protected Client(Class<T> t) {
        type = t;
    }

    /**
     * Clears the cache of this client.
     *
     * @return true if successful, false otherwise.
     */
    public abstract boolean clearCache();

    /**
     * Asynchronous execution of the given task.
     *
     * @param task Task to execute.
     */
    public abstract void enqueue(T task);

    /**
     * Synchronous execution of the given task.
     *
     * @param task Task to execute.
     */
    public abstract void execute(T task);

    /**
     * Checks, if this client is able to process the given Task.
     *
     * @param task Task to check for, if it is able to be processed.
     * @param <S> Type of the given Task.
     * @return true, if it can be processed, false otherwise.
     */
    public final <S> boolean canProcess(S task) {
        return task != null && type.isAssignableFrom(task.getClass());
    }
}
