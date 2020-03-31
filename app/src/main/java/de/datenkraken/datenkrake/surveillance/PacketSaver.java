package de.datenkraken.datenkrake.surveillance;

import android.content.Context;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.surveillance.util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;

import timber.log.Timber;

/**
 * Class providing functionality to save {@link ProcessedDataPacket}s to the packet cache.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class PacketSaver {

    private final WeakReference<Context> context;
    private final int MAX_FILES_PER_TASK;
    private final int PATH_MAX_LENGTH;
    private final int MAX_CACHE_SIZE;

    /**
     * Constructor of this class and initializes the constants.
     *
     * @param context used to load the constants from xml-resources
     */
    public PacketSaver(WeakReference<Context> context) {
        this.context = context;
        Timber.tag("PacketSaver");
        MAX_FILES_PER_TASK = context.get().getResources().getInteger(R.integer.packet_cache_files_per_mutation);
        PATH_MAX_LENGTH = context.get().getResources().getInteger(R.integer.packet_cache_max_path_length);
        MAX_CACHE_SIZE = context.get().getResources().getInteger(R.integer.packet_cache_limit_bytes);
    }

    /**
     * Attempts to compress the given list of {@link ProcessedDataPacket} and saves
     * the result to the cache.
     *
     * @param processedDataPackets List of {@link ProcessedDataPacket} to save
     * @return true if successful, false otherwise
     */
    public boolean save(List<ProcessedDataPacket> processedDataPackets) {
        if (context.get() == null) {
            Timber.d("context is null");
            return false;
        }

        Map<String, List<ProcessedDataPacket>> sortedPackets = new HashMap<>();//NOPMD

        for (ProcessedDataPacket packet : processedDataPackets) {
            if (!sortedPackets.containsKey(packet.getTaskId())) {
                sortedPackets.put(packet.getTaskId(), new ArrayList<>());
            }

            sortedPackets.get(packet.getTaskId()).add(packet);
        }

        Set<String> keys = sortedPackets.keySet();
        boolean result = true;

        File dir = FileUtil.getDataDir(context.get().getCacheDir().getPath(), context.get());
        long size = getCacheSize(dir); // Total size of current cache directory

        if (size < 0) {
            return false;
        }

        for (String key : keys) {
            int writtenBytes = saveListOfPacketType(dir, size, key, sortedPackets.get(key));
            if (writtenBytes > 0) {
                size += writtenBytes; // add written size to total size of directory
            } else if (writtenBytes < 0) {
                result = false; // if it couldn't write anything, an error occurred
            }
        }

        return result;
    }

    /**
     * Saves the list of {@link ProcessedDataPacket}s to a single file.
     * Type defines the name of the file.
     *
     * @param dir Directory in which the File should be saved
     * @param currentDirSize Size of the current Directory
     * @param type Type of {@link ProcessedDataPacket}s
     * @param packets List of {@link ProcessedDataPacket}s to save
     * @return bytes written. -1 if an error occurred
     */
    private int saveListOfPacketType(File dir, Long currentDirSize, String type, List<ProcessedDataPacket> packets) {
        byte[] bytes = toByteArray(packets);

        Timber.d("byte array length is %d", bytes.length);
        if (bytes.length == 0) {
            return 0;
        }

        byte[] compressed = compress(bytes);

        if (currentDirSize + compressed.length > MAX_CACHE_SIZE) {
            return -1;
        }

        Timber.d("compressed byte array length is %d", compressed.length);

        if (dir == null) {
            return -1;
        }

        try {
            if (saveFile(dir, compressed, type)) {
                return compressed.length;
            }
        } catch (IOException e) {
            Timber.e(e, "Couldn't save data!");
        }

        return -1;
    }

    /**
     * Compresses the given byte array.
     *
     * @param bytes byte array to compress
     * @return compressed byte array
     */
    private byte[] compress(byte[] bytes) {
        Deflater compressor = new Deflater();
        compressor.setInput(bytes);
        compressor.finish();

        /* From https://stackoverflow.com/questions/25642591/how-to-calculate-byte-size-for-deflater-compression-output-in-android */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        while (!compressor.finished()) {
            int byteCount = compressor.deflate(buf);
            baos.write(buf, 0, byteCount);
        }
        compressor.end();
        return baos.toByteArray();
    }

    /**
     * Parses the given List of {@link ProcessedDataPacket} to an byte array by using
     * {@link ProcessedDataPacket#save(ObjectOutputStream)} of each {@link ProcessedDataPacket}.
     *
     * @param processedDataPackets List of {@link ProcessedDataPacket} to parse.
     * @return byte array
     */
    private byte[] toByteArray(List<ProcessedDataPacket> processedDataPackets) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(byteArray)) {
            for (ProcessedDataPacket packet : processedDataPackets) {
                packet.save(outputStream);
            }
            outputStream.flush();
        } catch (IOException ignored) {
            /* we are saving the content of the data packets to a byte array, so a exception should
               never get thrown.*/
        }

        return byteArray.toByteArray();
    }

    /**
     * Saves the given byte array to a file in the given directory. First tries to append the byte
     * array to an existing file. Creates up to {@link #MAX_FILES_PER_TASK} files if it can't
     * access it.
     *
     * @param dir directory for the file
     * @param bytes byte array to save
     * @throws IOException thrown if unable to write
     */
    private boolean saveFile(File dir, byte[] bytes, String name) throws IOException {
        String[] pathList = new String[MAX_FILES_PER_TASK];

        for (int i = 0; i < pathList.length; i++) {
            pathList[0] = trimPath(dir.getPath() + File.separator + name) + i;
        }

        List<File> files = FileUtil.getValidFiles(pathList, 1, File::canWrite);

        if (files.isEmpty()) {
            return false;
        }

        File file = files.get(0);

        try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
            (new DataOutputStream(outputStream)).writeInt(bytes.length);
            outputStream.write(bytes);
        }

        return true;
    }

    /**
     * Trims the given path to {@link #PATH_MAX_LENGTH}. Important cause untrimmed path length are
     * about 122 Characters long, which is pretty close to androids limit of 127.
     *
     * @param path to trim
     * @return trimmed path
     */
    private String trimPath(String path) {
        return path.substring(0, PATH_MAX_LENGTH - 1);
    }

    /**
     * Returns the combined size of all files in the given directory.
     *
     * @param dir {@link File}
     * @return size of all files in the given directory or -1 if it failed to read them
     */
    private long getCacheSize(File dir) {
        if (dir == null) {
            return -1;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return -1;
        }

        long size = 0;

        for (File file : files) {
            size += file.length();
        }

        return size;
    }

}
