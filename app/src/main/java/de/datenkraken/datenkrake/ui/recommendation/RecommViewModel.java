package de.datenkraken.datenkrake.ui.recommendation;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.GetCategoriesOnlyQuery;
import de.datenkraken.datenkrake.GetCategoriesQuery;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.repository.SourceRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * This ViewModel is responsible for fetching {@link GetCategoriesOnlyQuery.Category}s
 * and {@link GetCategoriesQuery.RssSource}s from the server and providing them to the views. <br>
 * It also retrieves {@link Source}s from the {@link androidx.room.Database}
 * that have already been added by the user to find out
 * which ones should be displayed in the recommendation page. <br>
 * It is also used to temporarily store added {@link GetCategoriesOnlyQuery.Category}s and
 * {@link GetCategoriesQuery.RssSource}s so the latter can be added after submission by the user.<br>
 * It is shared among the classes in the recommendation package.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class RecommViewModel extends AndroidViewModel {

    // used to store the selected sources and retrieve existing ones to find out
    // which ones not to display again
    private final SourceRepository repository;

    // set of categories that are temporarily added while user
    // is still browsing the recommendation view
    public Map<String, GetCategoriesOnlyQuery.Category> selectedCategories;

    // list of fetched categories with according sources
    public List<GetCategoriesQuery.Category> categories;

    // set that stores the sources that have already been stored in the DB
    public Set<String> existingSources;

    // set of sources that are temporarily added while user is still browsing the recommendation view
    public Set<GetCategoriesQuery.RssSource> storedSources;

    // used for fetching categories and sources from the server
    private CategoryOnlyFetcher task;

    /**
     * Constructor for this class.
     *
     * @param application Application.
     */
    public RecommViewModel(@NonNull Application application) {
        super(application);

        Timber.tag("SourcesViewModel");

        repository = ((DatenkrakeApp) application).getSourceRepository();
        storedSources = new HashSet<>();

    }

    /**
     * Fetches the latest {@link GetCategoriesOnlyQuery.Category}s from the server.
     * The according {@link GetCategoriesQuery.RssSource}s will be fetched afterwards for better response time.
     *
     * @return List of {@link GetCategoriesOnlyQuery.Category}s including their respective sources
     fetched from the server
     */
    public LiveData<List<GetCategoriesOnlyQuery.Category>> fetchCategories() {
        // fetch categories
        task = new CategoryOnlyFetcher();
        task.request();

        return task.getCategoryNames();
    }

    /**
     * Temporarily stores {@link GetCategoriesQuery.RssSource}s in HashSet
     * so they can be added all at once at submission.
     *
     * @param source of type GetCategoriesQuery.RssSource
     */
    public void pickSource(GetCategoriesQuery.RssSource source) {
        storedSources.add(source);

    }

    /**
     * Deletes a {@link GetCategoriesQuery.RssSource} from the HashSet.
     *
     * @param source of type GetCategoriesQuery.RssSource
     */
    public void unpickSource(GetCategoriesQuery.RssSource source) {
        storedSources.remove(source);
    }


    /**
     * When clicking the submit button all stored sources will be added to the
     * {@link androidx.room.RoomDatabase} through the {@link SourceRepository}. <br>
     * The temporarily stored {@link GetCategoriesQuery.RssSource}s then get cleared.
     * @throws MalformedURLException thrown, if the url of the from server received source is invalid.
     */
    public void addSources() throws MalformedURLException {

        for (GetCategoriesQuery.RssSource source : storedSources) {
            Source newSource = new Source();
            newSource.name = source.name();
            newSource.url = new URL(source.url());
            repository.insertSource(newSource);
        }
        storedSources.clear();
    }

    /**
     * Returns LiveData list of {@link Source}s in DB.
     *
     * @return LiveData list of type {@link Source}.
     */
    public LiveData<List<Source>> getSavedSources() {
        return repository.getSources();
    }


    /**
     * Returns LiveData list of {@link GetCategoriesQuery.Category} from server.
     *
     * @return LiveData list of type {@link GetCategoriesQuery.Category}
     */
    public LiveData<List<GetCategoriesQuery.Category>> fetchCategorySources() {

        return task.getCategories();
    }


    /**
     * Observer method to add already existing {@link Source}s to existingSources.
     * It does not add them if the {@link Source} still exists but has been marked as deleted.
     *
     * @param sources List of type {@link Source}.
     */
    public void setSources(List<Source> sources) {
        // check if source has been deleted?
        for (Source s : sources) {
            if (!s.deleted) {
                existingSources.add(s.url.toString());
            }
        }

    }

}
