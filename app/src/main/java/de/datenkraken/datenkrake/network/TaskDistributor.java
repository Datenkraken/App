package de.datenkraken.datenkrake.network;

import android.content.Context;

import androidx.annotation.NonNull;

import de.datenkraken.datenkrake.authentication.AuthenticationManager;
import de.datenkraken.datenkrake.network.clients.Client;

import java.util.Collection;

import timber.log.Timber;

/**
 * Distributes {@link ITask} to different clients, provided by {@link ClientProvider}.
 * This is a singleton to instance Clients and the {@link ClientProvider} only once.
 *
 * @author  Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class TaskDistributor {
    protected static TaskDistributor instance;
    private final ClientProvider clientProvider;

    /**
     * Constructor for this class, initializing it.
     *
     * @param context to be used for {@link ClientProvider}.
     */
    private TaskDistributor(Context context) {
        Timber.tag("TaskDistributor");
        clientProvider = new ClientProvider(context);
    }

    /**
     * Returns the singleton instance. <br>
     * Throws an {@link ExceptionInInitializerError}, when the instance of the class is null.
     *
     * @return the instance of this class.
     */
    public static synchronized TaskDistributor getInstance() { //NOPMD
        if (instance == null) {
            throw new ExceptionInInitializerError(
                "Tried to access the TaskDistributor instance without initializing it."
                + "did you forgot to call TaskDistributor.setup()?");
        }
        return instance;
    }

    /**
     * Sets up a new instance of the TaskDistributor without authentication. <br>
     * Must be called before calling getInstance().
     *
     * @param context this TaskDistributor should use.
     */
    public static void setup(Context context) {
        instance = new TaskDistributor(context);
    }

    /**
     * Sets up a new instance of the TaskDistributor with authentication. <br>
     * Must be called before calling getInstance(). <br>
     * Passes the {@link net.openid.appauth.AuthState} from the {@link AuthenticationManager}
     * to the {@link ClientProvider} in this class.
     *
     * @param context this TaskDistributor should use.
     * @param authenticationManager {@link AuthenticationManager} to use to get the
     * {@link net.openid.appauth.AuthState}.
     */
    public static void setup(AuthenticationManager authenticationManager, Context context) {
        setup(context);
        instance.clientProvider.setAuthState(authenticationManager.getAuthState());
    }

    /**
     * Asynchronous request enqueuing the {@link Client} described by the given {@link ITask}. <br>
     * Checks if the given {@link Client} is able to execute the task.
     *
     * @param task to enqueue.
     */
    public void request(ITask task) {
        Client client = clientProvider.getClient(task.processedBy());
        if (client != null && client.canProcess(task)) {
            client.enqueue(task);
        }
    }


    /**
     * Synchronous request enqueuing the {@link Client} described by the given {@link ITask}. <br>
     * Checks if the given {@link Client} is able to execute the task.
     *
     * @param task to enqueue.
     */
    public void synchronousRequest(@NonNull ITask task) {
        Client client = clientProvider.getClient(task.processedBy());
        if (client != null && client.canProcess(task)) {
            client.execute(task);
        }
    }

    /**
     * Clears the cache of all instantiated {@link Client}s, provided by the {@link ClientProvider}.
     *
     * @return false if it could not access the cache, true otherwise.
     */
    public boolean clearCache() {
        boolean cleared = true;
        Collection<Client<?>> providers = clientProvider.getInstantiatedClients();
        for (Client client : providers) {
            if (client != null) {
                cleared = cleared && client.clearCache();
            }
        }
        return cleared;
    }
}
