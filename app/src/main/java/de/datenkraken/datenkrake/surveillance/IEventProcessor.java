package de.datenkraken.datenkrake.surveillance;

/**
 * Interface providing necessary functionality for event processing.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public interface IEventProcessor {

    /**
     * Returns {@link DataCollectionEventType}s which this class can process.
     *
     * @return Array of{@link DataCollectionEventType}s this class can process
     */
    DataCollectionEventType[] canProcess();

    /**
     * Gets called by {@link EventDistributor} giving it a {@link DataCollectionEvent} to process.
     *
     * @param event {@link DataCollectionEvent} to process
     * @param collector {@link ProcessedDataCollector} collector for the processed event data.
     */
    void process(DataCollectionEvent event, ProcessedDataCollector collector);
}
