package de.datenkraken.datenkrake.surveillance.processors.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.SubmitOSInformationMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;
import timber.log.Timber;

public class OSInformationProcessor implements IBackgroundProcessor {

    public OSInformationProcessor() {
        Timber.tag("OsInformationProcessor");
    }

    @Override
    public void process(Context context, ProcessedDataCollector collector) {
        if (context == null) {
            return;
        }

        String fingerprint = Build.FINGERPRINT;
        SharedPreferences pref =
            context.getSharedPreferences(context.getResources()
                .getString(R.string.surv_shared_preference_name), Context.MODE_PRIVATE);
        String savedFingerprint =
            pref.getString(context.getResources().getString(R.string.surv_shared_preference_os_fingerprint), "");

        if (savedFingerprint.equals(fingerprint)) {
            return;
        }

        ProcessedDataPacket packet = new ProcessedDataPacket(SubmitOSInformationMutation.OPERATION_ID);
        packet.putLong("timestamp", System.currentTimeMillis());
        packet.putInteger("sdk", Build.VERSION.SDK_INT);
        packet.putString("device", Build.DEVICE);
        packet.putString("model", Build.MODEL);
        packet.putString("vendor", Build.MANUFACTURER);
        packet.putString("serial", Settings.Secure.ANDROID_ID);

        collector.addPacket(packet);
        Timber.d("OS Information collected!");

        pref.edit().putString(
            context.getResources().getString(R.string.surv_shared_preference_os_fingerprint),
            fingerprint).apply();
    }
}
