package de.datenkraken.datenkrake.surveillance.graphqladapter;

import de.datenkraken.datenkrake.type.AppPermission;

/**
 * Application action enum, so that {@link AppPermission} is not used
 * by the rest of this application.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public enum Permission {

    LOCATION(AppPermission.ACCESS_LOCATION.rawValue()),
    $UNKNOWN(AppPermission.$UNKNOWN.rawValue());

    private final String value;

    /**
     * Constructs this enum.
     *
     * @param value value of this enum
     */
    Permission(String value) {
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
