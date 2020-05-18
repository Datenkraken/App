package de.datenkraken.datenkrake.surveillance.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.datenkraken.datenkrake.UserActivityMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import timber.log.Timber;

public class UserActivityReceiver extends Receiver {
    @Override
    void receive(Context context, Intent intent, ProcessedDataCollector collector) {

        Timber.d("recieved!");
        if (intent == null || intent.getAction() == null) {
            return;
        }


        String action = intent.getAction();
        Timber.d("action: %s", action);
        if (!action.equals(Intent.ACTION_USER_PRESENT) && !action.equals(Intent.ACTION_SCREEN_OFF)) {
            return;
        }

        ProcessedDataPacket packet = new ProcessedDataPacket(UserActivityMutation.OPERATION_ID);
        packet.putLong("datetime", System.currentTimeMillis());
        packet.putBoolean("activity", action.equals(Intent.ACTION_USER_PRESENT));
        Timber.d("got right action");
        collector.addPacket(packet);
    }

    @Override
    public IntentFilter getNonManifestIntentsFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        return filter;
    }
}
