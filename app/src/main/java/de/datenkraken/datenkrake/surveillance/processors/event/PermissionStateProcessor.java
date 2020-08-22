package de.datenkraken.datenkrake.surveillance.processors.event;

import android.util.Pair;

import de.datenkraken.datenkrake.PermissionStateMutation;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.IEventProcessor;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.graphqladapter.Permission;

/**
 * Collects and process the events to general application navigation and behavior.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class PermissionStateProcessor implements IEventProcessor {

    @Override
    public DataCollectionEventType[] canProcess() {
        return new DataCollectionEventType[] {DataCollectionEventType.PERMISSIONSTATE};
    }

    @Override
    public void process(DataCollectionEvent event, ProcessedDataCollector collector) {
        if (event.content.get() == null) {
            return;
        }

        Pair<Permission, Boolean> permissionState = (Pair<Permission, Boolean>) event.content.get();
        ProcessedDataPacket packet = new ProcessedDataPacket(PermissionStateMutation.OPERATION_ID);
        packet.putLong("timestamp", event.timestamp.getTime());
        packet.putObject("permission", permissionState.first);
        packet.putBoolean("state", permissionState.second);
        collector.addPacket(packet);
    }
}
