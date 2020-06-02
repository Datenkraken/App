package de.datenkraken.datenkrake.surveillance.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.datenkraken.datenkrake.SubmitBluetoothDeviceScanMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;
import timber.log.Timber;

public class BluetoothDeviceFound extends Receiver {

    public BluetoothDeviceFound() {
        Timber.tag("BluetoothFound");
    }

    public BluetoothDeviceFound(ProcessedDataCollector collector) {
        super(collector);
    }

    @Override
    void receive(Context context, Intent intent, ProcessedDataCollector collector) {

        if (intent.getAction() == null) {
            return;
        }

        if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            || (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                && intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_ON)
                    == BluetoothAdapter.STATE_OFF)) {
            BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);
            if (adapter != null) {
                adapter.disable();
            }
            context.unregisterReceiver(this);
            return;
        }

        if (!intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
            return;
        }
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device == null) {
            return;
        }

        ProcessedDataPacket packet = new ProcessedDataPacket(SubmitBluetoothDeviceScanMutation.OPERATION_ID);
        packet.putLong("timestamp", System.currentTimeMillis());
        packet.putString("name", device.getName());
        packet.putString("address", device.getAddress());
        packet.putBoolean("kown", device.getBondState() != BluetoothDevice.BOND_NONE);
        collector.addPacket(packet);
    }

    @Override
    public IntentFilter getNonManifestIntentsFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        return intentFilter;
    }
}
