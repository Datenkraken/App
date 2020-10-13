package de.datenkraken.datenkrake.util;

/**
 * Provides Callback functionality to display the result of an action.
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
