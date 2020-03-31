package de.datenkraken.datenkrake.ui.recommendation.category;

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
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.ui.recommendation.RecommViewModel;

import java.util.HashMap;
import java.util.HashSet;

import timber.log.Timber;


/**
 * Fragment displaying {@link de.datenkraken.datenkrake.GetCategoriesOnlyQuery.Category}s to
 * chose from in a scroll view.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class CategoryRecommFragment extends Fragment {

    @BindView(R.id.pick_sources)
    Button pickSources;
    @BindView(R.id.cat_cancel)
    Button cancel;
    @BindView(R.id.grid_recycler)
    RecyclerView cycler;
    private RecommViewModel recommModel;
    private CategoryRecommAdapter recomadapter;


    /**
     * Called on the creation of the view. <br>
     * Sets the {@link CategoryRecommAdapter} of the recyclerview and sets the LayoutManager. <br>
     * Sets the listeners for skipping and routing to the Source Recommendation View. <br>
     * Sets LiveData Observer for {@link de.datenkraken.datenkrake.GetCategoriesOnlyQuery.Category} and
     * {@link de.datenkraken.datenkrake.GetCategoriesQuery.RssSource}s. <br>
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

        Timber.tag("CategoryRecommFragment");

        View view = inflater.inflate(R.layout.category_recomm_fragment, container, false);

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

        // resetting existing sources because they might have changed if user deleted a source
        recommModel.existingSources = new HashSet<>();


        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        cycler.setLayoutManager(layoutManager);

        cycler.setAdapter(recomadapter);

        // fetch categories list form server, the setCategories in CategoryRecommAdapter is the observer
        recommModel.fetchCategories().observe(getViewLifecycleOwner(), recomadapter::setCategories);

        pickSources.setOnClickListener(v -> {
            if (recommModel.selectedCategories.size() == 0) {
                Toast.makeText(getContext(), getString(R.string.toast_no_cat_selected), Toast.LENGTH_SHORT).show();
            } else {
                NavController controller = Navigation.findNavController(v);
                controller.navigate(R.id.nav_recomm);
            }
        });

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
     * Initializes a {@link RecommViewModel} and a {@link CategoryRecommAdapter} and initializes the
     * subscription of the ui. <br>
     * Sets the selectedCategories of the model if it has not been set before.
     *
     * @param savedInstanceState bundle of saved instance sent to the function.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag("CategoryRecommFragment");

        recommModel = new ViewModelProvider(requireActivity(),
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
            .get(RecommViewModel.class);

        // if selectedCategories has not been instanciated before
        if (recommModel.selectedCategories == null) {
            recommModel.selectedCategories = new HashMap<>();
        }
        recomadapter = new CategoryRecommAdapter(recommModel, getContext());

    }


}

