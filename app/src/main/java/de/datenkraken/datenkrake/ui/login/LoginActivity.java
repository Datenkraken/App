package de.datenkraken.datenkrake.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import butterknife.BindView;
import butterknife.ButterKnife;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.MainActivity;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.authentication.AuthenticationManager;
import de.datenkraken.datenkrake.ui.datacollection.DataCollectionPopupFragment;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import timber.log.Timber;

/**
 * The login activity is used as a gateway and the first activity, that is called,
 * when opening the app.
 * This will ensure that a user has to login to proceed with a valid AccessToken.
 * The Token is also refreshed, when the app is opened.
 *
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 */
public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_screen_login_button) Button loginButton;
    @BindView(R.id.login_screen_register_button) Button goToRegisterButton;

    private AuthorizationService authorizationService;
    private AuthenticationManager authenticationManager;

    private static final int ACTIVITY_RESULT_AUTH = 2020;

    /**
     * Called, when the Activity is created. <br>
     * Retrieves the SharedPreferences from the LOGIN_STORAGE and, if ACCEPTED_DATA_COLLECTION is false,
     * it will display a {@link DataCollectionPopupFragment} that the user has to accept to proceed. <br>
     * Also creates a new {@link AuthenticationManager} and {@link AuthorizationService}, and checks,
     * if the user is authorized. If this is the case, it redirects to the {@link MainActivity}, else
     * it calls {@link LoginActivity#setupUI()}.
     *
     * @param savedInstanceState bundle of instance sent to the function.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authenticationManager = ((DatenkrakeApp) getApplication()).getAuthenticationManager();

        AppAuthConfiguration.Builder builder = new AppAuthConfiguration.Builder();
        builder.setConnectionBuilder(authenticationManager.getConnectionBuilder());

        authorizationService = new AuthorizationService(this, builder.build());
        authenticationManager.getAuthState().performActionWithFreshTokens(authorizationService,
            (accessToken, idToken, exception) -> {
                if (authenticationManager.getAuthState().isAuthorized()) {
                    redirectOnSuccess();
                } else {
                    setupUI();
                }
            });
    }

    /**
     * Sets the view of the activity and sets the on click listener to the buttons login and register.
     */
    private void setupUI() {
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // On click listener for Login Button.
        loginButton.setOnClickListener(v -> performAuth());

        // On click listener for Register Button.
        goToRegisterButton.setOnClickListener(
            v -> openInBrowser(Uri.parse(getString(R.string.login_registration_endpoint))));

        DataCollectionPopupFragment collectionFragment = new DataCollectionPopupFragment(findViewById(R.id.login));

        if (!collectionFragment.isAdded()) {

            collectionFragment.setCancelable(false);
            collectionFragment.show(getSupportFragmentManager(), "dataCollectionPopup");
        }
    }

    /**
     * Redirects to the {@link MainActivity} and closes this activity.
     */
    private void redirectOnSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Displays a Toast message that the login has failed.
     */
    private void showLoginFailed() {
        Toast.makeText(getApplicationContext(), R.string.login_screen_login_failure, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called, when the activity is destroyed. Disposes the currently used {@link AuthorizationService}.
     */
    @Override
    public void onDestroy() {
        if (authorizationService != null) {
            authorizationService.dispose();
        }

        super.onDestroy();
    }

    /**
     * Opens the targetPage in a browser using custom tabs.
     *
     * @param targetPage to be opened.
     */
    private void openInBrowser(Uri targetPage) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, targetPage);
    }

    /**
     * Start the authorization flow. <br>
     * Gets an {@link AuthorizationRequest} from the {@link AuthenticationManager} and creates an intent
     * with it through {@link AuthorizationService#getAuthorizationRequestIntent(AuthorizationRequest)}. <br>
     * Starts an activity waiting for a result, with the request code ACTIVITY_RESULT_AUTH. <br>
     * This has to be called on another thread.
     */
    private void performAuth() {
        AuthorizationRequest request = authenticationManager.getAuthorizationRequest();

        Intent requestAuthIntent = authorizationService.getAuthorizationRequestIntent(request);
        startActivityForResult(requestAuthIntent, ACTIVITY_RESULT_AUTH);
    }

    /**
     * Uses {@link AuthenticationManager#updateAuthStateWithAuthorizationResponse(AuthorizationResponse, AuthorizationException)}
     * to update the {@link AuthenticationManager}. If the response is not null, it will get a
     * {@link TokenRequest} and call
     * {@link AuthorizationService#performTokenRequest(TokenRequest, AuthorizationService.TokenResponseCallback)}
     * with it. Else it will use timber to display the exception and show a message to the user.
     *
     * @param response {@link AuthorizationResponse} for the user to authorize with.
     * @param exception {@link AuthorizationException} exception thrown by the authorization.
     */
    private void handleAuthorizationTokenResponse(AuthorizationResponse response,
                                                  AuthorizationException exception) {
        authenticationManager.updateAuthStateWithAuthorizationResponse(response, exception);
        Timber.i("Authorization token received");
        if (response != null) {
            // Try requesting an access token with the authorization code received.
            TokenRequest request = response.createTokenExchangeRequest();
            authorizationService.performTokenRequest(request, this::handleAccessTokenResponse);
        } else {
            Timber.e(exception, "Login failed:");

            // Display error.
            showLoginFailed();
        }
    }

    /**
     * Uses {@link AuthenticationManager#updateAuthStateWithTokenResponse(TokenResponse, AuthorizationException)}
     * to update the {@link AuthenticationManager}. Then checks, if the user is authorized. If this is
     * th case, redirects to the {@link MainActivity}, else displays a message with Timber and displays
     * an error message.
     *
     * @param response {@link TokenResponse} used for the authentication.
     * @param exception {@link AuthorizationException} exception thrown by the authorization.
     */
    private void handleAccessTokenResponse(TokenResponse response,
                                           AuthorizationException exception) {
        authenticationManager.updateAuthStateWithTokenResponse(response, exception);

        if (authenticationManager.getAuthState().isAuthorized()) {
            redirectOnSuccess();
        } else {
            Timber.e(exception, "Token exchange failed:");

            // Display error.
            showLoginFailed();
        }
    }

    /**
     * Called when an activity returns with a result. <br>
     * Checks, if this result has the request code ACTIVITY_RESULT_AUTH. If this is the case, it will get the
     * {@link AuthorizationResponse} and {@link AuthorizationException} form the data from the activity
     * and call {@link LoginActivity#handleAccessTokenResponse(TokenResponse, AuthorizationException)}
     * with it.
     *
     * @param requestCode to identify the activity.
     * @param resultCode to identify the result.
     * @param data returned by the intent.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if this was called after the user initiated the auth flow
        if (requestCode == ACTIVITY_RESULT_AUTH) {
            AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
            AuthorizationException exception = AuthorizationException.fromIntent(data);

            // Update the authentication state
            handleAuthorizationTokenResponse(response, exception);
        }
    }
}
