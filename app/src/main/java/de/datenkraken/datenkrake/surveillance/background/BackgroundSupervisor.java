package de.datenkraken.datenkrake.surveillance.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.logging.L;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessorProvider;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
        L.i("running at %s", new Date().toString());
        if (context.get() == null) {
            L.e("BackgroundSupervisor: could not run, context is null!");
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


        WorkManager workManager = WorkManager.getInstance(context.get());

        enqueueNextTask(workManager, context.get());
        try {
            Thread.sleep(keepAliveTime);
        } catch (InterruptedException e) {
            L.e(e, "Supervisor got interrupted in waiting for Threads");
        }
        dataCollector.flush();

        return Result.success();
    }

    private void enqueueNextTask(WorkManager workManager, Context context) {

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(BackgroundSupervisor.class)
            .setInitialDelay(1200000L - (System.currentTimeMillis() % 1200000L), TimeUnit.MILLISECONDS)
            .addTag(context.getResources().getString(R.string.background_service_supervisor))
            .build();

        L.i("Supervisor queried, set to %s",
            new Date(1200000L - (System.currentTimeMillis() % 1200000L) + System.currentTimeMillis()).toString());
        workManager.enqueue(request);
    }


}
