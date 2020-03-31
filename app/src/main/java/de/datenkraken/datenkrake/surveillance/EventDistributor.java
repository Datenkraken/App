package de.datenkraken.datenkrake.surveillance;

import java.util.Collection;

/**
 * Distributes {@link DataCollectionEvent} coming from the
 * {@link java.util.concurrent.ThreadPoolExecutor} in {@link EventManager} to
 * {@link IEventProcessor}s which signalize that they process them via
 * {@link IEventProcessor#canProcess()} for them.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class EventDistributor {
    final EventProcessorMap eventProcessors;
    private final ProcessedDataCollector collector;

    /**
     * Creates this {@link EventDistributor}. Registers {@link IEventProcessor}s via
     * {@link #registerEventProcessors()}.
     *
     * @param collector {@link ProcessedDataCollector} used by the {@link IEventProcessor}s.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // registerEventProcessors is private so not overrideable
    EventDistributor(ProcessedDataCollector collector) {
        eventProcessors = new EventProcessorMap();
        this.collector = collector;
        registerEventProcessors();
    }

    /**
     * Distributes the given {@link DataCollectionEvent} to {@link IEventProcessor}s which
     * subscribed for its {@link DataCollectionEventType}.
     *
     * @param event {@link DataCollectionEvent} to distribute
     */
    void distributeEvent(DataCollectionEvent event) {
        Collection<IEventProcessor> processors  = eventProcessors.get(event.type);

        if (processors == null) {
            return;
        }
        for (IEventProcessor processor : processors) {
            processor.process(event, collector);
        }
    }

    /**
     * Registers {@link IEventProcessor}s provided from
     * {@link ProcessorProvider#getEventProcessors()}.
     */
    private void registerEventProcessors() {
        IEventProcessor[] processors = ProcessorProvider.getEventProcessors();
        for (IEventProcessor processor : processors) {
            registerEventProcessor(processor);
        }
    }

    /**
     * Registers the given {@link IEventProcessor}.
     *
     * @param processor to register.
     */
    public void registerEventProcessor(IEventProcessor processor) {
        eventProcessors.addProcessor(processor);
    }

}
