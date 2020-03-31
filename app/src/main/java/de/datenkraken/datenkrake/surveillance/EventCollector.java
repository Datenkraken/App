package de.datenkraken.datenkrake.surveillance;

import android.content.Context;

import androidx.lifecycle.LifecycleObserver;
import de.datenkraken.datenkrake.R;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Collects raised events. Implements Limits to keep the queue size in check. <br>
 * Creates {@link ProcessEventTask}s for every {@link DataCollectionEvent} raised.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class EventCollector implements LifecycleObserver {

    private final int EVENT_QUEUE_SOFT_CAP;
    private final int EVENT_QUEUE_HARD_CAP;

    private final ScheduledThreadPoolExecutor pool;
    private final EventDistributor distributor;

    /**
     * Creates this collector.
     *
     * @param pool Pool which can hold the {@link ProcessEventTask}.
     * @param distributor {@link EventDistributor} which will distribute the {@link DataCollectionEvent}s
     to the {@link IEventProcessor}s.
     */
    EventCollector(ScheduledThreadPoolExecutor pool, EventDistributor distributor,
                   WeakReference<Context> context) {
        this.pool = pool;
        this.distributor = distributor;
        EVENT_QUEUE_SOFT_CAP = context.get().getResources().getInteger(R.integer.event_queue_hard_cap);
        EVENT_QUEUE_HARD_CAP = context.get().getResources().getInteger(R.integer.event_queue_soft_cap);
    }

    /**
     * Creates {@link ProcessEventTask} to process the given {@link DataCollectionEvent},
     * instantiate a {@link ProcessEventTask} and adds it to the queue of {@link #pool}.
     * If the queue size is greater than {@link #EVENT_QUEUE_SOFT_CAP} or
     * {@link #EVENT_QUEUE_HARD_CAP} constraints for the event priority apply.
     *
     * @param event {@link DataCollectionEvent} to process
     * @return true if added to queue, false otherwise.
     */
    private synchronized boolean addEvent(DataCollectionEvent event) { //NOPMD
        if (!canAcceptEvent(pool.getQueue().size(), event.priority)) {
            return false;
        }

        int delay = Math.max(event.delay, 0);

        ProcessEventTask processEventTask = new ProcessEventTask(new WeakReference<>(distributor), event);

        if (event.period <= 0) {
            pool.schedule(processEventTask, delay, TimeUnit.MILLISECONDS);
        } else {
            pool.scheduleWithFixedDelay(processEventTask, delay, event.period, TimeUnit.MILLISECONDS);
        }

        return true;
    }

    /**
     * Checks if the given priority allows to add an {@link DataCollectionEvent} to {@link #pool}.
     *
     * @param querySize Size of the current query
     * @param priority {@link DataCollectionEvent.Priority} priority of the {@link DataCollectionEvent}
     * @return true if it can be added, false otherwise
     */
    private boolean canAcceptEvent(int querySize, DataCollectionEvent.Priority priority) {
        return !((querySize > EVENT_QUEUE_SOFT_CAP
            && priority.getPriority() < DataCollectionEvent.Priority.PRIORITY_HIGH.getPriority())
            || querySize > EVENT_QUEUE_HARD_CAP
            || pool.isShutdown());
    }

    /**
     * Static function to raise an {@link DataCollectionEvent}.
     *
     * @param event raised {@link DataCollectionEvent}
     * @return true if it was added, false otherwise
     */
    public static boolean raiseEvent(DataCollectionEvent event) {
        return EventManager.getInstance().eventCollector.addEvent(event);
    }
}
