package de.datenkraken.datenkrake.ui.recommendation.source.singlecat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.google.android.flexbox.FlexboxLayoutManager;

import de.datenkraken.datenkrake.R;
import timber.log.Timber;

/**
 * Class contains the ViewHolder for showing a single
 * {@link de.datenkraken.datenkrake.GetCategoriesQuery.RssSource} field within the recyclerview.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class SingleCategoryViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.source_n)
    TextView source;

    /**
     * Binds ButterKnife to the view.
     *
     * @param itemView of an {@link de.datenkraken.datenkrake.GetCategoriesQuery.RssSource} in the RecyclerView.
     */
    SingleCategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        Timber.tag("SingleCategoryVHolder");
        ButterKnife.bind(this, itemView);
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp instanceof FlexboxLayoutManager.LayoutParams) {
            ((FlexboxLayoutManager.LayoutParams) lp).setFlexGrow(1);
        }
    }

}
