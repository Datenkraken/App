package de.datenkraken.datenkrake.ui.recommendation;

import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.GetCategoriesQuery;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloQuery;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


/**
 * Class used for fetching {@link GetCategoriesQuery.Category}s with their associated
 * {@link GetCategoriesQuery.RssSource}s from the server. <br>
 * It retrieves the list of {@link GetCategoriesQuery}s and will return a LiveData object to the RecommViewModel.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class CategoryFetcher extends ApolloQuery<GetCategoriesQuery.Data> {

    // MutableLiveDate list to store categories
    public final MutableLiveData<List<GetCategoriesQuery.Category>> categories = new MutableLiveData<>();


    /**
     * Returns the Query used to request {@link GetCategoriesQuery.Category}s.
     *
     * @return Query of type {@link GetCategoriesQuery}.
     */
    @Override
    public Query getQuery() {
        return GetCategoriesQuery.builder().build();
    }

    /**
     * Posts received {@link GetCategoriesQuery.Category}s in the response to {@link #categories}.
     *
     * @param response {@link Response} of type {@link GetCategoriesQuery.Data} from the server.
     */
    @Override
    public void onResponse(@NotNull Response<GetCategoriesQuery.Data> response) {
        if (response.data() != null) {
            categories.postValue(response.data().categories());

        }
    }

    /**
     * Gets called if there was a problem fetching the data.
     *
     * @param  e ApolloException.
     */
    @Override
    public void onFailure(@NotNull ApolloException e) {
        Timber.e(e, "failed to retrieve Categories");
    }

}

