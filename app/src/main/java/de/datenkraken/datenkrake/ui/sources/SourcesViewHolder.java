package de.datenkraken.datenkrake.ui.sources;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;
import timber.log.Timber;

/**
 * This class holds the view for an element created from a {@link de.datenkraken.datenkrake.model.Source}.
 * This element is displayed in a RecyclerView displayed in the {@link SourcesFragment}.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Jan Klinkmann - jan.klinkmann@stud.tu-darmstadt.de
 */
class SourcesViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.sourceName)
    TextView sourceName;
    @BindView(R.id.sourceURL)
    TextView sourceURL;
    @BindView(R.id.source_icon)
    ImageView icon;

    /**
     * Constructor for the class. <br>
     * Binds ButterKnife to the view and logs a timber message.
     *
     * @param itemView view of a source in the RecyclerView.
     */
    SourcesViewHolder(@NonNull View itemView) {
        super(itemView);
        Timber.tag("SourcesViewHolder");
        ButterKnife.bind(this, itemView);
    }
}


