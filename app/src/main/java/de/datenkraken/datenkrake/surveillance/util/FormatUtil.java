package de.datenkraken.datenkrake.surveillance.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class, providing functionality to format types to valid types for apollo mutations.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class FormatUtil {

    private FormatUtil() {

    }

    /**
     * Format date to a valid apollo mutation type.
     *
     * @param date to format.
     * @return formatted date.
     */
    public static String formatDate(Date date) {
        @SuppressLint("SimpleDateFormat")
        @SuppressWarnings("PMD.SimpleDateFormatNeedsLocale")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.format(date);
    }
}
