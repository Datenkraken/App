package de.datenkraken.datenkrake.db;

import androidx.lifecycle.LiveData;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import de.datenkraken.datenkrake.model.Article;

/**
 * Dao for {@link ArticleToSourceRelation}. <br>
 * Will be decorated by Room to be able to query the database. <br>
 * SQL-Queries for {@link ArticleToSourceRelation} are defined here.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Dao
interface DaoArticleToSourceRelation {

    // Get single ArticleToSourceRelation by the uid of an Article

    /**
     * Returns the {@link ArticleToSourceRelation} for the given article id. <br>
     * Works asynchronously.
     *
     * @param uid {@link Article} uid for the searched article of the relation.
     * @return LiveData of {@link ArticleToSourceRelation} for the {@link Article} with the given uid.
     */
    @Transaction
    @Query(value = "SELECT * FROM articles WHERE :uid = articles.uid")
    LiveData<ArticleToSourceRelation> getOneArticleToSourceByArticleUidAsync(long uid);

    /**
     * Returns the {@link ArticleToSourceRelation} for the given article id. <br>
     * Works synchronously.
     *
     * @param uid {@link Article} uid for the searched article of the relation.
     * @return the {@link ArticleToSourceRelation} for the {@link Article} with the given uid.
     */
    @Transaction
    @Query(value = "SELECT * FROM articles WHERE :uid = articles.uid")
    ArticleToSourceRelation getOneArticleToSourceByArticleUidSync(long uid);
}
