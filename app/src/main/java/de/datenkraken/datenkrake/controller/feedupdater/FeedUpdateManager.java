package de.datenkraken.datenkrake.controller.feedupdater;

import android.os.AsyncTask;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.OnLifecycleEvent;

import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

/**
 * Class handling the {@link Article} in the database depending on the {@link Source}s.<br>
 * Downloads and inserts new {@link Article}s periodically or on specific events.<br>
 * Should be added as an observer to a lifecycle object to work correctly.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class FeedUpdateManager implements LifecycleObserver {

    public final FeedUpdater feedUpdater;

    private static FeedUpdateManager instance;

    // setting this to -1 so the Timer will throw an error if it gets started, before these Constants got set.
    private long articleMaxAge = -1;
    private long updateInterval = -1;

    private final LiveData<List<Source>> allSources;
    private final AppDatabase database;
    private Timer feedUpdateTimer;

    /**
     * Constructor for the class, initializing it.
     * Creates a new {@link FeedUpdater}, and loads all {@link Source}s of the user into
     * {@link #allSources}. Init the subscription to these {@link Source}s.
     *
     * @param db database to get the {@link Source}s from and create the {@link FeedUpdater}.
     */
    private FeedUpdateManager(AppDatabase db) {
        Timber.tag("FeedUpdateManager");

        database = db;

        allSources = db.daoSource().getAllSourceNotMarkedAsync();
        feedUpdater = new FeedUpdater(db);
        subscribeToDatabase();
        feedUpdateTimer = new Timer(true);
    }

    /**
     * Provides the FeedUpdateManager instance.<br>
     * Will initialize it if necessary.
     *
     * @param db Database instance, required to fetch {@link Source}s and insert {@link Article}s.
     * @return FeedUpdateManager instance
     */
    public static synchronized FeedUpdateManager getInstance(AppDatabase db) { //NOPMD
        if (instance == null) {
            instance = new FeedUpdateManager(db);
        }
        return instance;
    }

    /**
     * Can be called to update all {@link Source}s manually, using
     * {@link FeedUpdater#updateArticlesFromSources(List)}.
     */
    public void dispatchUpdate() {
        AsyncTask.execute(() -> feedUpdater.updateArticlesFromSources(allSources.getValue()));
    }

    /**
     * Subscribes an observer to changes in the source table of {@link AppDatabase}.<br>
     * The observer handles {@link Article} refreshing and deleting depending on its {@link Source}.
     */
    private void subscribeToDatabase() {
        allSources.observeForever(this::updateIfRequired);
    }

    /**
     * Updates {@link Source}s if required, using {@link FeedUpdater#updateArticlesFromSources(List)}
     * and kills download tasks, using {@link FeedUpdater#purgeOrphanTasks(Set)},
     * for {@link Source}s which no longer exists or are marked as deleted.
     *
     * @param sources new List of  {@link Source}s
     */
    private void updateIfRequired(List<Source> sources) {
        List<Source> toUpdate = new ArrayList<>();
        Set<Long> uids = new HashSet<>();

        if (sources != null) {
            Date maxAge = new Date(System.currentTimeMillis() - updateInterval);

            for (Source source : sources) {
                uids.add(source.uid);
                if (source.updated == null || source.updated.before(maxAge)) {
                    toUpdate.add(source);
                }
            }
        }

        AsyncTask.execute(() -> {
            feedUpdater.updateArticlesFromSources(toUpdate);
            feedUpdater.purgeOrphanTasks(uids);
        });
    }

    /**
     * Called when the observed lifecycle enters the on_create state.
     * Starts the {@link FeedUpdater} and creates a new {@link Timer} to update the {@link Source}s
     * at a fixed rate.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        feedUpdateTimer = new Timer(true);
        feedUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Date maxAge = new Date(System.currentTimeMillis() - articleMaxAge);
                database.daoArticle().deleteAllArticleUnsavedByAgeSync(maxAge);
                updateIfRequired(allSources.getValue());
            }
        }, 0, 300000);
    }

    /**
     * Called when the observed lifecycle enters the on_destroy state.
     * Resets, the update time of all sources and kills all remaining download tasks.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        feedUpdateTimer.cancel();
        feedUpdater.purgeAllDownloadTasks();
    }

    /**
     * Updates the update interval for the {@link Source}s.
     *
     * @param newIntervalTime new update interval for the {@link Source}s.
     */
    public void updateIntervalTime(long newIntervalTime) {
        updateInterval = newIntervalTime;
    }

    /**
     * Updates the max age of {@link Article}s.
     *
     * @param newCacheTime new maximal age for articles.
     */
    public void updateCacheTime(long newCacheTime) {
        articleMaxAge = newCacheTime;
    }
}
