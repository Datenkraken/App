package de.datenkraken.datenkrake.repository;

import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.EventCollector;
import de.datenkraken.datenkrake.surveillance.actions.ArticleAction;

import java.lang.ref.WeakReference;
import java.util.List;

import kotlin.Triple;

import timber.log.Timber;

/**
 * A repository to retrieve and update {@link Article}s from the database and clear the cache.
 *
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Simon Schmalfuß - simon.schmalfuss@sstud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class ArticleRepository {

    private static ArticleRepository instance;
    private final AppDatabase database;

    /**
     * Constructor of the class.<br>
     * Sets database instance.
     *
     * @param database an instance of {@link AppDatabase}.
     */
    private ArticleRepository(final AppDatabase database) {
        Timber.tag("ArticleRepository");

        this.database = database;
    }

    /**
     * Returns the instance of this repository.<br>
     * This is implemented as a singleton.
     *
     * @param database an instance of {@link AppDatabase}.
     * @return the ArticleRepository instance.
     */
    public static synchronized ArticleRepository getInstance(final AppDatabase database) { //NOPMD
        if (instance == null) {
            instance = new ArticleRepository(database);
        }
        return instance;
    }

    /**
     * Returns the LiveData object that contains all cached {@link Article}s.
     *
     * @return LiveData containing all articles.
     */
    public LiveData<List<Article>> getArticles() {
        return database.daoArticle().getAllArticleAsync();
    }

    /**
     * Returns a {@link LiveData} object providing the requested article.
     *
     * @param articleId the id of the article to get.
     * @return the {@link LiveData} instance.
     */
    public LiveData<Article> getArticle(long articleId) {
        return database.daoArticle().getOneArticleByArticleUidAsync(articleId);
    }

    /**
     * Clears all cached {@link Article}s from the database.
     */
    public void clearAll() {
        AsyncTask.execute(() -> database.daoArticle().deleteAllArticleCachedSync());
    }

    /**
     * Updates article with ID and sets read field true.
     * Also raises event of article being opened.
     * @param articleID id of article that should be marked read.
     */
    public void setRead(long articleID) {
        // Raise opened event for article.
        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.ARTICLEIDACTION)
            .with(new Triple<>(ArticleAction.OPENED, articleID, new WeakReference<>(database))));

        AsyncTask.execute(() -> database.daoArticle().updateOneArticleReadSync(articleID, true));
    }

    /**
     * Updates the "read" state of the given {@link Article} via
     *  {@link de.datenkraken.datenkrake.db.DaoArticle}.<br>
     * Deletes the {@link Article}, if it's {@link de.datenkraken.datenkrake.model.Source}
     * isn't saved anymore.<br>
     * Works asynchronously.
     *
     * @param article {@link Article} to update or delete.
     * @param saved boolean indicating whether the {@link Article} is read or not.
     */
    public void setSavedAndTryToDelete(Article article, boolean saved) {
        // Raise saved event for article, only if article gets saved.
        if (saved) {
            EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.ARTICLEACTION)
                .with(new Triple<>(ArticleAction.SAVED, article.title, article.source.url.toString())));
        }

        AsyncTask.execute(() -> {
            if (!saved && article.source.deleted) {
                database.daoArticle().deleteOneArticleSync(article);
                SourceRepository.getInstance(database).deleteSourceIfEmpty(article.source);
            } else {
                database.daoArticle().updateOneArticleSavedSync(article.uid, saved);
            }
        });
    }
}
