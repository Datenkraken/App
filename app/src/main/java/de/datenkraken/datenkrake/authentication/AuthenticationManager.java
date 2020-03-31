package de.datenkraken.datenkrake.authentication;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.datenkraken.datenkrake.BuildConfig;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.util.ConnectionBuilderForTesting;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenResponse;
import net.openid.appauth.connectivity.ConnectionBuilder;
import net.openid.appauth.connectivity.DefaultConnectionBuilder;

import org.json.JSONException;

import timber.log.Timber;

/**
 * Manages the authentication state.
 * It will load / save and provide the reference to the current {@link AuthState} instance.
 *
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 */
public final class AuthenticationManager {

    private static AuthenticationManager instance;

    private AuthState authState;

    private final Context context;

    private final Uri redirectUri;
    private final Uri authorizeEndpoint;
    private final Uri tokenEndpoint;
    private final String clientId;

    private AuthenticationManager(@NonNull Context context) {
        this.context = context;

        redirectUri = Uri.parse(context.getString(R.string.login_redirect_uri));
        authorizeEndpoint = Uri.parse(context.getString(R.string.login_authorize_endpoint));
        tokenEndpoint = Uri.parse(context.getString(R.string.login_token_endpoint));
        clientId = context.getString(R.string.login_client_id);

        loadAuthState();
    }

    /**
     * Provides the AuthenticationManager instance.
     * Will initialize it if necessary.
     *
     * @param context Context used to get Application Context, required for the SharedPreferences.
     * @return AuthenticationManager instance.
     */
    public static synchronized AuthenticationManager create(Context context) { //NOPMD
        if (instance == null) {
            instance = new AuthenticationManager(context);
        }
        return instance;
    }

    /**
     * Returns the {@link AuthorizationRequest} instance, pre configured with all information needed
     * to make a valid request to the server.
     *
     * @return the AuthorizationRequest instance.
     */
    public AuthorizationRequest getAuthorizationRequest() {
        AuthorizationServiceConfiguration config =
            new AuthorizationServiceConfiguration(authorizeEndpoint, tokenEndpoint);

        return new AuthorizationRequest.Builder(
            config,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        ).build();
    }

    /**
     * Chooses the correct {@link ConnectionBuilder} instance. <br>
     * Normally it will return an instance of {@link DefaultConnectionBuilder}. <br>
     * It will return an instance of {@link ConnectionBuilderForTesting}, when the app is built
     * in DEBUG mode.
     *
     * @return the ConnectionBuilder instance
     */
    public ConnectionBuilder getConnectionBuilder() {
        if (BuildConfig.DEBUG) {
            return ConnectionBuilderForTesting.INSTANCE;
        }
        return DefaultConnectionBuilder.INSTANCE;
    }

    /**
     * Get the current {@link AuthState} instance.
     * This will provide access to the token, etc.
     *
     * @return the current AuthState instance.
     */
    @NonNull
    public AuthState getAuthState() {
        return authState;
    }

    /**
     * Update the current {@link AuthState} instance with a new {@link AuthorizationResponse} and
     * {@link AuthorizationException}. This is used by the AuthState internally to extract the
     * Authorization code and determine/save the status of the authentication flow.
     * This is usually called with the results of an
     * {@link AuthorizationService#performAuthorizationRequest(AuthorizationRequest, PendingIntent)}
     * call.
     *
     * @param response the response from the authorization request.
     * @param exception the exception from the authorization request.
     */
    public void updateAuthStateWithAuthorizationResponse(@Nullable AuthorizationResponse response,
                                                         AuthorizationException exception) {
        authState.update(response, exception);
        saveAuthState();
    }

    /**
     * Update the current {@link AuthState} instance with a new {@link TokenResponse} and
     * {@link AuthorizationException}. This is used by the AuthState internally to extract the
     * AccessToken, RefreshToken and to determine if the user is successfully authenticated.
     * This is usually called with the results of a
     * {@link AuthorizationResponse#createTokenExchangeRequest()}.
     *
     * @param response the response of the token exchange request.
     * @param exception the exception of the token exchange request.
     */
    public void updateAuthStateWithTokenResponse(@Nullable TokenResponse response,
                                                 @Nullable AuthorizationException exception) {
        authState.update(response, exception);
        saveAuthState();
    }

    /**
     * This will clear the current {@link AuthState} instance and create a new empty instance,
     * which will be persisted to the disk, to override the saved state.
     */
    public void clearAuthState() {
        authState = new AuthState();
        saveAuthState();
    }

    /**
     * Gets the {@link AuthState} as a string from the SharedPreference LOGIN_STORAGE
     * using the key AUTH_TOKEN. <br>
     * If the string is null or empty, it will create a new AuthState. <br>
     * Otherwise it will try to deserialize the string using JSon, receiving the AuthState. <br>
     * If the string can not be deserialized, it will display an error using timber and create a new AuthState
     */
    private void loadAuthState() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.preference_login_storage),
            Context.MODE_PRIVATE);
        String serializedState = sharedPreferences.getString(
            context.getString(R.string.login_authentication_state), null);

        // Return when we do not have any state saved
        if (serializedState == null || serializedState.isEmpty()) {
            authState = new AuthState();
            return;
        }

        try {
            authState = AuthState.jsonDeserialize(serializedState);
        } catch (JSONException e) {
            Timber.e(e, "Could not deserialize the authentication state!");
            authState = new AuthState();
        }
    }

    /**
     * Saves the {@link AuthState} compressed as a string, by using JSon, in the SharedPreference
     * LOGIN_STORAGE under the key AUTH_TOKEN.
     */
    @SuppressLint("ApplySharedPref")
    private void saveAuthState() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.preference_login_storage),
            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.login_authentication_state), authState.jsonSerializeString());
        editor.commit();
    }
}
