package de.datenkraken.datenkrake.surveillance;

import androidx.annotation.Nullable;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.surveillance.background.BackgroundPacketSender;
import de.datenkraken.datenkrake.surveillance.util.Callback;

import java.util.List;

/**
 * Interface providing necessary functionality to parse {@link ProcessedDataPacket}s to
 * {@link ITask}s.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public interface ISendProcessedData {

    /**
     * Creates and returns the {@link ITask}, which will be send to the server. <br>
     * When this method gets implemented in a subclass, it should not access any member fields in
     * its class. The returned {@link ITask} should send all given packets.
     * {@link Callback#onSuccess()} must be called in the successful callback of the created
     * {@link ITask} or in this function to signalize, that the saved packets can be deleted. <br>
     *
     * @param packets {@link ProcessedDataPacket}s to send.
     * @param callback Callback which must be called, when the task was successful.
     * @return {@link ITask}
     */
    @Nullable
    ITask getTask(List<ProcessedDataPacket> packets, Callback callback);

    /**
     * Id of the {@link ITask}. Get's used to identify from which {@link IEventProcessor}
     * the {@link #getTask(List, Callback)} method should get called, if the {@link BackgroundPacketSender}
     * wants to send a {@link ProcessedDataPacket}.
     * This id should be UNIQUE.
     *
     * @return Id of the {@link ITask}.
     */
    String getTaskId();
}
