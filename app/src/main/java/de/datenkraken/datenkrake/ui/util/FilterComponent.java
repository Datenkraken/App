package de.datenkraken.datenkrake.ui.util;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * Filter component used by {@link LiveDataFilter}, able to decide if a given object of Type
 * {@link T} is valid or should be removed. {@link #validValue} is used to make this
 * {@link FilterComponent} dynamic.
 *
 * @param <T> Type of object which will be validated
 * @param <S> Type of value which is required to validate objects.
 */
public abstract class FilterComponent<T, S> {
    final LiveData<S> validValue;

    /**
     * Constructor of this class setting {@link #validValue}.
     *
     * @param validValue {@link LiveData} of value which is used to validate objects.
     */
    public FilterComponent(@NonNull LiveData<S> validValue) {
        this.validValue = validValue;
    }

    /**
     * Simple wrapper function to call {@link #isValid(Object, Object)}.
     *
     * @param t object to validate
     * @return true if valid, otherwise false.
     */
    boolean checkForValidity(T t) {
        return isValid(t, validValue.getValue());
    }

    /**
     * Implemented by subclasses to contain functionality to validate the given object.
     *
     * @param t object to validate
     * @param s value of {@link #validValue} required to validate
     * @return true if valid, otherwise false.
     */
    public abstract boolean isValid(T t, S s);
}
