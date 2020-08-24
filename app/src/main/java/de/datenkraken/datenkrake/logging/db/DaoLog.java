package de.datenkraken.datenkrake.logging.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class DaoLog {

    @Insert
    public abstract void insertOne(LogEntry entry);

    @Insert
    public abstract void insertAll(List<LogEntry> entries);

    @Query(value = "SELECT * FROM log_entries")
    public abstract List<LogEntry> getAll();

    @Query(value = "DELETE FROM log_entries")
    public abstract void delete();
}
