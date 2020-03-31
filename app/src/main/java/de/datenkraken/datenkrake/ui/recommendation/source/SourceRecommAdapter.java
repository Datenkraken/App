package de.datenkraken.datenkrake.ui.recommendation.source;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import de.datenkraken.datenkrake.GetCategoriesQuery;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.ui.recommendation.RecommViewModel;
import de.datenkraken.datenkrake.ui.recommendation.source.singlecat.SingleCategoryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;


/**
 * This class is the adapter for the source recommendation recyclerview.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class SourceRecommAdapter extends RecyclerView.Adapter<SourceRecommViewHolder> {

    private List<GetCategoriesQuery.Category> selectedCategories;
    private final RecommViewModel recommModel;
    private final Context context;

    /**
     * Initializes SourceRecommAdapter and sets recommModel. <br>
     *
     * @param recommModel {@link RecommViewModel}
     * @param context     Context of fragment
     */
    SourceRecommAdapter(RecommViewModel recommModel, Context context) {
        Timber.tag("SourceRecommAdapter");
        this.recommModel = recommModel;
        this.context = context;
    }

    /**
     * Called on the creation of the view. Initializes SourceRecommViewHolder.
     *
     * @param parent   of ViewGroup.
     * @param viewType used in SourceRecommViewHolder.
     * @return a new SourceRecommViewHolder.
     */
    @NonNull
    @Override
    public SourceRecommViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new SourceRecommViewHolder(v);
    }

    /**
     * Loads Content into the ViewHolders of the {@link SourceRecommFragment}. <br>
     * Fills the ViewHolders with the name of {@link GetCategoriesQuery.Category}s and initializes
     * the {@link SingleCategoryAdapter} for showing sources of a particular {@link GetCategoriesQuery.Category}.
     *
     * @param holder   to be filled.
     * @param position of Holder.
     */
    @Override
    public void onBindViewHolder(@NonNull SourceRecommViewHolder holder, int position) {

        GetCategoriesQuery.Category currentCategory = selectedCategories.get(position);
        holder.category.setText(currentCategory.name());


        //  initialize the recycler adapter
        SingleCategoryAdapter singlesourceAdapter = new SingleCategoryAdapter(recommModel, context);

        // in the nested view only the sources that the user does not already have in the DB should be shown
        // therefore only non existing sources will be added to the adapters list

        for (GetCategoriesQuery.RssSource c : Objects.requireNonNull(currentCategory.rssSources())) {

            // check if source does already exist in DB

            if (!recommModel.existingSources.contains(c.url())) {
                // adding source to singleSourceAdapters list
                singlesourceAdapter.associatedSources.add(c);
            }
        }

        // if there is no new sources associated with this category
        if (singlesourceAdapter.associatedSources.size() != 0) {

            // init flexbox layout manager
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setFlexWrap(FlexWrap.WRAP);
            holder.cycler.setLayoutManager(layoutManager);
            holder.cycler.setAdapter(singlesourceAdapter);
        } else {
            // hide recyclerview because there are no new sources for the user
            // in this category and show text field to communicate this
            holder.cycler.setVisibility(View.GONE);
            holder.noNewSources.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Sets the selected {@link GetCategoriesQuery.Category}s.
     *
     * @param categories List of type {@link GetCategoriesQuery.Category}
     */
    void setSelectedCategories(List<GetCategoriesQuery.Category> categories) {
        List<GetCategoriesQuery.Category> list = new ArrayList<>();
        for (GetCategoriesQuery.Category category : categories) {
            if (recommModel.selectedCategories.containsKey(category.name())) {
                list.add(category);
            }
        }
        this.selectedCategories = list;
    }

    /**
     * Gets the Size of the variable selectedCategories.
     *
     * @return size of categories as int.
     */
    @Override
    public int getItemCount() {
        return selectedCategories == null ? 0 : selectedCategories.size();
    }
}
