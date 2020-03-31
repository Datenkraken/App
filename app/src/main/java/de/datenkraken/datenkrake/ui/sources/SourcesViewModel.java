package de.datenkraken.datenkrake.ui.sources;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.repository.SourceRepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import timber.log.Timber;


/**
 * ViewModel that provides functionality for the {@link SourcesFragment}. <br>
 * It is used to add, delete and get the sources in the {@link SourceRepository}
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class SourcesViewModel extends AndroidViewModel {

    private final SourceRepository repository;

    /**
     * Constructor for this class.
     *
     * @param application Application, needed to access the {@link SourceRepository}
     */
    public SourcesViewModel(@NonNull Application application) {
        super(application);

        Timber.tag("SourcesViewModel");

        repository = ((DatenkrakeApp) application).getSourceRepository();
    }

    /**
     * Returns all {@link Source}s given by the {@link SourceRepository}.
     *
     * @return LiveData object containing a list of {@link Source}s.
     */
    LiveData<List<Source>> getSavedSources() {
        return repository.getSources();
    }

    /**
     * Calls the {@link SourceRepository} to delete the given {@link Source}.
     *
     * @param source to delete
     */
    public void deleteSource(Source source) {
        repository.deleteSource(source);
    }

    /**
     * Create a {@link Source} and call {@link SourceRepository} to insert it.
     *
     * @param sourceURL the url for the generated {@link Source}
     * @throws MalformedURLException throws exception if sourceURL could not be converted to URL
     */
    public void addSource(String sourceURL) throws MalformedURLException {
        Source newSource = new Source();
        newSource.url = new URL(sourceURL);
        repository.insertSource(newSource);
    }

}
