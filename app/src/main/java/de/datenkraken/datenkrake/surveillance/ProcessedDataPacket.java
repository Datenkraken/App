package de.datenkraken.datenkrake.surveillance;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Class providing functionality to save and load serializable objects. <br>
 * Used to save the output from {@link IEventProcessor} and
 * {@link de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor} via the
 * {@link PacketSaver} to the disc. Will be loaded again by
 * {@link de.datenkraken.datenkrake.surveillance.background.PacketListStream}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@SuppressWarnings("PMD.TooManyMethods") // this class is allowed to have many methods
public class ProcessedDataPacket {

    private final Map<String, ? super Serializable> values = new HashMap<>();//NOPMD
    private String taskId;

    private static final Integer VERSION = 1;

    /**
     * Constructor of this class.
     *
     * @param taskId required to identify the {@link ISendProcessedData} which consumes this packet.
     */
    public ProcessedDataPacket(String taskId) {
        this();
        this.taskId = taskId;
    }

    private ProcessedDataPacket() {
        Timber.tag("DataPacket");
    }

    /**
     * Inserts a boolean with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putBoolean(String key, Boolean value) {
        values.put(key, value);
    }

    /**
     * Inserts a char with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putChar(String key, Character value) {
        values.put(key, value);
    }

    /**
     * Inserts a byte with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putByte(String key, Byte value) {
        values.put(key, value);
    }

    /**
     * Inserts a short with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putShort(String key, Short value) {
        values.put(key, value);
    }

    /**
     * Inserts a integer with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putInteger(String key, Integer value) {
        values.put(key, value);
    }

    /**
     * Inserts a long with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putLong(String key, Long value) {
        values.put(key, value);
    }

    /**
     * Inserts a float with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putFloat(String key, Float value) {
        values.put(key, value);
    }

    /**
     * Inserts a double with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putDouble(String key, Double value) {
        values.put(key, value);
    }

    /**
     * Inserts a string with the given key.
     *
     * @param key key for this value
     * @param value value for this key
     */
    public void putString(String key, String value) {
        values.put(key, value);
    }

    /**
     * Inserts a serializable object with the given key.
     *
     * @param <T> Class of the given Object
     * @param key key for this value
     * @param value value for this key
     */
    public  <T extends Serializable> void putObject(String key, T value) {
        values.put(key, value);
    }

    /**
     * Returns the boolean with the given key or default if the value doesn't exists or it is not a
     * boolean.
     *
     * @param key key for this Boolean.
     * @param def default value.
     * @return Boolean
     */
    public Boolean getBoolean(String key, Boolean def) {
        if (!exists(key, Boolean.class)) {
            return def;
        }
        return (Boolean) values.get(key);
    }

    /**
     * Returns the character with the given key or default if the value doesn't exists or it is not
     * a character.
     *
     * @param key key for this Character.
     * @param def default value.
     * @return Character
     */
    public Character getChar(String key, Character def) {
        if (!exists(key, Character.class)) {
            return def;
        }
        return (Character) values.get(key);
    }

    /**
     * Returns the byte with the given key or default if the value doesn't exists or it is not a
     * byte.
     *
     * @param key key for this Byte.
     * @param def default value.
     * @return Byte
     */
    public Byte getByte(String key, Byte def) {
        if (!exists(key, Byte.class)) {
            return def;
        }
        return (Byte) values.get(key);
    }

    /**
     * Returns the short with the given key or default if the value doesn't exists or it is not a
     * short.
     *
     * @param key key for this Short.
     * @param def default value.
     * @return Short
     */
    public Short getShort(String key, Short def) {
        if (exists(key, Short.class)) {
            return def;
        }
        return (Short) values.get(key);
    }

    /**
     * Returns the integer with the given key or default if the value doesn't exists or it is not a
     * integer.
     *
     * @param key key for this Integer.
     * @param def default value.
     * @return Integer
     */
    public Integer getInteger(String key, Integer def) {
        if (!exists(key, Integer.class)) {
            return def;
        }
        return (Integer) values.get(key);
    }

    /**
     * Returns the long with the given key or default if the value doesn't exists or it is not a
     * long.
     *
     * @param key key for this Long.
     * @param def default value.
     * @return Long
     */
    public Long getLong(String key, Long def) {
        if (!exists(key, Long.class)) {
            return def;
        }
        return (Long) values.get(key);
    }

