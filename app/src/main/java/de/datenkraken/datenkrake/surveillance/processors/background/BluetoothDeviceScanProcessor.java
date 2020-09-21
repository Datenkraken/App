package de.datenkraken.datenkrake.surveillance.processors.background;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;

import de.datenkraken.datenkrake.SubmitBluetoothDeviceScanMutation;
import de.datenkraken.datenkrake.logging.L;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import de.datenkraken.datenkrake.surveillance.broadcast.BluetoothScanInitiator;
import de.datenkraken.datenkrake.surveillance.broadcast.Receiver;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;

import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

public class BluetoothDeviceScanProcessor implements IBackgroundProcessor {

    public BluetoothDeviceScanProcessor() {
        Timber.tag("Bluetooth Scan");
    }

    @Override
    public void process(Context context, ProcessedDataCollector collector) {
        BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);

        if (adapter == null
            || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return;
        }

        if (!adapter.isEnabled()) {
            Receiver receiver = new BluetoothScanInitiator(collector);
            context.registerReceiver(receiver, receiver.getNonManifestIntentsFilter());
            adapter.enable();
            return; // We can't do anything anymore except waiting to the adapter to start
        }

        L.i("Bluetooth was enabled, starting scan");
        BluetoothUtil.scanLeDevices(new ScanCallback() {
            Set<String> scannedDevices = new HashSet<>();
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (callbackType == 0 || result == null) {
                    return;
                }

                BluetoothDevice device = result.getDevice();
                if (device != null && !scannedDevices.contains(result.getDevice().getName())) {
                    scannedDevices.add(result.getDevice().getName());
                    ProcessedDataPacket packet =
                        new ProcessedDataPacket(SubmitBluetoothDeviceScanMutation.OPERATION_ID);
                    packet.putLong("timestamp", System.currentTimeMillis());
                    packet.putString("name", device.getName());
                    packet.putString("address", device.getAddress());
                    packet.putBoolean("known", device.getBondState() != BluetoothDevice.BOND_NONE);
                    collector.addPacket(packet);
                }
            }
        }, null, 30000, context);
    }

    @Override
    public int keepAlive() {
        return 50000; //Keep the Background task 50 seconds alive to perform discovery
    }
}
