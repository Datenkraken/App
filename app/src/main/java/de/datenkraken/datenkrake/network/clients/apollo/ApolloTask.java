package de.datenkraken.datenkrake.network.clients.apollo;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Query;

import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.TaskDistributor;

/**
 * Class representing a task for Apollo Queries. <br>
 * Can be of type query or mutation.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
abstract class ApolloTask<T> extends ApolloCall.Callback<T> implements ITask {
    ApolloCall call;

    /**
     * Enum determining, if the type of the task is a request or a mutation.
     */
    protected enum Type {
        QUERY,
        MUTATION
    }

    /**
     * Returns the appropriate query for this task used by the
     * {@link com.apollographql.apollo.ApolloClient}.
     * Returns null if this task is a mutation.
     *
     * @return request of task as query.
     */
    protected abstract Query getQuery();

    /**
     * Returns the appropriate mutation for this task used by the
     * {@link com.apollographql.apollo.ApolloClient}.
     * Returns null if this task is a query.
     *
     * @return request of the task as mutation.
     */
    protected abstract Mutation getMutation();

    /**
     * Cancels the request of the task.
     */
    @Override
    public final void cancel() {
        if (this.call == null) {
            return;
        }

        this.call.cancel();
    }

    /**
     * Requests enqueueing of this task to the {@link TaskDistributor}.
     */
    @Override
    public void request() {
        TaskDistributor.getInstance().request(this);
    }

    /**
     * Returns the class of the {@link de.datenkraken.datenkrake.network.clients.Client}
     * this Task wants to get executed by.
     *
     * @return the {@link Apollo} class.
     */
    @Override
    public final Class<Apollo> processedBy() {
        return Apollo.class;
    }

    /**
     * Returns the type of this task, which gives {@link Apollo} a possibility to differentiate
     * between an apollo query and mutation.
     *
     * @return type of this task.
     */
    abstract Type getType();
}
