package de.datenkraken.datenkrake.ui.settings;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.MainActivity;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.network.clients.okhttp.OkHttpTask;
import de.datenkraken.datenkrake.surveillance.EventManager;
import de.datenkraken.datenkrake.util.Event;

import java.io.File;
import java.io.IOException;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

/**
 * Implements the ViewModel for the {@link SettingsPageFragment}.
 * With the option to delete the Cache, set the update interval, set the cache time,
 * Logout and delete the saved data and the account.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Jan Klinkmann - jan.klinkmann@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class SettingsPageViewModel extends AndroidViewModel {

    final MutableLiveData<Event<Boolean>> cacheStatus;
    final MutableLiveData<DeleteState> deleteState;
    final Application application;

    /**
     * Enum displaying the different states of the data delete function.
     * Can be set to failure or success.
     */
    enum DeleteState {
        FAILURE,
        SUCCESS
    }

    /**
     * Constructor to create a SettingsPageViewModel instance. <br>
     * Creates the MutableLiveData cacheStatus and deleteState.
     *
     * @param application the current {@link Application} instance.
     */
    public SettingsPageViewModel(@NonNull Application application) {
        super(application);
        this.cacheStatus = new MutableLiveData<>();
        this.deleteState = new MutableLiveData<>();
        this.application = application;
    }

    /**
     * Getter for the cache status live data.
     *
     * @return the live data for cache status as LiveData {@link Event}.
     */
    MutableLiveData<Event<Boolean>> getCacheStatus() {
        return cacheStatus;
    }

    /**
     * Getter for the delete status.
     *
     * @return live data for the delete status.
     */
    MutableLiveData<DeleteState> getDeleteState() {
        return deleteState;
    }

    /**
     * Initializes the asynchronous delete of the whole cache.
     * For this, it executes the AsyncTask {@link DeleteCache}.
     */
    void deleteCache() {
        ((DatenkrakeApp) getApplication()).getArticleRepository().clearAll();
        AsyncTask.execute(new DeleteCache(getApplication().getCacheDir()));
    }

    /**
     * Updates the interval time of the
     * {@link de.datenkraken.datenkrake.controller.feedupdater.FeedUpdater}.
     *
     * @param time new interval time.
     */
    void setUpdateInterval(String time) {
        try {
            long l = Long.parseLong(time);
            ((DatenkrakeApp) getApplication()).getFeedUpdateManager().updateIntervalTime(l);
        } catch (NumberFormatException e) {
            Timber.e(e, "Wrong Format for interval");
        }
    }

    /**
     * Updates the cache time of {@link Article}s of the
     * {@link de.datenkraken.datenkrake.controller.feedupdater.FeedUpdater}.
     *
     * @param time new interval time
     */
    void setCacheTime(String time) {
        try {
            long l = Long.parseLong(time);
            ((DatenkrakeApp) getApplication()).getFeedUpdateManager().updateCacheTime(l);
        } catch (NumberFormatException e) {
            Timber.e(e, "Wrong Format for cache time");
        }
    }


    /**
     * Helper class to clear the cache asynchronously.
     */
    private class DeleteCache implements Runnable {
        final File cacheDir;

        /**
         * Constructor for the class, initializing it.
         *
         * @param cacheDir from which the delete is executed.
         */
        DeleteCache(File cacheDir) {
            this.cacheDir = cacheDir;
        }

        /**
         * Calls deleteDir with cacheDir and sets an {@link Event} for the success of the function.
         */
        @Override
        public void run() {
            if (!deleteDir(cacheDir)) {
                cacheStatus.postValue(new Event<>(false));
            } else {
                cacheStatus.postValue(new Event<>(true));
            }
        }

        /**
         * Goes through directory recursively and deletes Files.
         *
         * @param dir from which the deleteDir is executed
         * @return boolean, for function success
         */
        private boolean deleteDir(File dir) {
            if (dir == null) {
                return false;
            }
            if (!(dir.isDirectory())) {
                return dir.delete();
            }
            String[] child = dir.list();
            if (child != null) {
                for (String s : child) {
                    boolean success = deleteDir(new File(dir, FilenameUtils.getName(s)));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        }
    }

    /**
     * Sends a request to delete the data and account of the user by calling {@link DeleteOkHttp}. <br>
     * Sets deleteState to display status of the delete. <br>
     * Also clears cache when called.
     * @param activity required to kill workers and clear user data via system service
     */
    public void deleteData(MainActivity activity) {
        deleteCache();
        activity.killWorker();
        EventManager.getInstance().cleanCache(activity.getBaseContext());

        // Get Auth State and Service.
        AuthState authState =
            ((DatenkrakeApp) application).getAuthenticationManager().getAuthState();
        AuthorizationService authorizationService =
            new AuthorizationService(application.getApplicationContext());

        // Send delete request.
        authState.performActionWithFreshTokens(authorizationService, (accessToken, idToken, ex) -> {
            DeleteOkHttp task = new DeleteOkHttp(
                accessToken,
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE)));
            task.request();
            authorizationService.dispose();
        });

    }

    /**
     * Wrapper function used to clear the authentication state via the
     * {@link de.datenkraken.datenkrake.authentication.AuthenticationManager}.
     */
    void logoutUser() {
        ((DatenkrakeApp) application).getAuthenticationManager().clearAuthState();
    }

    void clearRemainingData(ActivityManager manager) {
        manager.clearApplicationUserData();
    }

    /**
     * Requests a delete of the data and the account.
     */
    private class DeleteOkHttp extends OkHttpTask {
        private final String accessToken;
        private final ActivityManager manager;

        /**
         * Constructor for DeleteOkHttp, initializing it. <br>
         * Sets the accessToken of the user.
         *
         * @param token access token of user.
         */
        DeleteOkHttp(String token, ActivityManager manager) {
            this.accessToken = token;
            this.manager = manager;
        }

        /**
         * Sets a request with header, in which the access token is sent.
         *
         * @return request to be sent.
         */
        @Override
        public Request getRequest() {
            return new Request.Builder()
                .post(new RequestBody() {

                    /**
                     * Returns a media type of the request.
                     *
                     * @return media type parsed from "".
                     */
                    @Nullable
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("");
                    }

                    @Override
                    public void writeTo(@NotNull BufferedSink bufferedSink) {
                        // Does nothing here.
                    }
                })
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(application
                    .getString(R.string.preference_settings_data_delete_target_url))
                .build();
        }

        /**
         * Called upon failure of the request. Sets the enum deleteState to Failure
         * and displays a possible IOException using timber.
         *
         * @param call the of request.
         * @param e IOException thrown.
         */
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            Timber.e(e, "could not delete data");
            // Display failure.
            deleteState.postValue(DeleteState.FAILURE);
        }

        /**
         * Called upon success. Sets the value of delete State to Success, if the response code is
         * between 200 and 300, else sets the value to Failure. <br>
         * Uses timber to display the returned code.
         *
         * @param call of the request.
         * @param response from the request.
         */
        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            clearRemainingData(manager);
            deleteState.postValue(DeleteState.SUCCESS);
        }
    }

}
