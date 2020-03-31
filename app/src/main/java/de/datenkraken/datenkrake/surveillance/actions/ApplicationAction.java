package de.datenkraken.datenkrake.surveillance.actions;

import de.datenkraken.datenkrake.type.AppEventType;

/**
 * Application action enum, so that {@link de.datenkraken.datenkrake.type.AppEventType} is not used
 * by the rest of this application except
 * {@link de.datenkraken.datenkrake.surveillance.sender.ApplicationActionSender}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public enum ApplicationAction {

    SCROLL(AppEventType.SCROLL.rawValue()),
    BACKGROUND(AppEventType.BACKGROUND.rawValue()),
    STARTED(AppEventType.STARTED.rawValue()),
    CLOSED(AppEventType.CLOSED.rawValue()),
    $UNKNOWN(AppEventType.$UNKNOWN.rawValue());

    private final String value;

    /**
     * Constructs this enum.
     *
     * @param value value of this enum
     */
    ApplicationAction(String value) {
        this.value = value;
    }

    /**
     * Returns the value of this constant.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }
}
