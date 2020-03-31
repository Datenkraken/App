package de.datenkraken.datenkrake.controller.feedupdater;

import androidx.collection.LongSparseArray;

import com.rometools.rome.io.FeedException;

import de.datenkraken.datenkrake.controller.feedupdater.rss.OkHttpFeed;
import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.network.clients.okhttp.OkHttpTask;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import okhttp3.Call;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

/**
 * Manages running download tasks updating {@link Article}s in the database.
 * Can create new download tasks and purge old ones.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class FeedUpdater {

    private final LongSparseArray<OkHttpTask> downloadTasks = new LongSparseArray<>();

    public AppDatabase database;

    FeedUpdater(AppDatabase db) {
        database = db;
    }

    /**
     * Requests via {@link OkHttpFeed} new {@link Article}s from the given {@link Source}.<br>
     * After successful receiving, it deletes old ones and inserts the new ones. <br>
     * Also updates {@link Source#updated}. <br>
     * If the request does not succeed, displays this, using timber.
     *
     * @param source {@link Source} of the {@link Article}s to refresh.
     */
    private void updateArticlesFromSource(Source source) {
        if (downloadTasks.containsKey(source.uid)) {
            return;
        }
        OkHttpTask task = new OkHttpFeed(source) {

            /**
             * Called on success of the task. <br>
             * Sets the updated date of the {@link Source} in the
             * {@link de.datenkraken.datenkrake.db.DaoSource} to the current date, and updates the
             * {@link Article}s in the {@link de.datenkraken.datenkrake.db.DaoArticle}.
             *
             * @param articles List of articles
             */
            @Override
            public void onSuccessfulParsed(@NotNull List<Article> articles) {
                downloadTasks.remove(source.uid);
                source.updated = new Date();
                database.daoSource().updateOneSourceSync(source);
                database.daoArticle().insertOrUpdateAllArticleSync(articles);
            }

            /**
             * Called on failure of the task. Displays a message, that the task has failed, using timber.
             *
             * @param call that has failed.
             * @param e to be displayed.
             */
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Timber.e(e, "Failed to connect to %s", call.request().url().toString());
            }

            /**
             * Called, when an exception is thrown. Displays a message with the exception using timber.
             *
             * @param e FeedException to be displayed.
             */
            @Override
            public void onFeedException(@NotNull FeedException e) {
                Timber.e(e, "Failed to parse feed");
            }
        };

        downloadTasks.put(source.uid, task);
        task.request();
    }

    /**
     * Requests via {@link OkHttpFeed} new {@link Article}s from the given {@link Source}s.<br>
     * After successful receiving, it deletes the old ones, using {@link FeedUpdater#purgeAllDownloadTasks()}
     * and inserts the new ones, using {@link FeedUpdater#updateArticlesFromSource(Source)}.
     *
     * @param sources List of {@link Source}s of the {@link Article}s to refresh.
     */
    void updateArticlesFromSources(@Nullable List<Source> sources) {
        if (sources != null) {
            purgeAllDownloadTasks();
            for (Source source : sources) {
                updateArticlesFromSource(source);
            }
        }
    }

    /**
     * Cancels all running {@link OkHttpTask}s.
     */
    void purgeAllDownloadTasks() {
        int size = downloadTasks.size();
        for (int i = 0; i < size; i++) {
            downloadTasks.valueAt(i).cancel();
        }
        downloadTasks.clear();
    }

    /**
     * Checks for queried or running download tasks for {@link Source}s which no longer exists and
     * kills them.
     *
     * @param uids Set of uids of existing {@link Source}s.
     */
    void purgeOrphanTasks(Set<Long> uids) {
        int size = downloadTasks.size();
        long key;
        OkHttpTask value;
        for (int i = 0; i < size; i++) {
            key = downloadTasks.keyAt(i);
            value = downloadTasks.valueAt(i);
            if (value == null) {
                downloadTasks.remove(key);
                continue;
            }
            if (!uids.contains(key)) {
                value.cancel();
                downloadTasks.remove(key);
            }
        }
    }

    /**
     * Cancels and removes a download task, identified by the uid of the corresponding {@link Source}.
     *
     * @param uid uid of a {@link Source} to identify the download task.
     */
    public void purgeDownloadTask(long uid) {
        if (downloadTasks.containsKey(uid)) {
            OkHttpTask task = downloadTasks.get(uid);
            if (task != null) {
                task.cancel();
            }
            downloadTasks.remove(uid);
        }
    }

}
