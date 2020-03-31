package de.datenkraken.datenkrake.surveillance.processors.event;

import android.util.Pair;

import de.datenkraken.datenkrake.SourceActionMutation;
import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.IEventProcessor;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.actions.SourceAction;

import java.lang.ref.WeakReference;

import kotlin.Triple;

/**
 * Collects and process the events to general source interaction.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class SourceActionProcessor implements IEventProcessor {
    @Override
    public DataCollectionEventType[] canProcess() {
        return new DataCollectionEventType[]{
            DataCollectionEventType.SOURCEACTION,
            DataCollectionEventType.SOURCEIDACTION
        };
    }

    @Override
    public void process(DataCollectionEvent event, ProcessedDataCollector collector) {
        String url;
        SourceAction action;

        if (event.content.get() == null) {
            return;
        }

        if (event.type == DataCollectionEventType.SOURCEIDACTION) {
            Triple triple = (Triple) event.content.get();
            action = (SourceAction) triple.getFirst();
            AppDatabase db = (AppDatabase) ((WeakReference) triple.getThird()).get();

            if (db == null) {
                return;
            }

            url = db.daoSource().getOneSourceByIdSync((Long) triple.getSecond()).url.toString();
        } else {
            Pair pair = (Pair) event.content.get();
            action = (SourceAction) pair.first;
            url = (String) pair.second;
        }

        ProcessedDataPacket packet = new ProcessedDataPacket(SourceActionMutation.OPERATION_ID);
        packet.putLong("timestamp", event.timestamp.getTime());
        packet.putObject("action", action);
        packet.putString("url", url);
        collector.addPacket(packet);
    }
}
