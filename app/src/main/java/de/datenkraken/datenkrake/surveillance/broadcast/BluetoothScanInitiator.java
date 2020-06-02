package de.datenkraken.datenkrake.surveillance.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;

public class BluetoothScanInitiator extends Receiver {

    public BluetoothScanInitiator(ProcessedDataCollector collector) {
        super(collector);
    }

    @Override
    void receive(Context context, Intent intent, ProcessedDataCollector collector) {
        if (!intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            || intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1) != BluetoothAdapter.STATE_ON) {
            return;
        }
        BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);

        if (adapter == null) {
            return;
        }

        Receiver receiver = new BluetoothDeviceFound(collector);
        context.registerReceiver(receiver, receiver.getNonManifestIntentsFilter());
        BluetoothUtil.startDiscovery(adapter);
    }

    @Override
    public IntentFilter getNonManifestIntentsFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        return new IntentFilter();
    }
}
