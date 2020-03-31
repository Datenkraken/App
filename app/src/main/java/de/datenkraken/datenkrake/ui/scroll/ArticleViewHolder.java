package de.datenkraken.datenkrake.ui.scroll;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;

/**
 * Class containing the ViewHolder for the {@link ScrollFragment} RecyclerView which will get populated in
 * onBindViewHolder in the {@link ScrollAdapter}.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class ArticleViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.article_information)
    TextView articleInformation;
    @BindView(R.id.scroll_bookmark)
    ImageButton bookmark;
    @BindView(R.id.article_view_image_scroll)
    ImageView image;

    /**
     * Creates a ViewHolder.
     * Binds ButterKnife to the view.
     *
     * @param view view of an {@link de.datenkraken.datenkrake.model.Article} in the RecyclerView.
     */
    ArticleViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }
}
