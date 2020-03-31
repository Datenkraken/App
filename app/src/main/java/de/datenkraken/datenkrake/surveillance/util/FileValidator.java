package de.datenkraken.datenkrake.surveillance.util;

import java.io.File;

/**
 * Used by {@link FileUtil#getValidFiles(String[], int, FileValidator)} to validate files.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public interface FileValidator {

    /**
     * Validates the given file.
     *
     * @param file to validate
     * @return true if valid, false otherwise
     */
    boolean validate(File file);
}
