package de.datenkraken.datenkrake.ui.recommendation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.model.Category;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.repository.SourceRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * This ViewModel is responsible for fetching {@link Source}s from the server
 * and providing them to the view. <br>
 * It also retrieves {@link Source}s from the {@link androidx.room.Database}
 * that have already been added by the user to find out
 * which ones should be displayed in the recommendation page. <br>
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class RecommViewModel extends AndroidViewModel {

    // used to store the selected sources and retrieve existing ones to find out
    // which ones not to display again
    private final SourceRepository repository;

    // list of fetched categories with according sources
    private LiveData<List<Category>> categories;
    public MediatorLiveData<Map<String, Boolean>> sourceStatus = new MediatorLiveData<>();
    private LiveData<List<Source>> fetchedSources = new MutableLiveData<>();
    private final LiveData<List<Source>>  databaseSources;
    private final Map<String, Source> sourceMap = new HashMap<>();

    /**
     * Constructor for this class.
     *
     * @param application Application.
     */
    public RecommViewModel(@NonNull Application application) {
        super(application);

        Timber.tag("SourcesViewModel");

        repository = ((DatenkrakeApp) application).getSourceRepository();
        databaseSources = repository.getSources();
        sourceStatus.addSource(fetchedSources, sources -> {});
        sourceStatus.addSource(databaseSources, sources -> {
            if (sources == null) {
                return;
            }

            Map<String, Boolean> stati = new HashMap<>();
            if (fetchedSources.getValue() == null) {
                for (Source source : sources) {
                    stati.put(source.url.toString(), true);
                    sourceMap.put(source.url.toString(), source);
                }
                sourceStatus.postValue(stati);
                return;
            }

            for (Source source : fetchedSources.getValue()) {
                stati.put(source.url.toString(), false);
            }

            for (Source source : sources) {
                if (stati.containsKey(source.url.toString())) {
                    stati.replace(source.url.toString(), true);
                    sourceMap.replace(source.url.toString(), source);
                }
            }
            sourceStatus.postValue(stati);
        });


    }

    /**
     * Fetches the latest {@link GetCategoriesQuery.Category}s from the server.
     */
    public void fetchCategories() {
        // fetch categories
        // used for fetching categories and sources from the server
        CategoryFetcher task = new CategoryFetcher();
        task.request();
        this.categories = task.categories;
        this.sourceStatus.removeSource(this.fetchedSources);
        this.fetchedSources = Transformations.map(task.categories, newCategories -> {
            if (newCategories == null) {
                return new ArrayList<>();
            }
            List<Source> sources = new ArrayList<>();
            for (Category category : newCategories) {
                sources.addAll(category.sources);
            }
            return sources;
        });
        sourceStatus.addSource(fetchedSources, sources -> {
            if (sources == null) {
                return;
            }
            Map<String, Boolean> stati = new HashMap<>();
            for (Source source : sources) {
                stati.put(source.url.toString(), false);
                if (!sourceMap.containsKey(source.url.toString())) {
                    sourceMap.put(source.url.toString(), source);
                }
            }

            if (databaseSources.getValue() == null) {
                sourceStatus.postValue(stati);
                return;
            }

            for (Source source : databaseSources.getValue()) {
                if (stati.containsKey(source.url.toString())) {
                    stati.replace(source.url.toString(), true);
                }
            }
            sourceStatus.postValue(stati);
        });

    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public boolean toggleSelection(String url) {
        if (sourceStatus.getValue() == null) {
            return false;
        }

        boolean status = !sourceStatus.getValue().get(url);
        sourceStatus.getValue().replace(url, status);
        return status;
    }

    /**
     * When clicking the submit button all stored sources will be added to the
     * {@link androidx.room.RoomDatabase} through the {@link SourceRepository}.
     */
    public void editSources() {
        if (sourceStatus.getValue() == null) {
            return;
        }
        Source source;
        for (Map.Entry<String, Boolean> entry : sourceStatus.getValue().entrySet()) {
            source = sourceMap.get(entry.getKey());
            Timber.d("processing source %s, with id: %d", source.name, source.uid);
            if (entry.getValue() && source.uid == -1) {
                repository.insertSource(source);
            } else if (!entry.getValue() && source.uid != -1) {
                repository.deleteSource(source);
            }
        }
    }
}
