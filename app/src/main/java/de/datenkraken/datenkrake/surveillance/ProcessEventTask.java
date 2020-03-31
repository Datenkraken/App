package de.datenkraken.datenkrake.surveillance;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Runnable task, which gets queued in {@link ScheduledThreadPoolExecutor} in
 * {@link EventManager}. <br>
 * Contains the {@link DataCollectionEvent} and {@link WeakReference} to {@link EventDistributor}
 * to process and distribute the {@link #event}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ProcessEventTask implements Runnable {

    private final WeakReference<EventDistributor> distributor;
    private final DataCollectionEvent event;

    /**
     * Creates this class.
     *
     * @param distributor WeakReference to {@link EventDistributor}
     * @param event {@link DataCollectionEvent}
     */
    public ProcessEventTask(@NonNull WeakReference<EventDistributor> distributor,
                            @NonNull DataCollectionEvent event) {
        this.distributor = distributor;
        this.event = event;
    }

    /**
     * Gets executed by a thread in {@link ScheduledThreadPoolExecutor} and distributes the event
     * via {@link EventDistributor}.
     */
    @Override
    public void run() {
        if (distributor.get() == null) {
            return;
        }

        distributor.get().distributeEvent(event);
    }
}
