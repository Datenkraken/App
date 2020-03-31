package de.datenkraken.datenkrake.surveillance.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import de.datenkraken.datenkrake.BuildConfig;

/**
 * Utility class, providing functionality to access information about the network.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class NetworkUtil {

    /**
     * Private constructor so this class doesn't get instantiated and silence PMD.
     */
    private NetworkUtil() {

    }

    /**
     * Checks for different SDK versions if wifi or ethernet is available.
     * From: https://stackoverflow.com/questions/3841317/how-do-i-see-if-wi-fi-is-connected-on-android
     *
     * @param context context
     * @return true if wifi or ethernet is available, false otherwise.
     */
    public static boolean isWifiEnabled(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
            } else {
                NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || (BuildConfig.DEBUG
                            && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
                } else {
                    return false;
                }
            }
        }
        return false;
    }

}
