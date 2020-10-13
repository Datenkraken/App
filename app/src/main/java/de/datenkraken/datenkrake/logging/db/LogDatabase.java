package de.datenkraken.datenkrake.logging.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


/**
 * A class extending RoomDatabase, responsible to contain classes/objects with Room annotations. <br>
 * Designed according to the Singleton pattern, because instantiating a RoomDatabase is performance
 * heavy. <br>
 * Used by Room to gather knowledge about Room entities, TypeConverters and Daos.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Database(entities = {LogEntry.class}, version = 1)
public abstract class LogDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "datenkrake_log";

    protected static LogDatabase instance;

    public abstract DaoLog daoLog();

    /**
     * Provides a singleton {@link LogDatabase} instance.
     * Will instantiate it if necessary, using Room.
     *
     * @param context Context used to get application context, required by Room.
     * @return AppDatabase instance.
     */
    public static synchronized LogDatabase getInstance(Context context) { //NOPMD
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    LogDatabase.class, DATABASE_NAME)
                    .build();
        }
        return instance;
    }
}
