package de.datenkraken.datenkrake.surveillance;

/**
 * Enum, containing all types of events. If a {@link DataCollectionEvent} needs a new type,
 * it should be added here.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public enum DataCollectionEventType {

    DEFAULT(0),
    APPLICATIONACTION(1),
    ARTICLEACTION(2),
    ARTICLEIDACTION(3),
    SOURCEACTION(4),
    SOURCEIDACTION(5);

    private int id;

    DataCollectionEventType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
