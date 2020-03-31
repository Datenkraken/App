package de.datenkraken.datenkrake.network.clients.okhttp;

import de.datenkraken.datenkrake.network.clients.Client;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import timber.log.Timber;

/**
 * {@link OkHttpClient} wrapper, able to execute task synchronous and asynchronous.
 * Provides functionality to clear its cache, and to enqueue and execute tasks.
 *
 * @author  Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class OkHttp extends Client<OkHttpTask> {
    private final OkHttpClient okHttpClient;

    /**
     * Constructor for this class. Instantiating the {@link OkHttpClient}.
     */
    public OkHttp() {
        super(OkHttpTask.class);

        Timber.tag("OkHttp");
        okHttpClient = new OkHttpClient().newBuilder()
            .retryOnConnectionFailure(true)
            .build();
    }

    /**
     * Tries to clear the cache of the {@link OkHttpClient}. <br>
     * On success, returns true, else logs the error and returns false.
     *
     * @return true, if clear was successful, else false.
     */
    @Override
    public boolean clearCache() {
        try (Cache cache = this.okHttpClient.cache()) {
            if (cache == null) {
                Timber.e("Failed to clear cache, no cache existing.");
                return true;
            }
            cache.delete();
            return true;
        } catch (IOException e) {
            Timber.e(e, "Failed to clear cache, tried to access cache during blocking IO-action");
        }
        return false;
    }

    /**
     * Requests to enqueue an {@link OkHttpTask} as a {@link Call}.
     *
     * @param task {@link OkHttpTask} to enqueue.
     */
    @Override
    public void enqueue(OkHttpTask task) {
        if (task == null) {
            return;
        }
        Call call = okHttpClient.newCall(task.getRequest());
        call.enqueue(task);
        task.call = call;
    }

    /**
     * Executes a {@link Call} and handles the response of the call with
     * {@link OkHttpTask#onResponse(Call, Response)} and {@link OkHttpTask#onFailure(Call, IOException)}.
     *
     * @param task {@link OkHttpTask} to execute.
     */
    @Override
    public void execute(OkHttpTask task) {
        if (task == null) {
            return;
        }

        Call call = okHttpClient.newCall(task.getRequest());
        try (Response response = call.execute()) {
            task.onResponse(call, response);
        } catch (IOException e) {
            task.onFailure(call, e);
        }
    }

    /**
     * Returns the OkHttpClient. <br>
     * The OkHttpClient is often required by other network clients.
     * (see {@link de.datenkraken.datenkrake.network.clients.apollo.Apollo}).
     *
     * @return {@link OkHttpClient}
     */
    public OkHttpClient getClient() {
        return okHttpClient;
    }
}
