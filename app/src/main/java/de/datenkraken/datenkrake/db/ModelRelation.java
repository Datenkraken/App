package de.datenkraken.datenkrake.db;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

/**
 * Abstract class to display a relation between {@link Article}s and {@link Source}s.
 */
public abstract class ModelRelation {

    /**
     * Sets the Source reference in {@link Article} to reference the {@link Source} in this relation.
     */
    abstract void reference();
}
