package de.datenkraken.datenkrake.db;

import androidx.lifecycle.LiveData;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/**
 * Dao for {@link SourceToArticleRelation}. <br>
 * Will be decorated by Room to be able to query the database. <br>
 * SQL-Queries for {@link SourceToArticleRelation} are defined here.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Dao
interface DaoSourceToArticleRelation {

    // Get multiple SourceToArticleRelation

    /**
     * Returns all {@link SourceToArticleRelation} from the database. <br>
     * The source reference of all articles in {@link SourceToArticleRelation} is set to null. <br>
     * Works asynchronously.
     *
     * @return LiveData of the list of {@link SourceToArticleRelation}(s).
     */
    @Transaction
    @Query(value = "SELECT * FROM sources")
    LiveData<List<SourceToArticleRelation>> getAllSourceToArticleAsync();

    /**
     * Returns all {@link SourceToArticleRelation} from the database. <br>
     * The source reference of all articles in {@link SourceToArticleRelation} is set to null. <br>
     * Works synchronously.
     *
     * @return list of the {@link SourceToArticleRelation}(s).
     */
    @Transaction
    @Query(value = "SELECT * FROM sources")
    List<SourceToArticleRelation> getAllSourceToArticleSync();


    // Get single SourceToArticleRelation by the uid of a source

    /**
     * Returns all {@link SourceToArticleRelation} from the database with the given
     * {@link de.datenkraken.datenkrake.model.Source} id. <br>
     * The source reference of all {@link de.datenkraken.datenkrake.model.Article}s
     * in {@link SourceToArticleRelation} is set to null. <br>
     * Works asynchronously.
     *
     * @param uid {@link de.datenkraken.datenkrake.model.Source} uid of the searched source in the relation.
     * @return LiveData of a single {@link SourceToArticleRelation} containing the searched source.
     */
    @Transaction
    @Query(value = "SELECT * FROM sources WHERE uid = :uid")
    LiveData<SourceToArticleRelation> getOneSourceToArticleBySourceUidAsync(long uid);

    /**
     * Returns all {@link SourceToArticleRelation} from the database with the given
     * {@link de.datenkraken.datenkrake.model.Source} id. <br>
     * The source reference of all {@link de.datenkraken.datenkrake.model.Article}s
     * in {@link SourceToArticleRelation} is set to null. <br>
     * Works synchronously.
     *
     * @param uid {@link de.datenkraken.datenkrake.model.Source} uid of the searched source in the relation.
     * @return single {@link SourceToArticleRelation} containing the searched source.
     */
    @Transaction
    @Query(value = "SELECT * FROM sources WHERE uid = :uid")
    SourceToArticleRelation getOneSourceToArticleBySourceUidSync(long uid);
}
