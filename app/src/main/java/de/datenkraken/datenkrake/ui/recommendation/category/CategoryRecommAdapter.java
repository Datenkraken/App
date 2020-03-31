package de.datenkraken.datenkrake.ui.recommendation.category;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import de.datenkraken.datenkrake.GetCategoriesOnlyQuery;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.ui.recommendation.RecommViewModel;

import java.util.List;

import timber.log.Timber;

/**
 * This class creates the View Holders and populates them with {@link GetCategoriesOnlyQuery.Category} information.
 * It also enables the user to pick select {@link GetCategoriesOnlyQuery.Category}s who's source he wants to add.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class CategoryRecommAdapter extends RecyclerView.Adapter<CategoryRecommViewHolder> {

    private List<GetCategoriesOnlyQuery.Category> categories;
    private final RecommViewModel recommModel;
    // colors for marking add button
    private final int HIGHLIGHTED;
    private final int BLUE;

    /**
     * Initializes CategoryRecommAdapter and set recommModel. <br>
     * Sets colors for showing whether {@link GetCategoriesOnlyQuery.Category} has been selected.
     *
     * @param recommModel to be used.
     * @param context     to be used.
     */
    CategoryRecommAdapter(RecommViewModel recommModel, Context context) {
        Timber.tag("SourceRecommAdapter");
        this.recommModel = recommModel;
        HIGHLIGHTED = ContextCompat.getColor(context, R.color.highlightedBlue);
        BLUE = ContextCompat.getColor(context, R.color.colorPrimaryLight);
    }

    /**
     * Called on the creation of the view. Initializes CategoryRecommViewHolder.
     *
     * @param parent   of ViewGroup.
     * @param viewType used in ArticleViewHolder.
     * @return a new CategoryRecommViewHolder.
     */
    @NonNull
    @Override
    public CategoryRecommViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_grid_item, parent, false);
        return new CategoryRecommViewHolder(v);
    }

    /**
     * Loads Content into the ViewHolders of the {@link CategoryRecommFragment}. <br>
     * Fills the ViewHolders with the name of {@link GetCategoriesOnlyQuery.Category} and sets Click Listeners.
     * It also greys out picked {@link GetCategoriesOnlyQuery.Category}s.
     *
     * @param holder   to be filled.
     * @param position of Holder.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryRecommViewHolder holder, int position) {
        GetCategoriesOnlyQuery.Category category = categories.get(position);
        holder.category.setText(category.name());
        if (recommModel.selectedCategories.containsKey(category.name())) {
            holder.category.getBackground().setColorFilter(HIGHLIGHTED, PorterDuff.Mode.SRC_IN);
        } else {
            holder.category.getBackground().setColorFilter(BLUE, PorterDuff.Mode.SRC_IN);
        }

        holder.itemView.setOnClickListener(v -> {
            if (recommModel.selectedCategories.containsKey(category.name())) {
                holder.category.getBackground().setColorFilter(BLUE, PorterDuff.Mode.SRC_IN);
                recommModel.selectedCategories.remove(category.name());
            } else {
                recommModel.selectedCategories.put(category.name(), category);
                holder.category.getBackground().setColorFilter(HIGHLIGHTED, PorterDuff.Mode.SRC_IN);
            }

        });

    }

    /**
     * Sets list of {@link GetCategoriesOnlyQuery.Category}s to be displayed in RecyclerView.
     *
     * @param categories list of type {@link GetCategoriesOnlyQuery.Category}.
     */
    void setCategories(List<GetCategoriesOnlyQuery.Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    /**
     * Gets the Size of the variable categories.
     *
     * @return size of categories as int.
     */
    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }
}
