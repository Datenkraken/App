package de.datenkraken.datenkrake.surveillance.background;

import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import timber.log.Timber;

/**
 * Provides a stream of list of {@link ProcessedDataPacket} from the current file.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class PacketListStream {

    private static int MAX_VALID_SIZE = 10 * 1024 * 1024; //no packet batch should be bigger than 10 MB

    private final FileInputStream fileInputStream;

    /**
     * Constructs this class and tries to open a stream to the given file.
     *
     * @param file to open
     * @throws IOException when it was unable to open the file
     */
    public PacketListStream(File file) throws IOException {
        Timber.tag("PacketListStream");
        fileInputStream = new FileInputStream(file);
    }

    /**
     * Loads the next batch of {@link ProcessedDataPacket} from the current File.
     * Returns null if the end of file was reached.
     *
     * @return List of {@link ProcessedDataPacket} or null
     */
    public List<ProcessedDataPacket> loadNext() {
        if (fileInputStream == null) {
            return null;
        }
        byte[] bytes;
        try {
            int size = getInteger();
            if (size > MAX_VALID_SIZE) { //quickfix
                fileInputStream.close();
                return null;
            }
            Timber.d("trying to allocate %s kb", ((double) size) / 1000);
            bytes = new byte[size];
            Timber.d("remaining: %d", fileInputStream.available());
            int read = fileInputStream.read(bytes);
            Timber.d("read %d bytes!", read);
            if (read == -1) {
                fileInputStream.close();
                return null;
            }

        } catch (IOException e) {
            Timber.d(e, "No more data to read");
            return null;
        }

        List<ProcessedDataPacket> list = new ArrayList<>();
        byte[] decompressedBytes = decompress(bytes);
        if (decompressedBytes == null) {

            return list;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decompress(bytes));
        ProcessedDataPacket packet;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            while (true) {
                packet = ProcessedDataPacket.load(objectInputStream);
                if (packet != null) {
                    list.add(packet);
                }
            }
        } catch (EOFException e) {
            Timber.d("Reached end of file");
            return list;
        } catch (IOException e) {
            Timber.e(e, "Failed to read from ByteArray?!");
        }
        return list;
    }

    /**
     * Returns the next integer in {@link #fileInputStream}.
     *
     * @return next Integer
     * @throws IOException when no Integer was found.
     */
    private Integer getInteger() throws IOException {

        // this resource mustn't get closed, otherwise the fileInputStream will get closed.
        @SuppressWarnings("PMD.CloseResource")
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        int integer = dataInputStream.readInt();
        Timber.d("Trying to load %d Bytes", integer);
        return integer;
    }

    /**
     * Tries to decompress the given byte-array with {@link java.util.zip.Deflater}.
     *
     * @param bytes bytes to decompress
     * @return null if the decompressing failed, the decompress bytes otherwise.
     */
    private byte[] decompress(byte[] bytes) {
        Inflater decompressor = new Inflater();
        decompressor.setInput(bytes);

        /* From https://stackoverflow.com/questions/25642591/how-to-calculate-byte-size-for-deflater-compression-output-in-android */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try {
            while (!decompressor.finished()) {
                int byteCount = decompressor.inflate(buf);
                baos.write(buf, 0, byteCount);
            }
        } catch (DataFormatException e) {
            Timber.e(e, "Tried to load wrong format");
            return null;
        }
        decompressor.end();
        return baos.toByteArray();
    }

    /**
     * Tries to close the current stream.
     */
    public void close() {
        if (fileInputStream == null) {
            return;
        }

        try {
            fileInputStream.close();
        } catch (IOException e) {
            Timber.e(e,"failed to close stream!");
        }
    }
}
