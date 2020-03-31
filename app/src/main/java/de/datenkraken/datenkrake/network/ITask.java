package de.datenkraken.datenkrake.network;

import de.datenkraken.datenkrake.network.clients.Client;

/**
 * Interface providing functions to  represent a task for HTTP Queries.
 * To start or cancel this task call request() or cancel().
 * This class should be implemented by template Tasks for different {@link Client}s.
 * When request() is called, this class will be distributed by TaskDistributor and executed by
 * it's the client, given in {@link #processedBy()}.
 *
 * @author  Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public interface ITask {

    /**
     * Cancels this request.
     */
    void cancel();

    /**
     * Request enqueuing this task.
     */
    void request();

    /**
     * Returns the class of the {@link Client} this Task wants to get executed by.
     * @return Class of {@link Client}
     */
    Class<? extends Client<? extends ITask>> processedBy();
}
