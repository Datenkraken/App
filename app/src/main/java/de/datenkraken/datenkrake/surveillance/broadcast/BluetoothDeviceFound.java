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

    boolean disableBluetooth;

    public BluetoothDeviceFound(ProcessedDataCollector collector, boolean disableBluetooth) {
        super(collector);
        Timber.tag("BluetoothFound");
        this.disableBluetooth = disableBluetooth;
    }

    @Override
    void receive(Context context, Intent intent, ProcessedDataCollector collector) {

        if (intent.getAction() == null) {
            return;
        }

        if (scanFinished(intent, intent.getAction())) {
            BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);
            if (adapter != null && disableBluetooth) {
                adapter.disable();
            }
            context.unregisterReceiver(this);
            flush();
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

    private boolean scanFinished(Intent intent, String action) {
        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            return true;
        }
        int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);

        return (extra == BluetoothAdapter.STATE_OFF
                || extra == BluetoothAdapter.STATE_TURNING_OFF);
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
