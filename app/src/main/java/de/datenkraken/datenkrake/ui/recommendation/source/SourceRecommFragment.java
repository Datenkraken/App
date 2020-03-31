package de.datenkraken.datenkrake.ui.recommendation.source;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.ui.recommendation.RecommViewModel;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;

import timber.log.Timber;


/**
 * Class for displaying {@link de.datenkraken.datenkrake.GetCategoriesQuery.Category}s
 * with their associated {@link de.datenkraken.datenkrake.GetCategoriesQuery.RssSource}s fetched from server.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class SourceRecommFragment extends Fragment {

    private RecommViewModel recommModel;
    private Context context;

    @BindView(R.id.category_recycler)
    RecyclerView recycler;
    @BindView(R.id.submit_sources)
    Button submit;
    @BindView(R.id.categories_back)
    Button categoriesBack;
    @BindView(R.id.sources_cancel)
    Button cancel;


    /**
     * Called on the creation of the view. <br>
     * Sets the {@link SourceRecommAdapter} of the recyclerview and sets the LayoutManager. <br>
     * Sets the listeners for submitting {@Source}s, skipping and going back to the
     * {@link de.datenkraken.datenkrake.ui.recommendation.category.CategoryRecommFragment}. <br>
     * Hides the toolbar.
     *
     * @param inflater           to inflate the view.
     * @param container          the view is contained in.
     * @param savedInstanceState bundle of saved instance sent to the function.
     * @return view created in this function.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Timber.tag("RecommendationFragement");

        View view = inflater.inflate(R.layout.source_recomm_fragment, container, false);
        ButterKnife.bind(this, view);

        // hiding the toolbar
        if (getActivity() != null) {
            ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (toolbar != null) {
                toolbar.hide();
            }
        }

        // get already saved sources from DB in order to find out what not to display again for recommendaiton
        recommModel.getSavedSources().observe(getViewLifecycleOwner(), recommModel::setSources);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);

        //  initialize the adapter
        SourceRecommAdapter recomadapter = new SourceRecommAdapter(recommModel, getContext());
        recommModel.fetchCategorySources().observe(getViewLifecycleOwner(), recomadapter::setSelectedCategories);

        recycler.setAdapter(recomadapter);

        // listener for submitting the selected sources
        submit.setOnClickListener(v -> {
            // overwrite old selected categories on onCreate
            recommModel.selectedCategories = new HashMap<>();
            NavController controller = Navigation.findNavController(v);
            controller.navigate(R.id.nav_scroll);
            // only add sources if any have been added
            if (recommModel.storedSources.size() != 0) {
                try {
                    recommModel.addSources();
                    Toast.makeText(context, getString(R.string.toast_sources_successfully_added),
                        Toast.LENGTH_SHORT).show();

                } catch (MalformedURLException e) {
                    Toast.makeText(context, getString(R.string.toast_sources__added_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // listener for going back to categories
        categoriesBack.setOnClickListener(v -> {
            NavController controller = Navigation.findNavController(v);
            controller.navigate(R.id.nav_cat_recomm);
        });

        // listener for canceling and going to scroll view
        cancel.setOnClickListener(v -> {
            // overwrite old selected categories on onCreate
            recommModel.selectedCategories = new HashMap<>();
            NavController controller = Navigation.findNavController(v);
            controller.navigate(R.id.nav_scroll);
        });

        return view;
    }

    /**
     * Called on the creation of the fragment. <br>
     * Gets a shared {@link RecommViewModel} resets storedSources. <br>
     *
     * @param savedInstanceState bundle of saved instance sent to the function.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag("RecommendationFr");


        context = requireContext();
        recommModel = new ViewModelProvider(requireActivity(),
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
            .get(RecommViewModel.class);

        recommModel.storedSources = new HashSet<>();

    }


}
