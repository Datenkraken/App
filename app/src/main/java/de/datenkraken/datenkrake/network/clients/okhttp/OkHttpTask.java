package de.datenkraken.datenkrake.network.clients.okhttp;

import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.TaskDistributor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

/**
 * Class representing a task for OkHttp Queries.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public abstract class OkHttpTask implements ITask, Callback {
    // call created by the TaskDistributor
    public Call call;

    /**
     * Returns the appropriate Request for this task used by an OkHttpClient.
     *
     * @return Request
     */
    public abstract Request getRequest();

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
     * Requests to enqueue this task in the {@link TaskDistributor}.
     */
    @Override
    public void request() {
        TaskDistributor.getInstance().request(this);
    }

    /**
     * Returns the class of the {@link de.datenkraken.datenkrake.network.clients.Client}
     * this task wants to get executed by.
     *
     * @return the OkHttp task.
     */
    @Override
    public final Class<OkHttp> processedBy() {
        return OkHttp.class;
    }
}
