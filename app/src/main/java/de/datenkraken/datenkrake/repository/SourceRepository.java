package de.datenkraken.datenkrake.repository;

import android.os.AsyncTask;
import android.util.Pair;

import androidx.lifecycle.LiveData;

import de.datenkraken.datenkrake.controller.feedupdater.FeedUpdateManager;
import de.datenkraken.datenkrake.controller.feedupdater.FeedUpdater;
import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.controller.feedupdater.rss.OkHttpFeed;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.EventCollector;
import de.datenkraken.datenkrake.surveillance.actions.SourceAction;
import de.datenkraken.datenkrake.util.Helper;

import java.util.List;

import timber.log.Timber;

/**
 * A repository to retrieve insert and delete {@link Source}s from the database.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class SourceRepository {

    private static SourceRepository instance;
    private final AppDatabase database;

    /**
     * Constructor of the class.<br>
     * Sets database instance.
     *
     * @param database an instance of {@link AppDatabase}.
     */
    private SourceRepository(final AppDatabase database) {
        Timber.tag("SourceRepository");

        this.database = database;
    }

    /**
     * Returns the instance of this repository. <br>
     * This is implemented as a singleton.
     *
     * @param database an instance of {@link AppDatabase}.
     * @return the ArticleRepository instance.
     */
    public static synchronized SourceRepository getInstance(final AppDatabase database) { //NOPMD
        if (instance == null) {
            instance = new SourceRepository(database);
        }
        return instance;
    }

    /**
     * Returns a LiveData Object containing all {@link Source}s.
     *
     * @return LiveData containing all {@link Source}s.
     */
    public LiveData<List<Source>> getSources() {
        return database.daoSource().getAllSourceNotMarkedAsync();
    }

    /**
     * Inserts a {@link Source} into the Database asynchronously.
     *
     * @param source {@link Source} to insert.
     */
    public void insertSource(Source source) {

        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.SOURCEACTION)
            .with(new Pair<>(SourceAction.ADDED, source.url.toString())));

        source.uid = Helper.generateSourceUid(source);
        AsyncTask.execute(() -> database.daoSource().insertOneSourceSync(source));
    }

    /**
     * Marks and tries to delete the {@link Source} via {@link #deleteSourceIfEmpty(Source)} asynchronously. <br>
     * Cancels all {@link OkHttpFeed}s in {@link FeedUpdater}for the corresponding {@link Source}. <br>
     * Deletes unsaved {@link Article}s originating from the given source.
     *
     * @param source {@link Source} to delete.
     */
    public void deleteSource(Source source) {
        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.SOURCEACTION)
            .with(new Pair<>(SourceAction.REMOVED, source.url.toString())));

        AsyncTask.execute(() -> {
            FeedUpdateManager.getInstance(database).feedUpdater.purgeDownloadTask(source.uid);
            database.daoArticle().deleteAllArticleUnsavedBySourceUidSync(source.uid);
            database.daoSource().updateOneSourceMarkToDeleteSync(source);
            deleteSourceIfEmpty(source);
        });
    }

    /**
     * Deletes the {@link Source} if no more {@link Article}s originating from the given
     * {@link Source} exist in the database. <br>
     * Works asynchronously.
     *
     * @param source {@link Source} to delete.
     */
    void deleteSourceIfEmpty(Source source) {
        AsyncTask.execute(() -> {
            if (database.daoArticle().getArticleCountBySourceUidSync(source.uid) == 0) {
                database.daoSource().deleteOneSourceBySourceUidSync(source.uid);
            }
        });
    }

    /**
     * Returns the LiveData object that contains one {@link Source} identified by it's uid.
     *
     * @param uid uid of the {@link Source}.
     * @return LiveData containing one {@link Source} or null.
     */
    public LiveData<Source> getSource(long uid) {
        return database.daoSource().getOneSourceByIdAsync(uid);
    }
}

