package de.datenkraken.datenkrake.surveillance;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import de.datenkraken.datenkrake.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import timber.log.Timber;


/**
 * Controls the lifetime of all components of the surveillance system.
 * Contains all Objects necessary to accept and process {@link DataCollectionEvent}s.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class EventManager implements LifecycleObserver {

    private static EventManager instance;

    private final ScheduledThreadPoolExecutor pool;
    private final ProcessedDataCollector processedDataCollector;
    final EventDistributor distributor;
    final EventCollector eventCollector;

    /**
     * Constructor, initializing all required objects, to accept and process {@link DataCollectionEvent}s.
     */
    private EventManager(WeakReference<Context> context) {
        Timber.tag("EventManager");

        int threadPoolSize = context.get().getResources().getInteger(R.integer.event_processor_pool);

        pool = new ScheduledThreadPoolExecutor(threadPoolSize);
        processedDataCollector = new ProcessedDataCollector(context);
        distributor = new EventDistributor(processedDataCollector);
        eventCollector = new EventCollector(pool, distributor, context);
    }

    /**
     * Returns the instance of {@link EventManager}. <br>
     * This is implemented as a singleton.
     *
     * @return singleton instance.
     */
    public static synchronized EventManager getInstance() { //NOPMD
        if (instance == null) {
            throw new ExceptionInInitializerError("Tried to access the EventManager instance without initializing it."
                + "did you forgot to call EventManager.setup()?");
        }
        return instance;
    }

    /**
     * Set ups the {@link EventManager}. Must be called before calling {@link #getInstance()}.
     *
     * @param context Context this EventManager should use.
     */
    public static void setup(WeakReference<Context> context) {
        instance = new EventManager(context);
    }

    /**
     * Gets called when the observer LifecycleOwner enters the ON_DESTROY state. <br>
     * Shutdowns the {@link #pool}, removes all {@link IEventProcessor}s and write all remaining
     * {@link ProcessedDataPacket}s in {@link ProcessedDataCollector} to the disk.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void shutdown() {
        pool.shutdownNow();
        distributor.eventProcessors.clear();
        processedDataCollector.flush();
    }

    /**
     * Gets called when the observer LifecycleOwner enters the ON_PAUSE state.
     * Writes all remaining {@link ProcessedDataPacket}s in {@link ProcessedDataCollector} to the disk.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause() {
        processedDataCollector.flush();
    }

    /**
     * Registers the given {@link IEventProcessor}.
     *
     * @param processor to register.
     */
    public void registerEventProcessor(IEventProcessor processor) {
        distributor.registerEventProcessor(processor);
    }
}
