package de.datenkraken.datenkrake.controller.feedupdater.rss;

import com.rometools.rome.io.FeedException;

/**
 * Exception to get thrown, when an unsupported RSS Version tries to get parsed.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @version 1.0
 * @since 06.12.2019
 */
public class UnsupportedVersionException extends FeedException {

    static final long serialVersionUID = 5234122312222L;

    /**
     * Constructor for the exception.
     *
     * @param msg for the exception description.
     */
    UnsupportedVersionException(String msg) {
        super(msg);
    }

    /**
     * Constructor for the exception.
     *
     * @param msg for the exception description.
     * @param rootCause exception that caused this exception.
     */
    public UnsupportedVersionException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }
}
