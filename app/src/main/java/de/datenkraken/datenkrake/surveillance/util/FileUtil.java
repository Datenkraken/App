package de.datenkraken.datenkrake.surveillance.util;

import android.content.Context;

import de.datenkraken.datenkrake.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * Utility class, providing functionality to work with the filesystem.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public final class FileUtil {


    /**
     * Private constructor so this class doesn't get instantiated and silence PMD.
     */
    private FileUtil() {

    }

    /**
     * Returns the cache directory from the given root directory.
     *
     * @param root root directory
     * @param context current context
     * @return cache directory
     */
    public static File getDataDir(String root, Context context) {
        File dir = new File(root + File.separator
            + context.getResources().getString(R.string.packet_cache_folder));
        if (!dir.exists() && !dir.mkdir()) {
            Timber.d("couldn't create data dir!");
            return null;
        }
        return dir;
    }

    /**
     * Reads all files from the given dir and sorts them by their modified age. <br>
     * The file at position 0 will be the oldest.
     *
     * @param dir dir to load the files from.
     * @return sorted array of files.
     */
    public static File[] getSortedContent(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return new File[0];
        }

        Arrays.sort(files,  (o1, o2) -> Long.compare(o1.lastModified(), o2.lastModified()));
        return files;
    }


    /**
     * Returns a list of valid files. Checks until all available names are checked or the limit of
     * maxValidFiles is reached. Often times, files are invalid cause a background process is
     * accessing them
     * (e.g. {@link de.datenkraken.datenkrake.surveillance.background.BackgroundPacketSender}).
     *
     * @param paths file paths to validate
     * @param maxValidFiles maximal number of valid files this function should return
     * @param validator {@link FileValidator} this function uses to validate the files
     * @return List of files or null
     */
    @SuppressWarnings("PMD.EmptyCatchBlock") // see comment in catch block
    public static List<File> getValidFiles(String[] paths, int maxValidFiles, FileValidator validator) {
        List<File> validFiles = new ArrayList<>();
        File file;

        for (int i = 0; i < paths.length && validFiles.size() < maxValidFiles; i++) {
            file = new File(paths[i]);

            if (file.exists()) {
                if (validator.validate(file)) {
                    validFiles.add(file);
                } else {
                    continue;
                }
            }

            try {
                if (file.createNewFile() && validator.validate(file)) {
                    validFiles.add(file);
                }
            } catch (IOException ignore) {
                // We can't create a new file? Lets try it with the next one.
            }
        }

        return validFiles;
    }
}
