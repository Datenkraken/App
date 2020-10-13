package de.datenkraken.datenkrake.surveillance.processors.event;

import de.datenkraken.datenkrake.ApplicatonActionMutation;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.IEventProcessor;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.graphqladapter.ApplicationAction;

/**
 * Collects and process the events to general application navigation and behavior.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ApplicationActionProcessor implements IEventProcessor {

    @Override
    public DataCollectionEventType[] canProcess() {
        return new DataCollectionEventType[] {DataCollectionEventType.APPLICATIONACTION};
    }

    @Override
    public void process(DataCollectionEvent event, ProcessedDataCollector collector) {
        if (event.content.get() == null) {
            return;
        }

        ApplicationAction action = (ApplicationAction) event.content.get();
        ProcessedDataPacket packet = new ProcessedDataPacket(ApplicatonActionMutation.OPERATION_ID);
        packet.putObject("action", action);
        packet.putLong("timestamp", event.timestamp.getTime());
        collector.addPacket(packet);
    }
}
