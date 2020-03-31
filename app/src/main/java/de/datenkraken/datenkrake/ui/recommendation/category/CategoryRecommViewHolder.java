package de.datenkraken.datenkrake.ui.recommendation.category;

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
 * Class contains the ViewHolder for showing a {@link de.datenkraken.datenkrake.GetCategoriesOnlyQuery.Category}
 * in the category recommendation view.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class CategoryRecommViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.category_name)
    TextView category;

    /**
     * Binds ButterKnife to the view.
     *
     * @param itemView of an {@link de.datenkraken.datenkrake.GetCategoriesOnlyQuery.Category} in the RecyclerView.
     */
    CategoryRecommViewHolder(@NonNull View itemView) {
        super(itemView);
        Timber.tag("CategoryRecomm");
        ButterKnife.bind(this, itemView);
        ViewGroup.LayoutParams lp = itemView.getLayoutParams();
        if (lp instanceof FlexboxLayoutManager.LayoutParams) {
            ((FlexboxLayoutManager.LayoutParams) lp).setFlexGrow(1);
        }
    }

}