    /**
     * Returns the float with the given key or default if the value doesn't exists or it is not a
     * float.
     *
     * @param key key for this Float.
     * @param def default value.
     * @return Float
     */
    public Float getFloat(String key, Float def) {
        if (!exists(key, Float.class)) {
            return def;
        }
        return (Float) values.get(key);
    }

    /**
     * Returns the double with the given key or default if the value doesn't exists or it is not a
     * double.
     *
     * @param key key for this Double.
     * @param def default value.
     * @return Double
     */
    public Double getDouble(String key, Double def) {
        if (!exists(key, Double.class)) {
            return def;
        }
        return (Double) values.get(key);
    }

    /**
     * Returns the string with the given key or default if the value doesn't exists or it is not a
     * string.
     *
     * @param key key for this String.
     * @param def default value.
     * @return String
     */
    public String getString(String key, String def) {
        if (!exists(key, String.class)) {
            return def;
        }
        return (String) values.get(key);
    }

    /**
     * Returns the object with the given key or default if the value doesn't exists or it is the
     * expected class.
     *
     * @param <O> class of the expected Object
     * @param type expected Class of the object
     * @param key key for this Object.
     * @param def default value.
     * @return Object
     */
    public <O extends Serializable> O getObject(Class<O> type, String key, O def) {
        if (!exists(key, type)) {
            return def;
        }
        return (O) values.get(key);
    }

    /**
     * Checks if a value at the given key exists and its of the same type as the given class.
     *
     * @param key key
     * @param c type of expected object
     * @return true or false
     */
    private boolean exists(String key, Class c) {
        if (!values.containsKey(key)) {
            return false;
        }
        Object obj = values.get(key);
        return (obj != null && obj.getClass() == c);
    }

    /**
     * Returns the id identifying the {@link ISendProcessedData} by which this packet should be
     * consumed.
     *
     * @return {@link ISendProcessedData} id
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Ensures that important data gets saved into an object output stream, independent of the rest
     * of the packet.
     *
     * @param stream Stream to save to
     * @throws IOException thrown if the stream is not writeable
     */
    void save(ObjectOutputStream stream) throws IOException {
        stream.writeObject(VERSION);
        saveData(stream);
    }

    /**
     * Saves the packet content into an object output stream.
     *
     * @param stream Stream to save to
     * @throws IOException thrown if the stream is not writeable
     */
    private void saveData(ObjectOutputStream stream) throws IOException {
        stream.writeObject(getTaskId());
        Integer size = values.size();
        stream.writeObject(size);
        for (Map.Entry<String, ? super Serializable> entry : values.entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    /**
     * Loads the version from the object input stream and calls the appropriate loading method to
     * load the content of this packet.
     *
     * @param stream stream to load from
     * @return the recreatede {@link ProcessedDataPacket}
     * @throws IOException thrown if the stream is not readable or has no more bytes
     */
    public static ProcessedDataPacket load(ObjectInputStream stream) throws IOException {
        ProcessedDataPacket processedDataPacket = new ProcessedDataPacket();
        try {
            Integer version = (Integer) stream.readObject();
            switch (version) {
                case 1:
                    loadVersion1(processedDataPacket, stream);
                    return processedDataPacket;
                default:
                    return null;
            }
        } catch (ClassNotFoundException e) {
            Timber.e(e, "rip");
        }
        return null;
    }

    /**
     * Loads the content of the first version of {@link ProcessedDataPacket}.
     *
     * @param processedDataPacket {@link ProcessedDataPacket} to initialize
     * @param stream object output stream to load from
     * @throws IOException thrown if the stream is not readable or has no more bytes
     * @throws ClassNotFoundException thrown if it couldn't find the appropriate class for the loaded object
     */
    private static void loadVersion1(ProcessedDataPacket processedDataPacket, ObjectInputStream stream)
                                            throws IOException, ClassNotFoundException {
        processedDataPacket.taskId = (String) stream.readObject();
        Integer size = (Integer) stream.readObject();
        for (int i = 0; i < size; i++) {
            processedDataPacket.values.put((String) stream.readObject(), (Serializable) stream.readObject());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + "id='" + taskId + "', "
            + "version=" + VERSION + ", "
            + "values=" + values.toString()
            + "}";
    }
}
