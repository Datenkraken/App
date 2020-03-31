package de.datenkraken.datenkrake.surveillance.actions;

import de.datenkraken.datenkrake.type.ArticleEventType;

/**
 * Enum for article actions, so that {@link de.datenkraken.datenkrake.type.AppEventType} is not used
 * by the rest of this application except
 * {@link de.datenkraken.datenkrake.surveillance.sender.ArticleActionSender}.
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public enum ArticleAction {

    OPENED(ArticleEventType.OPENED.rawValue()),
    CHROMEOPENED(ArticleEventType.CHROMEOPENED.rawValue()),
    SAVED(ArticleEventType.SAVED.rawValue()),
    SHARED(ArticleEventType.SHARED.rawValue()),
    $UNKNOWN(ArticleEventType.$UNKNOWN.rawValue());

    private final String value;

    ArticleAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
