package de.datenkraken.datenkrake.db;

import androidx.lifecycle.LiveData;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import de.datenkraken.datenkrake.model.Source;

import java.util.List;


/**
 * Dao for {@link Source}. <br>
 * Will be decorated by Room to be able to query the database. <br>
 * SQL-Queries for {@link Source} are defined here.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Dao
public abstract class DaoSource {

    /**
     * Returns all {@link Source}s from the database which are not marked as deleted as LiveData. <br>
     * Works asynchronously.
     *
     * @return LiveData of list of {@link Source}s.
     */
    @Query(value = "SELECT * FROM sources WHERE deleted = 0")
    public abstract LiveData<List<Source>> getAllSourceNotMarkedAsync();

    /**
     * Returns all {@link Source}s from the database which are not marked as deleted. <br>
     * Works synchronously.
     *
     * @return list of {@link Source}s.
     */
    @Query(value = "SELECT * FROM sources WHERE deleted = 0")
    public abstract List<Source> getAllSourceNotMarkedSync();

    /**
     * Returns all {@link Source}s from the database. <br>
     * Works asynchronously.
     *
     * @return LiveData of list of {@link Source}s.
     */
    @Query(value = "SELECT * FROM sources")
    public abstract LiveData<List<Source>> getAllSourceAsync();

    /**
     * Returns all {@link Source}s from the database. <br>
     * Works synchronously.
     *
     * @return list of {@link Source}s.
     */
    @Query(value = "SELECT * FROM sources")
    public abstract List<Source> getAllSourceSync();

    /**
     * Returns a single {@link Source} from the database identified by its ID.
     * Returns null if no {@link Source} with the given ID exists. <br>
     * Works asynchronously.
     *
     * @param uid Uid of the {@link Source} to be returned.
     * @return LiveData of {@link Source} with the given uid.
     */
    @Query(value = "SELECT * FROM sources WHERE uid = :uid")
    public abstract LiveData<Source> getOneSourceByIdAsync(long uid);

    /**
     * Returns a single {@link Source} from the database identified by its ID.
     * Returns null if no {@link Source} with the given ID exists. <br>
     * Works synchronously.
     *
     * @param uid Uid of the {@link Source} to be searched.
     * @return {@link Source} with the given uid.
     */
    @Query(value = "SELECT * FROM sources WHERE uid = :uid")
    public abstract Source getOneSourceByIdSync(long uid);

    /**
     * Inserts a single {@link Source} into the database. <br>
     * Works synchronously.
     *
     * @param source {@link Source} to insert into the database.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertOneSourceSync(Source source);

    /**
     * Updates a single {@link Source}, identified by its uid. <br>
     * Works synchronously.
     *
     * @param source {@link Source} with new attributes and the same uid as the source to be replaced.
     */
    @Update
    public abstract void updateOneSourceSync(Source source);

    /**
     * Marks the {@link Source} as deleted in the database. <br>
     * Works synchronously.
     *
     * @param source {@link Source} to mark as deleted.
     */
    @Query(value = "UPDATE sources SET deleted = 1 WHERE :source = uid")
    public abstract void updateOneSourceMarkToDeleteSync(Source source);

    /**
     * Deletes one {@link Source} from the database. <br>
     * Works synchronously.
     *
     * @param uid {@link Source} to identify the source to be deleted.
     */
    @Query(value = "DELETE FROM sources WHERE uid = :uid")
    public abstract void deleteOneSourceBySourceUidSync(long uid);

}
