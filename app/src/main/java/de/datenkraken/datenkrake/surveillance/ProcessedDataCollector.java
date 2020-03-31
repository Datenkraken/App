package de.datenkraken.datenkrake.surveillance;

import android.content.Context;

import de.datenkraken.datenkrake.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Collects {@link ProcessedDataPacket}s from different {@link IEventProcessor}s and saves them to
 * the disk.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ProcessedDataCollector {

    private final long PACKET_LIMIT;
    private final List<ProcessedDataPacket> packetList = new ArrayList<>();
    private final PacketSaver packetSaver;

    /**
     * Constructor of this class, initializing constants.
     *
     * @param context required to load constants from xml resources.
     */
    public ProcessedDataCollector(WeakReference<Context> context) {
        Timber.tag("DataCollector");
        packetSaver = new PacketSaver(context);
        PACKET_LIMIT = context.get().getResources().getInteger(R.integer.packet_runtime_limit);
    }

    /**
     * Adds an packet to {@link #packetList}. Checks if the {@link #packetList} exceeds
     * {@link #PACKET_LIMIT} and writes it to the disk if necessary.
     *
     * @param packet {@link ProcessedDataPacket} to add
     */
    public void addPacket(ProcessedDataPacket packet) {
        synchronized (packetList) {
            packetList.add(packet);
            if (packetList.size() >= PACKET_LIMIT) {
                flush();
            }
        }
    }

    /**
     * Saves {@link #packetList} to disk via {@link PacketSaver}.
     */
    public void flush() {
        synchronized (packetList) {
            if (!packetList.isEmpty()) {
                packetSaver.save(packetList);
                packetList.clear();
            }
        }
    }
}
