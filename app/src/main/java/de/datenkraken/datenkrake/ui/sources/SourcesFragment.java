package de.datenkraken.datenkrake.ui.sources;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.datenkraken.datenkrake.R;

import de.datenkraken.datenkrake.ui.sources.dialogs.AddSourcesDialogFragment;
import timber.log.Timber;



/**
 * This Fragment is used for interaction with the sources view
 * where a user can add new {@link de.datenkraken.datenkrake.model.Source}s.
 * The sources are being displayed as a list in a recyclerview.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Jan Klinkmann - jan.klinkmann@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class SourcesFragment extends Fragment {

    @BindView(R.id.sourceHolder)
    RecyclerView recycler;
    @BindView(R.id.addSource)
    FloatingActionButton buttonNewSource;

    private SourcesViewModel sourceModel;

    /**
     * Called on the creation of the view. <br>
     * Inflates the view and initializes a {@link SourcesViewModel} and {@link SourcesAdapter}. <br>
     * Sets on click listeners calling the {@link AddSourcesDialogFragment}.
     *
     * @param inflater used to inflate the layout.
     * @param container of the view group.
     * @param savedInstanceState bundle of saved instance sent to the function.
     * @return the inflated view.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Timber.tag("SourcesFragment");

        View view = inflater.inflate(R.layout.fragment_sources, container, false);
        ButterKnife.bind(this, view);

        Context context = requireContext();

        //  recycler view for source edit view
        recycler.addItemDecoration(new DividerItemDecoration(
            context,
            DividerItemDecoration.HORIZONTAL
        ));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);

        //  initialize the recycler adapter
        SourcesAdapter sourcesAdapter = new SourcesAdapter(sourceModel, getContext(), requireActivity());
        sourceModel.getSavedSources().observe(getViewLifecycleOwner(), sourcesAdapter::setSources);
        recycler.setAdapter(sourcesAdapter);



        buttonNewSource.setOnClickListener(v -> {
            AddSourcesDialogFragment addFragment =
                new AddSourcesDialogFragment(
                    sourceModel,
                    (ViewGroup) requireView().getRootView());

            addFragment.show(getChildFragmentManager(), "addSource");
        });

        return view;
    }

    /**
     * Called on creation of the fragment. <br>
     * Initializing {@link #sourceModel}.
     *
     * @param savedInstanceState bundle of saved instance sent to function.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sourceModel = new ViewModelProvider(this,
            new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
            .get(SourcesViewModel.class);
    }
}
