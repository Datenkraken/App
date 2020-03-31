package de.datenkraken.datenkrake.ui.scroll;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Article;

import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.EventCollector;
import de.datenkraken.datenkrake.surveillance.actions.ApplicationAction;
import de.datenkraken.datenkrake.surveillance.actions.SourceAction;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

import kotlin.Triple;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * Class for displaying the RecyclerView with {@link Article}s from the users feeds.
 * Provides the functionalities to refresh articles and filter articles.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class ScrollFragment extends Fragment {

    @BindView(R.id.cycler)
    RecyclerView recycler;
    @BindView(R.id.swiperefresh_article_list)
    SwipeRefreshLayout swipeRefreshLayout;

    ScrollViewModel scrollModel; // package private to prevent auto generation of accessor method
    private ScrollAdapter scrollAdapter;
    private Bundle recyclerSaveState;

    /**
     * Called on the creation of the options menu. <br>
     * creates a search bar/icon, with which the {@link Article}s can be filtered.
     * This search bar calls {@link ScrollViewModel#searchQuery} to filter the articles.
     *
     * @param menu for the search bar to be displayed in.
     * @param inflater for inflating the layout.
     */
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_bar, menu);
        inflater.inflate(R.menu.main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search_bar);
        menuItem.getIcon().setTint(ContextCompat.getColor(Objects.requireNonNull(getContext()),
            R.color.white));
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setBackgroundResource(R.drawable.search_round);
        EditText editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setTextColor(Color.BLACK);
        ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        icon.setColorFilter(Color.BLACK);

        // Reload Query into SearchView
        String query = scrollModel.searchQuery.getValue();
        if (query != null && !query.isEmpty()) {
            menuItem.expandActionView();
            searchView.setQuery(scrollModel.searchQuery.getValue(), false);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Called, when a query is submitted. Not used.
             *
             * @param query to be filtered by.
             * @return false.
             */
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            /**
             * Called, when the query text changes.
             * Filters the {@link Article}s with {@link ScrollViewModel#searchQuery} and returns true.
             *
             * @param newText query, that the {@link Article}s should be filtered by.
             * @return true.
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                scrollModel.searchQuery.postValue(newText);
                return true;
            }
        });
    }

    /**
     * Called on the creation of the view. <br>
     * Sets the {@link ScrollAdapter} and the swipe refresh layout. <br>
     * Raises the event that the scroll view was opened and collects wifi data. <br>
     * Collects arguments from a bundle, and, if necessary, filters the {@link Article} by
     * {@link de.datenkraken.datenkrake.model.Source} or saved state.
     *
     * @param inflater to inflate the view.
     * @param container the view is contained in.
     * @param savedInstanceState bundle of saved instance sent to the function.
     * @return view created in this function.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_scroll, container, false);

        ButterKnife.bind(this, view);

        // showing toolbar in case user pressed back button in categories view
        if (getActivity() != null) {
            ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (toolbar != null) {
                toolbar.show();
            }
        }


        //  RecyclerView for article preview view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        recycler.setLayoutManager(layoutManager);

        recycler.setAdapter(scrollAdapter);

        Bundle arguments = getArguments();

        // check whether one is being directed form the single source view or just general articles.

        if (arguments != null && arguments.containsKey("source_id")) {
            // id of the source to fetch articles from
            long sourceId = arguments.getLong("source_id");

            scrollModel.stopGettingSourceName(getViewLifecycleOwner());
            scrollModel.getSourceName(sourceId).observe(getViewLifecycleOwner(), name ->
                Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar())
                    .setTitle(name)
            );

            EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.SOURCEIDACTION)
                .with(new Triple<>(
                    SourceAction.FILTERED,
                    sourceId,
                    new WeakReference<>(((DatenkrakeApp) requireActivity().getApplication()).getDatabase()))));

            scrollModel.filterSourceUid.postValue(sourceId);
        } else {
            scrollModel.filterSourceUid.postValue(null);
            scrollModel.stopGettingSourceName(getViewLifecycleOwner());
        }

        if (arguments != null && arguments.containsKey("show_saved")) {
            boolean showSaved = arguments.getBoolean("show_saved");
            scrollModel.filterSavedArticles.postValue(showSaved);
        } else {
            scrollModel.filterSavedArticles.postValue(false);
        }

        // Set listener for swipe to refresh layout
        // Update feeds
        swipeRefreshLayout.setOnRefreshListener(this::refreshArticles);

        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.APPLICATIONACTION)
            .with(ApplicationAction.SCROLL));

        return view;
    }

    /**
     * Called on the creation of the fragment. <br>
     * Initializes a {@link ScrollViewModel} and a {@link ScrollAdapter} and initializes the
     * subscription of the ui. <br>
     * Sets the fragment to have an options menu.
     *
     * @param savedInstanceState bundle of saved instance sent to the function.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag("ScrollFragment");

        scrollModel = new ViewModelProvider(requireActivity()).get(ScrollViewModel.class);

        //  get article data from view model
        scrollAdapter = new ScrollAdapter(scrollModel, requireContext());
        subscribeUi(scrollModel.getArticles());
        setHasOptionsMenu(true);
    }

    /**
     * Sets an observer to a given list of {@link Article}s. <br>
     * On change, call {@link ScrollAdapter#setArticleList(List)} with the changed list.
     *
     * @param liveData containing a list of {@link Article}s to be set.
     */
    private void subscribeUi(LiveData<List<Article>> liveData) {
        liveData.observe(this, articles -> scrollAdapter.setArticleList(articles));
    }

    /**
     * Refreshes the {@link Article}s by calling {@link ScrollViewModel#fetchArticles()}.
     */
    private void refreshArticles() {
        scrollModel.fetchArticles();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Called, when the Fragment is paused. <br>
     * Creates the recyclerSaveState bundle and saves the search query in it
     * from the {@link ScrollViewModel#searchQuery}.
     */
    @Override
    public void onPause() {
        super.onPause();
        recyclerSaveState = new Bundle();

        // Save Query of SearchView
        recyclerSaveState.putString(getString(R.string.search_saved_query), scrollModel.searchQuery.getValue());
    }

    /**
     * Called, when the {@link ScrollFragment} is reopened. <br>
     * Resets the search query of the {@link ScrollViewModel#searchQuery}
     * from the recyclerSaveState bundle.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (recyclerSaveState != null) {
            // Reload Query into SearchView
            String query = recyclerSaveState.getString(getString(R.string.search_saved_query));
            scrollModel.searchQuery.setValue(query);
        }
    }
}
