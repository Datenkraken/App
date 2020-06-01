package de.datenkraken.datenkrake.surveillance.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;

import java.lang.ref.WeakReference;


public abstract class Receiver extends BroadcastReceiver {

    abstract void receive(Context context, Intent intent, ProcessedDataCollector collector);

    public abstract IntentFilter getNonManifestIntentsFilter();

    @Override
    public void onReceive(Context context, Intent intent) {
        ProcessedDataCollector collector = new ProcessedDataCollector(new WeakReference<>(context));
        receive(context, intent, collector);
        collector.flush();
    }
}
