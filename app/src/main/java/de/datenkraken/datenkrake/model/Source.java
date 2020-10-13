package de.datenkraken.datenkrake.model;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import de.datenkraken.datenkrake.db.DaoSource;

import java.net.URL;
import java.util.Date;
import java.util.Objects;

/**
 * Container Class for an standardized Source. <br>
 * Required and used by Room, creating the table in the database. <br>
 * Used by {@link DaoSource} to save and load these from and to the database.
 * Provides the functionalities to create a hash for the source, get the icon of the source and
 * compare it to a given object.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Entity(tableName = "sources")
public class Source {

    @Ignore
    private static final String ICON_PATH = "favicon.ico";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public long uid = -1;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "url")
    public URL url;

    @ColumnInfo(name = "updated")
    public Date updated;

    @ColumnInfo(name = "deleted")
    public boolean deleted = false;


    /**
     * Creates a hash out of the name, url and updated status of the source.
     *
     * @return hash from the name, url and updated status of the source.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, url, updated);
    }

    /**
     * Checks, if the current source is equal to a given object. <br>
     * Checks for the equality of the uids, names, urls and the updated value
     * Returns true, if all of them are equal, else returns false. <br>
     * Also returns false, when o is null or not of the same type as this class.
     *
     * @param o object to check equality for.
     * @return boolean, that is true, if he objects and the sources uid, name, url and updated
     status are equal, else false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Source source = (Source) o;
        return Objects.equals(uid, source.uid)
            && Objects.equals(name, source.name)
            && Objects.equals(url, source.url)
            && Objects.equals(updated, source.updated);
    }

    /**
     * Returns an Uri, linking to the Icon of this {@link Source}. <br>
     * Generates the Uri out of the host part of the URL and it's favicon. <br>
     * Returns null if the url is null.
     *
     * @return Uri, linking to the Icon if this {@link Source}.
     */
    public Uri getIcon() {
        if (url == null) {
            return null;
        }

        return (new Uri.Builder())
                .scheme(url.getProtocol())
                .authority(url.getAuthority())
                .appendPath(ICON_PATH)
                .build();
    }
}
