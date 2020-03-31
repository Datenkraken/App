package de.datenkraken.datenkrake.network.clients.apollo;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.TaskDistributor;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * Class representing a task for Apollo Mutations, extending the {@link ApolloTask}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public abstract class ApolloMutation<T> extends ApolloTask<T> implements ITask {

    /**
     * Returns the appropriate mutation for this task used by the
     * {@link com.apollographql.apollo.ApolloClient}.
     *
     * @return request of task as query.
     */
    @Override
    public abstract Mutation getMutation();

    /**
     * Returns null, because this class is a mutation.
     *
     * @return null.
     */
    @Override
    protected final Query getQuery() {
        return null;
    }

    /**
     * We override this with an generic logging for errors so the subclasses don't have to override them. <br>
     * Mutations are used to update data at the backend, so you usually don't need to process the response. <br>
     * Displays a list of errors from the response, using timber.
     *
     * @param response apollo response to display the errors for.
     */
    @Override
    public void onResponse(@NonNull Response response) {
        if (response.hasErrors()) {
            @NotNull List errors = response.errors();
            for (Object error : errors) {
                Timber.d("got apollo response with error: %s", error.toString());
            }
        }
    }

    /**
     * We override this with a generic body so the subclasses don't have to override them. <br>
     * Mutations are used to update data at the backend, so you usually don't need to check if any
     * exception occurs. <br>
     * Displays the exception using timber.
     *
     * @param e ApolloException to be displayed by timber.
     */
    @Override
    public void onFailure(@NonNull ApolloException e) {
        Timber.e(e, "Mutation failed");
    }

    /**
     * Requests to enqueue this mutation in the {@link TaskDistributor}.
     */
    @Override
    public void request() {
        TaskDistributor.getInstance().request(this);
    }

    /**
     * Gets the type of the task.
     *
     * @return mutation type.
     */
    @Override
    protected final Type getType() {
        return Type.MUTATION;
    }
}
