package de.datenkraken.datenkrake.surveillance;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * Class, extending {@link java.util.HashMap} to provide functionality for supporting multiple
 * {@link IEventProcessor} for one {@link DataCollectionEventType}. Used by {@link EventDistributor}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class EventProcessorMap extends ConcurrentHashMap<DataCollectionEventType, Collection<IEventProcessor>> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor of this class, setting up Timber.
     */
    EventProcessorMap() {
        Timber.tag("EventProcessorMap");
    }

    /**
     * Adds an {@link IEventProcessor} to the collection identified by one {@link DataCollectionEventType}
     * in this Map. Creates the Collection if necessary and adds the {@link IEventProcessor} to it.
     * If the {@link IEventProcessor} exists already in the collection, it does nothing.
     *
     * @param type {@link DataCollectionEventType} identifies the collection
     * @param processor {@link IEventProcessor} to add
     */
    private void addToMap(DataCollectionEventType type, IEventProcessor processor) {
        Collection<IEventProcessor> set;

        if (!this.containsKey(type)) {
            set = new HashSet<>();
            this.put(type, set);

        } else {
            set = this.get(type);

            if (set == null) {
                set = new HashSet<>();
                this.put(type, set);
            }
        }

        if (set.contains(processor)) {
            Timber.e("EventProcessor %s is already registered for event %s",
                processor, type);
            return;
        }

        set.add(processor);
    }

    /**
     * Adds an {@link IEventProcessor} to this Map.
     * Does nothing if the {@link IEventProcessor} is already added.
     *
     * @param processor {@link IEventProcessor} to add
     */
    void addProcessor(@NonNull IEventProcessor processor) {

        DataCollectionEventType[] types = processor.canProcess();

        if (types == null) {
            Timber.e("EventProcessor %s doesn't listen to any events", processor);
            return;
        }

        for (DataCollectionEventType type : types) {
            addToMap(type, processor);
        }
    }
}
