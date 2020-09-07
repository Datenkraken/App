package de.datenkraken.datenkrake.surveillance.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import de.datenkraken.datenkrake.logging.L;

import java.util.Date;

public final class BluetoothUtil {

    /**
     * Private constructor so this class doesn't get instantiated and silence PMD.
     */
    private BluetoothUtil() {

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

    public static void scanLeDevices(ScanCallback callback, Runnable stoppedCallback, long scanPeriod)  {
        BluetoothLeScanner bluetoothLeScanner =
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            L.i("stopping bluetooth scan at %s", new Date());
            bluetoothLeScanner.stopScan(callback);
            if (stoppedCallback != null) {
                stoppedCallback.run();
            }
        }, scanPeriod);
        L.i("starting bluetooth scan at %s", new Date());
        bluetoothLeScanner.startScan(callback);
    }

}
