package de.datenkraken.datenkrake.surveillance.processors.background;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import de.datenkraken.datenkrake.WifiDataMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import de.datenkraken.datenkrake.surveillance.sender.WifiConnectionSender;
import de.datenkraken.datenkrake.surveillance.util.Callback;

import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Example Processor, collecting WIFI info.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class WifiConnectionProcessor implements IBackgroundProcessor {

    /**
     * Creates this class.
     */
    public WifiConnectionProcessor() {
        Timber.tag("WifiInfoProcessor");
    }

    @Override
    public void process(Context context, ProcessedDataCollector collector) {
        if (context == null) {
            return;
        }

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connManager == null) {
            return;
        }

        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo == null || !networkInfo.isConnected()) {
            return;
        }

        final WifiManager wifiManager =
            (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            return;
        }

        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        if (connectionInfo == null) {
            return;
        }

        collector.addPacket(createPacket(new Date().getTime(),
            connectionInfo.getSSID(),
            connectionInfo.getBSSID(),
            connectionInfo.getRssi()));
    }

    /**
     * Creates the {@link ProcessedDataPacket} used by
     * {@link WifiConnectionSender#getTask(List, Callback)}.
     * @param time long
     * @param ssid String
     * @param bssid String
     * @param rssi int
     * @return {@link ProcessedDataPacket}
     */
    private ProcessedDataPacket createPacket(Long time, String ssid, String bssid, int rssi) {
        ProcessedDataPacket packet = new ProcessedDataPacket(WifiDataMutation.OPERATION_ID);
        packet.putLong("time", time);
        packet.putString("SSID", ssid);
        packet.putString("BSSID", bssid);
        packet.putInteger("RSSI", rssi);
        return packet;
    }
}
