package de.datenkraken.datenkrake.ui.recommendation;

import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.GetCategoriesOnlyQuery;
import de.datenkraken.datenkrake.GetCategoriesQuery;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloQuery;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


/**
 * Class used to fetching a list of {@link GetCategoriesOnlyQuery.Category} first to retrieve the names.<br>
 * It uses an instance of {@link CategoryFetcher} to fetch the entire {@link GetCategoriesQuery.Category}s
 * and {@link GetCategoriesQuery.RssSource}s list after the category
 * fetching has been successful. <br>
 * It then provides this list to the RecommViewModel. This is done to achieve faster
 * access speed.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class CategoryOnlyFetcher extends ApolloQuery<GetCategoriesOnlyQuery.Data> {

    // MutableLiveDate list to store categories
    private final MutableLiveData<List<GetCategoriesOnlyQuery.Category>> categories = new MutableLiveData<>();
    private final CategoryFetcher fetcher = new CategoryFetcher();


    /**
     * Returns the Query used to request {@link GetCategoriesOnlyQuery.Category}s.
     *
     * @return Query of type {@link GetCategoriesOnlyQuery}.
     */
    @Override
    public Query getQuery() {
        return GetCategoriesOnlyQuery.builder().build();
    }


    /**
     * Posts received {@link GetCategoriesOnlyQuery.Category}s in the response to {@link #categories}.
     *
     * @param response {@link Response} of type {@link GetCategoriesOnlyQuery.Data} from the server.
     */
    @Override
    public void onResponse(@NotNull Response<GetCategoriesOnlyQuery.Data> response) {
        if (response.data() != null) {
            categories.postValue(response.data().categories());
            fetcher.request();

        }
    }

    /**
     * Gets called if there was a problem fetching the data.
     *
     * @param e ApolloException.
     */
    @Override
    public void onFailure(@NotNull ApolloException e) {
        Timber.e(e, "failed to retrieve Categories");
    }

    /**
     * Used to fetching {@link GetCategoriesQuery} with their associated
     * {@link GetCategoriesQuery.RssSource}s from the server.
     * It retrieves the list of all {@link GetCategoriesQuery.Category}s and will return
     * {@link CategoryFetcher#categories}.
     *
     * @return MutableLiveData object containing a list of {@link GetCategoriesQuery.Category}
     */
    public MutableLiveData<List<GetCategoriesQuery.Category>> getCategories() {
        return fetcher.categories;
    }

    /**
     * Used to fetching a list {@link GetCategoriesOnlyQuery.Category}, with their names from the server.
     * It retrieves the list of all categories and will return a LiveData object to the ViewModel.
     *
     * @return MutableLiveData object containing a list of {@link GetCategoriesOnlyQuery.Category}
     */
    public MutableLiveData<List<GetCategoriesOnlyQuery.Category>> getCategoryNames() {
        return categories;
    }
}


