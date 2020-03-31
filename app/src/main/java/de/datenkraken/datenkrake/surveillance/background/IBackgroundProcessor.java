package de.datenkraken.datenkrake.surveillance.background;

import android.content.Context;

import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;

/**
 * Processor executed in background by {@link BackgroundSupervisor}.
 */
public interface IBackgroundProcessor {

    /**
     * Gets called by {@link BackgroundSupervisor} giving it it's context to acquire data.
     *
     * @param context used to acquire data about the device
     * @param collector {@link ProcessedDataCollector} collector for the processed data.
     */
    void process(Context context, ProcessedDataCollector collector);
}
