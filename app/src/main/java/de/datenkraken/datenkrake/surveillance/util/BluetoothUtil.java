package de.datenkraken.datenkrake.surveillance.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.provider.Settings;

import java.nio.charset.StandardCharsets;

public final class BluetoothUtil {
    public static AdvertiseSettings getBluetoothAdvertSettings() {
        return new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setTimeout(10000) // 10 seconds
            .setConnectable(false)
            .build();
    }

    public static AdvertiseData getAdvertisePayload(Context context) {
        return new AdvertiseData.Builder()
            .addServiceData(ParcelUuid.fromString("0bc9aefd-cf5f-4515-bebb-190f8eb4a402"),
                Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                                            Settings.Secure.ANDROID_ID).getBytes(StandardCharsets.UTF_8))
            .build();
    }

    public static ScanSettings getScanSettings() {
        return new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .build();
    }

    public static void startDiscovery(BluetoothAdapter adapter) {

        if (adapter.isDiscovering()) {
            adapter.cancelDiscovery();
        }

        adapter.startDiscovery();
    }

    public static BluetoothAdapter getAdapter(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return null;
            }

            adapter = bluetoothManager.getAdapter();
        }
        return adapter;
    }

}
