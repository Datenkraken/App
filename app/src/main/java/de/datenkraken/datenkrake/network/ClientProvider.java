package de.datenkraken.datenkrake.network;

import android.content.Context;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.network.clients.Client;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;

import net.openid.appauth.AuthState;

import timber.log.Timber;

/**
 * Contains all instantiated {@link Client}s, providing them to other Classes and
 * instantiating them, if required.
 *
 * @author  Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ClientProvider {

    public final String SERVER_URL;
    private AuthState authState;
    private final AbstractMap<Class<? extends Client<? extends ITask>>, Client<? extends ITask>> clients;
    private final WeakReference<Context> context;

    /**
     * Constructor for the class, initializing it. <br>
     * Saves context to provide it to {@link Client}s and extract necessary constants from xml
     * resources.
     *
     * @param context to be used by instantiated {@link Client}s
     */
    ClientProvider(Context context) {
        Timber.tag("ClientProvider");
        this.context = new WeakReference<>(context);
        clients = new HashMap<>();
        SERVER_URL = context.getResources().getString(R.string.graphql_endpoint);
    }

    /**
     * Returns a {@link Client} identified by it's class. <br>
     * Instantiates the client if required and adds it to the {@link #clients}. <br>
     * If instantiating fails, it will log the error and return null.
     *
     * @param clientClass class of a {@link Client}.
     * @return the {@link Client} or null.
     */
    public Client getClient(Class<? extends Client<? extends ITask>> clientClass) {
        Client<? extends ITask> client = null;

        if (!clients.containsKey(clientClass)
            || clients.get(clientClass) == null) {
            try {
                client = instantiateClient(clientClass);
                clients.put(clientClass, client);
            } catch (Exception e) { // NOPMD
                Timber.e(e, "Couldn't instantiate the Client for %s", clientClass);
            }
        } else {
            client =  clients.get(clientClass);
        }

        return client;
    }

    /**
     * Tries to instantiate the given class of a {@link Client}. <br>
     * Searches through all accessible constructors of the given class prioritizing constructors
     * accepting a {@link ClientProvider}. <br>
     * Tries to use the default constructor if no prioritized constructor was found.
     *
     * @param clientClass class of {@link Client} to instantiate.
     * @return the instantiated {@link Client}.
     * @throws NoSuchMethodException thrown, if no suitable constructor was found.
     * @throws IllegalAccessException thrown, if this constructor object is enforcing Java
     language access control and the underlying constructor is inaccessible.
     * @throws InstantiationException thrown, if the class that declares the underlying constructor
     represents an abstract class.
     * @throws InvocationTargetException thrown, if the underlying constructor throws an exception.
     */
    public Client<?> instantiateClient(Class<? extends Client<? extends ITask>> clientClass) throws
                                                                        NoSuchMethodException,
                                                                        IllegalAccessException,
                                                                        InstantiationException,
                                                                        InvocationTargetException {
        Constructor<?>[] constructors = clientClass.getConstructors();
        Class<?>[] parameters;
        Constructor<?> defaultConstructor = null;

        for (Constructor<?> constructor : constructors) {
            parameters = constructor.getParameterTypes();
            if (parameters.length == 1 && parameters[0] == this.getClass()) {
                return (Client) constructor.newInstance(this);
            } else if (parameters.length == 0) {
                defaultConstructor = constructor;
            }
        }
        if (defaultConstructor != null) {
            return (Client) defaultConstructor.newInstance();
        }

        throw new NoSuchMethodException("Client " + clientClass + " doesn't provide a suitable constructor.\n"
            + "Client(ClientProvider) or Client() is required");
    }

    /**
     * Returns a collection of all instantiated {@link Client}s.
     *
     * @return a Collection of {@link Client}s.
     */
    public Collection<Client<? extends ITask>> getInstantiatedClients() {
        return clients.values();
    }

    /**
     * Returns the current {@link AuthState}.
     *
     * @return the current {@link AuthState}.
     */
    public AuthState getAuthState() {
        return authState;
    }

    /**
     * Returns a {@link WeakReference} to the current context of the class.
     *
     * @return a {@link WeakReference} to context.
     */
    public WeakReference<Context> getContext() {
        return context;
    }

    /**
     * Sets the {@link AuthState} for this class.
     *
     * @param state new {@link AuthState} for this class.
     */
    void setAuthState(AuthState state) {
        authState = state;
    }
}
