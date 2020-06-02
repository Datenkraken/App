package de.datenkraken.datenkrake.surveillance.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public final class BluetoothUtil {

    /**
     * Private constructor so this class doesn't get instantiated and silence PMD.
     */
    private BluetoothUtil() {

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
