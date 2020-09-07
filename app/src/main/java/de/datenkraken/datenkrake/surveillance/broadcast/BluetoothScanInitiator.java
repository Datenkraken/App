package de.datenkraken.datenkrake.surveillance.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.datenkraken.datenkrake.SubmitBluetoothDeviceScanMutation;
import de.datenkraken.datenkrake.logging.L;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;

import java.util.HashSet;
import java.util.Set;

public class BluetoothScanInitiator extends Receiver {

    public BluetoothScanInitiator(ProcessedDataCollector collector) {
        super(collector);
    }

    @Override
    void receive(Context context, Intent intent, ProcessedDataCollector collector) {

        if (intent.getAction() == null) {
            return;
        }

        if (!intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)
            || intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                != BluetoothAdapter.STATE_ON) {
            return;
        }
        BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);
        if (adapter == null || !adapter.isEnabled()) {
            return;
        }

        L.i("Bluetooth got enabled, starting scan");
        BluetoothUtil.scanLeDevices(new ScanCallback() {
            Set<String> scannedDevices = new HashSet<>();
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (callbackType == 0 || result == null) {
                    return;
                }

                BluetoothDevice device = result.getDevice();
                if (device != null && !scannedDevices.contains(result.getDevice().getName())) {
                    scannedDevices.add(device.getName() + device.getAddress());
                    ProcessedDataPacket packet =
                        new ProcessedDataPacket(SubmitBluetoothDeviceScanMutation.OPERATION_ID);
                    packet.putLong("timestamp", System.currentTimeMillis());
                    packet.putString("name", device.getName());
                    packet.putString("address", device.getAddress());
                    packet.putBoolean("known", device.getBondState() != BluetoothDevice.BOND_NONE);
                    collector.addPacket(packet);
                }
            }
        }, adapter::disable, 30000);
        context.unregisterReceiver(this);
    }

    @Override
    public IntentFilter getNonManifestIntentsFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }
}
