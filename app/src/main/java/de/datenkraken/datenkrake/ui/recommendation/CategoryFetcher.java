package de.datenkraken.datenkrake.ui.recommendation;

import androidx.lifecycle.MutableLiveData;

import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.GetCategoriesQuery;
import de.datenkraken.datenkrake.model.Category;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloQuery;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    public final MutableLiveData<List<Category>> categories = new MutableLiveData<>();


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
        if (response.data() == null) {
            return;
        }
        List<GetCategoriesQuery.Category> apolloCategories = response.data().categories();

        if (apolloCategories == null) {
            return;
        }

        List<Category> categories = new ArrayList<>();
        for (GetCategoriesQuery.Category apolloCategory : apolloCategories) {
            categories.add(parseCategory(apolloCategory));
        }
        this.categories.postValue(categories);
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

    private Category parseCategory(GetCategoriesQuery.Category apolloCategory) {
        Category category = new Category();
        category.name = apolloCategory.name();
        category.sources = new ArrayList<>();

        if (apolloCategory.rssSources() == null) {
            return category;
        }

        Source source;
        for (GetCategoriesQuery.RssSource rssSource : apolloCategory.rssSources()) {
            source = new Source();
            source.name = rssSource.name();
            try {
                source.url = new URL(rssSource.url());
                category.sources.add(source);
            } catch (MalformedURLException e) {
                Timber.e("Source %s has a malformed url!", source.name);
            }
        }
        return category;
    }
}

