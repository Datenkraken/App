package de.datenkraken.datenkrake.util;

/**
 * Generic event class that is used for passing events via live data.
 * The Event carries a content, that can be accessed via getContentIfNotHandled or peekContent.
 *
 * @param <T> Type of the event
 * @author Jan Klinkmann - jan.klinkmann@stud.tu-darmstadt.de
 */
public class Event<T> {
    // Content carried.
    private final T content;
    // Boolean, that is true, if content was accessed by getContentIfNotHandled.
    private Boolean handled = false;

    /**
     * Constructor for the Event, creating a new instance carrying the given content.
     *
     * @param content to be carried by the event.
     */
    public Event(T content) {
        this.content = content;
    }

    /**
     * Get content of the event if the event has not been handled yet (handled is false).
     * After accessing the content, handled will be set to true.
     * Thus, the content can not be accessed again with this function
     * If the content already has been handled, this function returns null.
     *
     * @return the content of the event or null if the event has been handled.
     */
    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        } else {
            handled = true;
            return content;
        }
    }

    /**
     * Get content ignoring if the event has been handled or not.
     * This function does not set a value for handled.
     * Because of this, the content can be accessed multiple times with this function.
     *
     * @return the content of the event.
     */
    public T peekContent() {
        return this.content;
    }
}
