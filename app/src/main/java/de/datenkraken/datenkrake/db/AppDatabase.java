package de.datenkraken.datenkrake.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

import timber.log.Timber;


/**
 * A class extending RoomDatabase, responsible to contain classes/objects with Room annotations. <br>
 * Designed according to the Singleton pattern, because instantiating a RoomDatabase is performance
 * heavy. <br>
 * Used by Room to gather knowledge about Room entities, TypeConverters and Daos.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Database(entities = {Article.class, Source.class}, version = 1)
@TypeConverters({DatabaseConverters.class})
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "datenkraken";

    protected static AppDatabase instance;

    public abstract DaoArticle daoArticle();

    public abstract DaoSource daoSource();

    /**
     * Constructor to instantiate the RoomDatabase and DaoWrapper.
     */
    protected AppDatabase() {
        Timber.tag("AppDatabase");
    }

    /**
     * Provides a singleton {@link AppDatabase} instance.
     * Will instantiate it if necessary, using Room.
     *
     * @param context Context used to get application context, required by Room.
     * @return AppDatabase instance.
     */
    public static synchronized AppDatabase getInstance(Context context) { //NOPMD
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME)
                    .build();
        }
        return instance;
    }
}
