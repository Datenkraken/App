package de.datenkraken.datenkrake.surveillance.processors.event;

import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.IEventProcessor;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;

import timber.log.Timber;

/**
 * Example Processor, logging the {@link DataCollectionEvent#content}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class LogProcessor implements IEventProcessor {

    /**
     * Creates this class.
     */
    public LogProcessor() {
        Timber.tag("LogProcessor");
    }

    /**
     * Returns the example {@link DataCollectionEventType} this processor can process.
     *
     * @return {@link DataCollectionEventType#DEFAULT} as an example type.
     */
    @Override
    public DataCollectionEventType[] canProcess() {
        return new DataCollectionEventType[]{DataCollectionEventType.DEFAULT};
    }

    /**
     * Processes the {@link DataCollectionEvent} by printing it's content via Timber to the console.
     *
     * @param event {@link DataCollectionEvent} to process
     */
    @Override
    public void process(DataCollectionEvent event, ProcessedDataCollector collector) {
        Timber.d("Logging: %s", event.content.get());
    }
}
