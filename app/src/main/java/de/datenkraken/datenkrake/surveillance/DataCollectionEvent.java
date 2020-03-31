package de.datenkraken.datenkrake.surveillance;

import java.lang.ref.WeakReference;
import java.util.Date;

/**
 * Generic Event, which holds all necessary information to process it. Can be created by all
 * classes in this application. Gets processed by {@link IEventProcessor}s.
 *
 * @param <T> Type of data this event should hold.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class DataCollectionEvent<T> {

    /**
     * Enum for the Priority of this Event. Events with a priority of {@link #PRIORITY_MEDIUM} or
     * lower can be ignored if {@link EventCollector} reaches its soft cap.
     */
    enum Priority {
        PRIORITY_LOW(0),
        PRIORITY_MEDIUM(1),
        PRIORITY_HIGH(2);

        private int priority;

        Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    public DataCollectionEventType type;
    Priority priority = Priority.PRIORITY_LOW;
    int delay = 0;
    int period = 0;

    public Date timestamp;
    public WeakReference<T> content;

    /**
     * Creates this Event with the given {@link DataCollectionEventType}.
     *
     * @param t {@link DataCollectionEventType}
     */
    public DataCollectionEvent(DataCollectionEventType t) {
        timestamp = new Date();
        type = t;
    }

    /**
     * Sets the content of this event.
     *
     * @param content {@link #content} to set.
     * @return {@link DataCollectionEvent}
     */
    public DataCollectionEvent with(T content) {
        this.content = new WeakReference<>(content);
        return this;

    }

    /**
     * Sets the priority of this event.
     *
     * @param priority {@link #priority} to set
     * @return {@link DataCollectionEvent}
     */
    public DataCollectionEvent setPriority(Priority priority) {
        this.priority = priority;
        return this;
    }

}
