package de.datenkraken.datenkrake.network.clients.apollo;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Query;

import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.TaskDistributor;

/**
 * Class representing a task for Apollo Queries, extending the {@link ApolloTask}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public abstract class ApolloQuery<T> extends ApolloTask<T> implements ITask {

    /**
     * Returns the appropriate query for this task used by the
     * {@link com.apollographql.apollo.ApolloClient}.
     *
     * @return request of task as query.
     */
    @Override
    public abstract Query getQuery();

    /**
     * Returns null, because this task is a query.
     *
     * @return null.
     */
    @Override
    protected final Mutation getMutation() {
        return null;
    }

    /**
     * Request enqueuing this query.
     */
    @Override
    public void request() {
        TaskDistributor.getInstance().request(this);
    }

    /**
     * Returns the type of this task, which gives {@link Apollo} a possibility to differentiate
     * between an apollo query and mutation.
     *
     * @return type of this query.
     */
    @Override
    protected final Type getType() {
        return Type.QUERY;
    }
}
