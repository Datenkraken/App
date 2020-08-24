package de.datenkraken.datenkrake;

import android.app.Application;

import de.datenkraken.datenkrake.authentication.AuthenticationManager;
import de.datenkraken.datenkrake.controller.feedupdater.FeedUpdateManager;
import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.logging.L;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.repository.ArticleRepository;
import de.datenkraken.datenkrake.repository.SourceRepository;

import timber.log.Timber;

/**
 * Base class containing all other components. <br>
 * Customized to add Timber initialization. <br>
 * Here, the database, repositories, {@link FeedUpdateManager} and {@link AuthenticationManager} can be called.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 */
public class DatenkrakeApp extends Application {

    /**
     * onCreate() gets called when the application is starting,
     * before any other objects have been created. <br>
     * Timber gets initialized in this function.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        L.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    /**
     * Get the Database instance of the app.
     *
     * @return the database of the app.
     */
    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this);
    }

    /**
     * Get the repository to access the cached articles.
     *
     * @return the {@link ArticleRepository} instance from the database.
     */
    public ArticleRepository getArticleRepository() {
        return ArticleRepository.getInstance(getDatabase());
    }

    /**
     * Get the repository to access the saved {@link Source}s.
     *
     * @return the {@link SourceRepository} instance from the database.
     */
    public SourceRepository getSourceRepository() {
        return SourceRepository.getInstance(getDatabase());
    }

    /**
     * Returns the {@link FeedUpdateManager}.
     *
     * @return the {@link FeedUpdateManager} instance from the database.
     */
    public FeedUpdateManager getFeedUpdateManager() {
        return FeedUpdateManager.getInstance(getDatabase());
    }

    /**
     * Get the {@link AuthenticationManager} instance.
     *
     * @return the created {@link AuthenticationManager} instance.
     */
    public AuthenticationManager getAuthenticationManager() {
        return AuthenticationManager.create(this);
    }
}
