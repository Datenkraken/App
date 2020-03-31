package de.datenkraken.datenkrake.db;

import android.net.Uri;

import androidx.room.TypeConverter;

import de.datenkraken.datenkrake.model.Source;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;

import timber.log.Timber;


/**
 * Class containing type converter for Room.
 * Referenced by {@link AppDatabase}. <br>
 * Room uses these converters if its unable to store an attribute of an entity
 * to convert it to a storable format.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
final class DatabaseConverters {

    /**
     * Constructor for this class, initializing it.
     */
    private DatabaseConverters() {
    }

    /**
     * Type converter for Room. <br>
     * Converts {@link Date} to {@link Long} representing the number of milliseconds
     * since January 1, 1970, 00:00:00 GM. <br>
     * Returns null if the {@link Date} is null.
     *
     * @param date {@link Date} to convert to {@link Long}.
     * @return {@link Long} with the number of milliseconds or null.
     */
    @TypeConverter
    public static Long dateToLong(Date date) {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }

    /**
     * Type converter for Room. <br>
     * Converts a {@link Long} representing the number of milliseconds
     * since January 1, 1970, 00:00:00 GM to java.util.Date. <br>
     * Returns null if the {@link Long} is null.
     *
     * @param l {@link Long} with the number of milliseconds to convert to {@link Date}.
     * @return {@link Date} or null.
     */
    @TypeConverter
    public static Date longToDate(Long l) {
        if (l == null) {
            return null;
        }
        return new Date(l);
    }

    /**
     * Type converter for Room. <br>
     * Converts {@link Uri} to {@link String}. <br>
     * Returns null if the {@link Uri} is null.
     *
     * @param uri {@link Uri} to convert to {@link String}.
     * @return {@link String} or null.
     */
    @TypeConverter
    public static String uriToString(Uri uri) {
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }

    /**
     * Type converter for Room. <br>
     * Converts {@link String} to {@link Uri}. <br>
     * Returns null if the {@link String} is null.
     *
     * @param s {@link String} to convert to {@link Uri}.
     * @return {@link Uri} or null.
     */
    @TypeConverter
    public static Uri stringToUri(String s) {
        if (s == null) {
            return null;
        }
        return Uri.parse(s);
    }

    /**
     * Type converter for Room. <br>
     * Converts {@link URL} to {@link String}. <br>
     * Returns null if the {@link URL} is null.
     *
     * @param url {@link URL} to convert to {@link String}.
     * @return {@link String} or null.
     */
    @TypeConverter
    public static String urlToString(URL url) {
        if (url == null) {
            return null;
        }
        return url.toString();
    }

    /**
     * Type converter for Room. <br>
     * Converts {@link String} to {@link URL}. <br>
     * Returns null if it is unable to convert the passed {@link String}, and
     * throws a {@link MalformedURLException}, which is displayed using timber.
     *
     * @param string {@link String} to convert to {@link URL}.
     * @return {@link URL} or null.
     */
    @TypeConverter
    public static URL stringToURL(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            Timber.e(e, "Malformed url read from Database");
            return null;
        }
    }

    /**
     * Type converter for Room. <br>
     * Converts {@link Source} to long by returning the {@link Source} uid.
     *
     * @param source {@link Source} to convert to {@link Long}.
     * @return long from the {@link Source} uid.
     */
    @TypeConverter
    public static long sourceToLong(Source source) {
        return source.uid;
    }

    /**
     * Type converter for Room. <br>
     * This method is required by Room to function properly. <br>
     * Since it's is impossible to generate a proper {@link Source} object without accessing
     * the {@link AppDatabase}, this method will only return null.
     *
     * @param l long, source id to convert.
     * @return null.
     */
    @TypeConverter
    public static Source longToSource(long l) {
        return null;
    }
}
