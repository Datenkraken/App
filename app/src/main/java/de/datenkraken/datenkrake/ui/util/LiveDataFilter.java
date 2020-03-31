package de.datenkraken.datenkrake.ui.util;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generic Class, used to Filter LiveData objects. <br>
 * Filters when LiveData is updated, works asynchronously. <br>
 * Behavior is defined by it's {@link FilterComponent}s.
 *
 * @param <T> Type of Object to Filter
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class LiveDataFilter<T> {
    private final MutableLiveData<List<T>> liveDataOutput;
    private final MutableLiveData<List<T>> liveDataInput;
    private final List<FilterComponent<T, ?>> filterComponents;


    /**
     * Constructor initialing important components components of this class and adding observer
     * to given filterable content to omit it into {@link #liveDataInput}. We have to use
     * {@link #liveDataInput} so we can notify it's observer via
     * {@link MutableLiveData#postValue(Object)}. Also ads an {@link androidx.lifecycle.Observer}
     * to {@link #liveDataInput} to filter as soon as the value changes. It filters by iterating
     * over {@link #filterComponents} and calling {@link FilterComponent#checkForValidity(Object)}
     * for each {@link T} in the {@link List} of {@link T}s in the given {@link LiveData}.
     *
     * @param filterAbleContent {@link List} of {@link T} to filter.
     */
    public LiveDataFilter(LiveData<List<T>> filterAbleContent) {
        liveDataOutput = new MutableLiveData<>();
        liveDataInput = new MutableLiveData<>();
        filterComponents = new ArrayList<>();
        filterAbleContent.observeForever(liveDataInput::postValue);

        liveDataInput.observeForever(ts -> {
            if (ts == null) {
                liveDataOutput.postValue(null);
                return;
            }
            List<T> result = new ArrayList<>(ts);

            Iterator<T> iterator = result.iterator();
            while (iterator.hasNext()) {
                T t = iterator.next();
                for (FilterComponent<T, ?> filterComponent : filterComponents) {
                    if (!filterComponent.checkForValidity(t)) {
                        iterator.remove();
                    }
                }
            }
            liveDataOutput.postValue(result);
        });
    }

    /**
     * Adds an {@link FilterComponent} to {@link #filterComponents} and decorates
     * {@link FilterComponent#validValue} with a {@link androidx.lifecycle.Observer} to update
     * {@link #liveDataInput} as soon as it changes.
     *
     * @param filterComponent {@link FilterComponent} to add
     * @param <S> Type of value the {@link FilterComponent} also accepts.
     */
    public <S> void addFilterComponent(FilterComponent<T, S> filterComponent) {
        if (filterComponent.validValue != null) {
            filterComponent.validValue.observeForever(s ->
                liveDataInput.postValue(liveDataInput.getValue()));
        }
        filterComponents.add(filterComponent);
    }

    /**
     * Returns the output of this filter.
     *
     * @return {@link LiveData} with {@link List} of {@link T}
     */
    public LiveData<List<T>> getLiveDataOutput() {
        return liveDataOutput;
    }
}
