package de.datenkraken.datenkrake.network.clients.apollo;

import android.content.Context;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.interceptor.ApolloInterceptor;
import com.apollographql.apollo.interceptor.ApolloInterceptorChain;
import com.apollographql.apollo.request.RequestHeaders;

import de.datenkraken.datenkrake.network.ClientProvider;
import de.datenkraken.datenkrake.network.clients.Client;
import de.datenkraken.datenkrake.network.clients.okhttp.OkHttp;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import kotlin.NotImplementedError;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * {@link ApolloClient} wrapper, able to execute tasks asynchronous. <br>
 * Provides functionality to clear its cache.
 *
 * @author  Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class Apollo extends Client<ApolloTask> {

    private final ApolloClient apolloClient;
    final AuthState authState;
    final WeakReference<Context> context;


    /**
     * Constructor for the class, instantiating the {@link ApolloClient}.
     * Requires the {@link ClientProvider} to decorate the {@link ApolloClient} with the
     * {@link okhttp3.OkHttpClient}.
     *
     * @param clientProvider {@link ClientProvider} providing the {@link okhttp3.OkHttpClient}.
     */
    public Apollo(ClientProvider clientProvider) {
        super(ApolloTask.class);
        Timber.tag("Apollo");
        this.context = clientProvider.getContext();
        this.authState = clientProvider.getAuthState();
        apolloClient = ApolloClient.builder()
            .serverUrl(clientProvider.SERVER_URL)
            .addApplicationInterceptor(new AuthenticationInterceptor())
            .okHttpClient(((OkHttp) clientProvider.getClient(OkHttp.class)).getClient())
            .build();
    }

    /**
     * Clears the cache of the {@link ApolloClient}.
     *
     * @return true, if {@link ApolloClient#clearNormalizedCache()} was successful, else false.
     */
    @Override
    public boolean clearCache() {
        apolloClient.clearHttpCache();
        return apolloClient.clearNormalizedCache();
    }

    /**
     * Enqueues a task in an {@link ApolloCall}. <br>
     * If the task is a query or mutation, gets the query or mutation and enqueues it.
     * After this, sets the call of the {@link ApolloTask}.
     *
     * @param task Task to execute.
     */
    @Override
    public void enqueue(ApolloTask task) {
        if (task == null) {
            return;
        }
        ApolloCall call;
        if (task.getType() == ApolloTask.Type.QUERY) {
            call = apolloClient.query(task.getQuery());
            call.enqueue(task);
        } else {
            call = apolloClient.mutate(task.getMutation());
            call.enqueue(task);
        }

        task.call = call;
    }

    /**
     * Apollo doesn't provide any functionality to execute task synchronous. <br>
     * So this function will throw an {@link NotImplementedError}.
     *
     * @param task Task to execute.
     */
    @Override
    public void execute(ApolloTask task) {
        throw new NotImplementedError("Apollo cant execute task synchronously");
    }

    /**
     * Apollo interceptor, which will append the current access token to an apollo request or mutation.
     */
    private class AuthenticationInterceptor implements ApolloInterceptor {

        private final AuthorizationService authorizationService;

        /**
         * Constructs of this class. Instantiates the {@link AuthorizationService} required to access
         * the access token. If no {@link #authState} is given or context is no longer valid, this
         * class won't append the access token.
         */
        AuthenticationInterceptor() {
            Timber.tag("AuthInterceptor");
            if (authState == null || context.get() == null) {
                authorizationService = null;
                return;
            }
            authorizationService = new AuthorizationService(context.get());
        }

        /**
         * Appends an access token to an apollo query/mutation header if possible. <br>
         * Ensures by using functionality of oauth
         * ({@link AuthState#performActionWithFreshTokens(AuthorizationService, AuthState.AuthStateAction)})
         * that the access token is valid.
         *
         * @param request
         * {@link com.apollographql.apollo.interceptor.ApolloInterceptor.InterceptorRequest} to be sent.
         * @param chain {@link ApolloInterceptorChain} to call
         * {@link ApolloInterceptorChain#proceedAsync(InterceptorRequest, Executor, CallBack)} with.
         * @param dispatcher to be given to
         * {@link ApolloInterceptorChain#proceedAsync(InterceptorRequest, Executor, CallBack)}.
         * @param callBack to be given to
         * {@link ApolloInterceptorChain#proceedAsync(InterceptorRequest, Executor, CallBack)}.
         */
        @Override
        public void interceptAsync(@NotNull InterceptorRequest request,
                                   @NotNull ApolloInterceptorChain chain,
                                   @NotNull Executor dispatcher,
                                   @NotNull CallBack callBack) {
            if (authorizationService == null) {
                chain.proceedAsync(request, dispatcher, callBack);
                return;
            }

            authState.performActionWithFreshTokens(authorizationService, (accessToken, idToken, ex) -> {
                InterceptorRequest newRequest = request.toBuilder().requestHeaders(
                    RequestHeaders.builder().addHeader("Authorization", "Bearer " + accessToken)
                        .build()).build();
                chain.proceedAsync(newRequest, dispatcher, callBack);
            });
        }

        /**
         * Disposes the {@link AuthorizationService}. Important, otherwise the access token might
         * leak.
         */
        @Override
        public void dispose() {
            authorizationService.dispose();
        }
    }
}
