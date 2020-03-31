package de.datenkraken.datenkrake.surveillance.background;

import android.content.Context;

import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Class providing functionality to load from the packet cache.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class PacketLoader {

    private int index = -1;
    private PacketListStream packetListStream;
    private List<File> files;

    /**
     * Constructor for this class, initializes the constants and checking for any valid files in
     * the packet cache directory.
     *
     * @param context used to load the constants from xml-resources
     */
    PacketLoader(Context context) throws IOException {
        Timber.tag("PacketLoader");
        File dir = FileUtil.getDataDir(context.getCacheDir().getPath(), context);
        if (dir == null) {
            throw new IOException("Could not access cache directory!");
        }
        String[] filePaths = dir.list();

        if (filePaths == null) {
            files = new ArrayList<>();
        } else {
            for (int i = 0; i < filePaths.length; i++) {
                filePaths[i] = dir.getPath() + File.separator + filePaths[i];
            }
            files = FileUtil.getValidFiles(filePaths, filePaths.length, File::canRead);
        }
    }

    /**
     * Attempts to load one cache file and parse it back to a list of {@link ProcessedDataPacket}s.
     *
     * @return null if the cache is empty or it failed to read.
     */
    List<ProcessedDataPacket> loadNext() {
        index++; // got to next file
        List<ProcessedDataPacket> listPart = null;

        for (; index < files.size(); index++) {
            if (packetListStream != null) {
                packetListStream.close();
            }

            try {
                packetListStream = new PacketListStream(files.get(index));
                listPart = packetListStream.loadNext();
            } catch (IOException e) {
                Timber.d(e, "Failed to load from valid file %s", files.get(index).getPath());
            }

            if (listPart != null) {
                break;
            }
        }

        // When the for loop didn't found any file, index is bigger than the file size.
        if (index >= files.size()) {
            return null;
        }

        List<ProcessedDataPacket> list = new ArrayList<>();
        while (listPart != null) {
            list.addAll(listPart);
            listPart = packetListStream.loadNext();
        }

        if (packetListStream != null) {
            packetListStream.close();
        }

        return list;
    }

    /**
     * Returns the current file path.
     *
     * @return current file path
     */
    String currentFilePath() {
        if (files.size() == index) {
            return null;
        }

        return files.get(index).getPath();
    }
}
