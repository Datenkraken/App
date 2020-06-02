package de.datenkraken.datenkrake.surveillance.processors.background;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import de.datenkraken.datenkrake.surveillance.broadcast.BluetoothDeviceFound;
import de.datenkraken.datenkrake.surveillance.broadcast.BluetoothScanInitiator;
import de.datenkraken.datenkrake.surveillance.broadcast.Receiver;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;
import timber.log.Timber;

public class BluetoothDeviceScanProcessor implements IBackgroundProcessor {

    public BluetoothDeviceScanProcessor() {
        Timber.tag("BLE SCAN");
    }

    @Override
    public void process(Context context, ProcessedDataCollector collector) {
        BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);

        if (adapter == null) {
            return;
        }

        if (!adapter.isEnabled()) {
            Receiver receiver = new BluetoothScanInitiator(collector);
            context.registerReceiver(receiver, receiver.getNonManifestIntentsFilter());
            adapter.enable();
            return; // We can't do anything anymore except waiting to the adapter to start
        }

        Receiver receiver = new BluetoothDeviceFound(collector);
        context.registerReceiver(receiver, receiver.getNonManifestIntentsFilter());
        BluetoothUtil.startDiscovery(adapter);
    }

    @Override
    public int keepAlive() {
        return 120000; //Keep the Background task 2 minutes alive to perform discovery
    }
}
