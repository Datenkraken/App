package de.datenkraken.datenkrake.logging.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "log_entries")
public class LogEntry {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public long id = 0;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "message")
    public String message;

    public LogEntry(String type, String message) {
        this.type = type;
        this.message = message;
    }
}
