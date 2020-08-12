package de.datenkraken.datenkrake.ui.recommendation.singlecat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.ui.recommendation.RecommViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * This class is the adapter for showing the {@link Source}s within the source recommendation.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class SingleCategoryAdapter extends RecyclerView.Adapter<SingleCategoryViewHolder> {

    // sources that are in the given category and which the user does not already have
    public List<Source> associatedSources;
    private final RecommViewModel recommModel;
    // colors for marking add button
    private final int HIGHLIGHTED;
    private final int BLUE;

    /**
     * Initializes SingleCategoryAdapter.
     *
     * @param recommModel to be used.
     * @param context     to be used.
     */
    public SingleCategoryAdapter(RecommViewModel recommModel, Context context) {
        Timber.tag("SingleCategoryAdapter");
        this.recommModel = recommModel;
        associatedSources = new ArrayList<>();
        HIGHLIGHTED = ContextCompat.getColor(context, R.color.button_text);
        BLUE = ContextCompat.getColor(context, R.color.text_view);

    }

    /**
     * Called on the creation of the view. Initializes SingleCategoryViewHolder.
     *
     * @param parent   of ViewGroup.
     * @param viewType used in SingleCategoryViewHolder.
     * @return a new SingleCategoryViewHolder.
     */
    @NonNull
    @Override
    public SingleCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.source_recomm_item, parent, false);
        return new SingleCategoryViewHolder(v);
    }

    /**
     * Loads Content into the ViewHolders of the {@link SingleCategoryViewHolder}. <br>
     * Fills the ViewHolders with the name of {@link Source}s and sets a listener
     * for adding a {@link Source}.
     *
     * @param holder   to be filled.
     * @param position of Holder.
     */
    @Override
    public void onBindViewHolder(@NonNull SingleCategoryViewHolder holder, int position) {
        // reuse categories holder but this time with source names
        Source currentSource = associatedSources.get(position);
        holder.source.setText(currentSource.name);
        colorHolder(holder, Objects.requireNonNull(recommModel.sourceStatus.getValue())
            .getOrDefault(currentSource.url.toString(), false));

        holder.itemView.setOnClickListener(v ->
            colorHolder(holder, recommModel.toggleSelection(currentSource.url.toString())));
    }

    private void colorHolder(SingleCategoryViewHolder holder, boolean picked) {
        if (picked) {
            holder.source.setTextColor(HIGHLIGHTED);
            holder.source.setShadowLayer(10, 0, 0, HIGHLIGHTED);
            holder.itemView.setBackgroundResource(R.drawable.category_holder_highlighted);
        } else {
            holder.source.setTextColor(BLUE);
            holder.source.setShadowLayer(0, 0, 0, BLUE);
            holder.itemView.setBackgroundResource(R.drawable.category_holder);
        }
    }

    /**
     * Gets the Size of the variable associatedSources.
     *
     * @return size of associatedSources as int.
     */
    @Override
    public int getItemCount() {
        return associatedSources == null ? 0 : associatedSources.size();
    }
}
