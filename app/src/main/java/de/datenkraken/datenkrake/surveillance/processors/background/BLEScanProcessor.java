package de.datenkraken.datenkrake.surveillance.processors.background;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.ParcelUuid;
import android.provider.Settings;

import java.util.Arrays;
import java.util.List;

import de.datenkraken.datenkrake.authentication.AuthenticationManager;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import de.datenkraken.datenkrake.surveillance.broadcast.BluetoothDeviceFound;
import de.datenkraken.datenkrake.surveillance.broadcast.BluetoothScanInitiator;
import de.datenkraken.datenkrake.surveillance.broadcast.Receiver;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;
import timber.log.Timber;

public class BLEScanProcessor implements IBackgroundProcessor {

    public BLEScanProcessor() {
        Timber.tag("BLE SCAN");
    }

    @Override
    public void process(Context context, ProcessedDataCollector collector) {
        Timber.d("ID is: %s", Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
            Settings.Secure.ANDROID_ID));
        Timber.d("Id Token is: %s", new String(BluetoothUtil.getAdvertisePayload(context).getServiceData().get(ParcelUuid.fromString("0bc9aefd-cf5f-4515-bebb-190f8eb4a402"))));

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
