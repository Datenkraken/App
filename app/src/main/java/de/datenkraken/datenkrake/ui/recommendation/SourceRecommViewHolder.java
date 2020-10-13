package de.datenkraken.datenkrake.ui.recommendation;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;
import timber.log.Timber;

/**
 * Class contains the ViewHolder for showing {@link de.datenkraken.datenkrake.GetCategoriesQuery.Category} to user
 * which he can chose {@link de.datenkraken.datenkrake.GetCategoriesQuery.RssSource}s from. <br>
 * It also holds a RecyclerView with sources associated with a category that can be hidden and replaced
 * by a TextView if no sources that have not been added already are present.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class SourceRecommViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.category_tag)
    TextView category;
    @BindView(R.id.sources_recycler)
    RecyclerView cycler;
    @BindView(R.id.no_new_sources)
    TextView noNewSources;

    /**
     * Binds ButterKnife to the view.
     *
     * @param itemView view of a {@link de.datenkraken.datenkrake.GetCategoriesQuery.Category}
     and its respective {@link de.datenkraken.datenkrake.GetCategoriesQuery.RssSource}s
     in the recyclerview.
     */
    SourceRecommViewHolder(@NonNull View itemView) {
        super(itemView);
        Timber.tag("SourceRecommViewHolder");
        ButterKnife.bind(this, itemView);

    }

}
