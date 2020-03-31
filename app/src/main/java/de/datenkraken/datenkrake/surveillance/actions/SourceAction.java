package de.datenkraken.datenkrake.surveillance.actions;

import de.datenkraken.datenkrake.type.SourceEventType;

/**
 * Source action enum, so that {@link de.datenkraken.datenkrake.type.SourceEventType} is not used
 * by the rest of this application except
 * {@link de.datenkraken.datenkrake.surveillance.sender.SourceActionSender}.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public enum SourceAction {

    ADDED(SourceEventType.ADDED.rawValue()),
    REMOVED(SourceEventType.REMOVED.rawValue()),
    FILTERED(SourceEventType.FILTERED.rawValue()),
    $UNKNOWN(SourceEventType.$UNKNOWN.rawValue());

    private final String value;

    /**
     * Constructs this enum.
     * @param value value of this enum
     */
    SourceAction(String value) {
        this.value = value;
    }

    /**
     * Returns the value of this constants.
     * @return value
     */
    public String getValue() {
        return value;
    }
}
