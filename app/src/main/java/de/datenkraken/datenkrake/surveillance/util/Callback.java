package de.datenkraken.datenkrake.surveillance.util;

/**
 * Utility callback function used by
 * {@link de.datenkraken.datenkrake.surveillance.background.BackgroundPacketSender} and
 * {@link de.datenkraken.datenkrake.surveillance.ISendProcessedData}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public interface Callback {

    /**
     * Gets called on success.
     */
    void onSuccess();

    /**
     * Gets called on failure.
     */
    void onFailure();
}
