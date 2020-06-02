package de.datenkraken.datenkrake.surveillance.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessorProvider;

import java.lang.ref.WeakReference;
import java.util.Date;

import timber.log.Timber;

/**
 * This Class provides the functionality for the background worker to acquire data about this device.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class BackgroundSupervisor extends Worker {

    private final WeakReference<Context> context;

    // PMD thinks, that we only use this variable on one method, which is not true
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    private final IBackgroundProcessor[] processors;

    private final ProcessedDataCollector dataCollector;

    /**
     * Constructor, creating this class and initializing the {@link IBackgroundProcessor} provided by
     * {@link ProcessorProvider#getBackgroundProcessors()}.
     *
     * @param context Worker context, given by the {@link androidx.work.WorkManager}
     * @param workerParams params, given in the initializing
     */
    public BackgroundSupervisor(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Timber.tag("BackgroundSupervisor");
        this.context = new WeakReference<>(context);
        processors = ProcessorProvider.getBackgroundProcessors();
        dataCollector = new ProcessedDataCollector(new WeakReference<>(context));
    }

    /**
     * Called by the {@link androidx.work.WorkManager}. Lets all {@link IBackgroundProcessor}s in
     * {@link #processors} run to acquire information about the device.
     *
     * @return Success or Failure
     */
    @NonNull
    @Override
    public Result doWork() {
        Timber.i("running at %s", new Date().toString());
        if (context.get() == null) {
            return Result.failure();
        }
        int keepAliveTime = 0;
        for (IBackgroundProcessor processor : processors) {
            processor.process(context.get(), dataCollector);
            if (processor.keepAlive() > keepAliveTime) {
                keepAliveTime = processor.keepAlive();
            }
        }
        dataCollector.flush();

        try {
            Thread.sleep(keepAliveTime);
        } catch (InterruptedException e) {
            Timber.e(e);
        }
        dataCollector.flush();
        return Result.success();
    }


}
