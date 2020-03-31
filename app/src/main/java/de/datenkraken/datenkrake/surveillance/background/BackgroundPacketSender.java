package de.datenkraken.datenkrake.surveillance.background;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.datenkraken.datenkrake.authentication.AuthenticationManager;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.TaskDistributor;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.ProcessorProvider;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.NetworkUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * This Class provides the functionality for the background worker to send data to the graphql
 * backend. It runs only when wifi or ethernet connection is available.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class BackgroundPacketSender extends Worker {
    private final WeakReference<Context> context;
    private final Map<String, ISendProcessedData> processors;

    /**
     * Constructor, creating this class and initializing the {@link ISendProcessedData} provided by
     * {@link ProcessorProvider}. These contains the functionality to create the Apollo mutations.
     *
     * @param context Worker context, given by the {@link androidx.work.WorkManager}
     * @param workerParams params, given in the initializing
     */
    public BackgroundPacketSender(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Timber.tag("DataSender");
        this.context = new WeakReference<>(context);
        processors = new HashMap<>();
        ISendProcessedData[] eventProcessorArray = ProcessorProvider.getSendProcessor();
        TaskDistributor.setup(AuthenticationManager.create(context), context);
        for (ISendProcessedData processor : eventProcessorArray) {
            processors.put(processor.getTaskId(), processor);
        }
    }

    /**
     * Called by the {@link androidx.work.WorkManager}. Attempts to read all cached packets and
     * send them to the graphql endpoint if ethernet or wifi is available.
     *
     * @return Success or Failure
     */
    @NonNull
    @Override
    public Result doWork() {
        if (context.get() == null) {
            Timber.e("Context is null");
            return Result.failure();
        }

        if (!NetworkUtil.isWifiEnabled(context.get())) {
            Timber.e("wifi is %s", NetworkUtil.isWifiEnabled(context.get()));
            return Result.failure();
        }

        PacketLoader packetLoader;
        try {
            packetLoader = new PacketLoader(context.get());
        } catch (IOException e) {
            Timber.e(e, "Could not instantiate packet Loader");
            return Result.failure();
        }

        List<ProcessedDataPacket> data = packetLoader.loadNext();

        while (data != null) {
            sendPackets(data, packetLoader.currentFilePath());
            data = packetLoader.loadNext();
        }

        return Result.success();
    }

    /**
     * Creates an {@link ITask} for the given packet list and enqueues it.
     *
     * @param list list of {@link ProcessedDataPacket}
     */
    private void sendPackets(List<ProcessedDataPacket> list, String currentFilePath) {
        if (list.isEmpty()) {
            return;
        }

        ITask task = null;
        ISendProcessedData processor = processors.get(list.get(0).getTaskId());

        if (processor != null) {
            task = processor.getTask(list, new BackgroundSenderCallback(currentFilePath));
        }

        if (task != null) {
            task.request();
        }
    }

    /**
     * Simple {@link Callback} to delete the cache file which got already processed to a list of
     * {@link ProcessedDataPacket} and sent to the graphql backend.
     */
    private static class BackgroundSenderCallback implements Callback {

        String filePath;

        BackgroundSenderCallback(String path) {
            filePath = path;
        }

        @Override
        public void onFailure() {
            // We don't do anything if the mutation fails, it will be tried again later anyways.
        }

        @Override
        public void onSuccess() {
            if (filePath != null) {
                (new File(filePath)).delete();
            }
        }
    }

}
