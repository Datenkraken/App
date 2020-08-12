package de.datenkraken.datenkrake.ui.sources;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.ui.sources.dialogs.DeleteSourceDialogFragment;

import java.util.List;

import timber.log.Timber;


/**
 * This class creates the View Holders and populates them with the {@link Source}s a user has already added.
 * The ViewHolders are used in the RecyclerView in the {@link SourcesFragment}.
 *
 * @author Simon Schmalfuß; - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Jan Klinkmann - jan.klinkmann@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class SourcesAdapter extends RecyclerView.Adapter<SourcesViewHolder> {

    private final SourcesViewModel sourcesViewModel;
    private final Context context;
    private final FragmentActivity activity;
    protected List<Source> sources;

    /**
     * Constructor for this class.
     *  @param sourcesViewModel {@link SourcesViewModel}, needed to delete {@link Source}s.
     * @param context used for the {@link DeleteSourceDialogFragment}.
     * @param activity
     */
    SourcesAdapter(SourcesViewModel sourcesViewModel, Context context, FragmentActivity activity) {
        Timber.tag("SourceAdapter");
        this.sourcesViewModel = sourcesViewModel;
        this.context = context;
        this.activity = activity;
    }

    /**
     * Updates the List of {@link Source}s with a new list of {@link Source}s. <br>
     * Calculates the difference between the old list and the new list of sources.
     * Notifies the recycler view for changes.
     *
     * @param sourceList List of {@link Source}s.
     */
    public void setSources(final List<Source> sourceList) {
        if (sources == null) {
            sources = sourceList;
            notifyItemRangeInserted(0, sources.size());
            return;
        }

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            /**
             * Gets the size of the old {@link Source}s.
             *
             * @return size of sources as integer.
             */
            @Override
            public int getOldListSize() {
                return sources.size();
            }

            /**
             * Gets the size of the new {@link Source}s.
             *
             * @return size of sourceList as integer.
             */
            @Override
            public int getNewListSize() {
                return sourceList.size();
            }

            /**
             * Checks, if two uids of {@link Source}s in two different positions in the lists are the same.
             *
             * @param oldItemPosition position of the item in the old list.
             * @param newItemPosition position of the item in the new list.
             * @return boolean, that is true, if {@link Source}s have the same uids.
             */
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return sources.get(oldItemPosition).uid == sourceList.get(newItemPosition).uid;
            }

            /**
             * Checks if the contents of two {@link Source}s in two different positions
             * in the two lists are the same.
             *
             * @param oldItemPosition position of the item in the old list.
             * @param newItemPosition position of the item in the new list.
             * @return boolean, that is true, if items are equal.
             */
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return sources.get(oldItemPosition).equals(sourceList.get(newItemPosition));
            }
        });
        sources = sourceList;
        result.dispatchUpdatesTo(this);
    }

    /**
     * Called on the creation of the view holder. <br>
     * Inflates the layout and creates a new {@link SourcesViewHolder}.
     *
     * @param parent of the view.
     * @param viewType that is used on the view.
     * @return {@link SourcesViewHolder} that was created.
     */
    @NonNull
    @Override
    public SourcesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.source_item, parent, false);
        return new SourcesViewHolder(v);
    }

    /**
     * Called on the binding of the view holder. <br>
     * Sets the {@link Source} name, url and image to a single view holder in a specified position.
     * Also sets an on click listener calling {@link DeleteSourceDialogFragment} to ask the user,
     * if he wants to delete the source on the view holder.
     *
     * @param holder {@link SourcesViewHolder} to be used.
     * @param position position of the view holder.
     */
    @Override
    public void onBindViewHolder(@NonNull SourcesViewHolder holder, int position) {
        // Get the source that needs to be loaded into the view holder.
        Source source = sources.get(position);

        if (source.name != null) {
            holder.sourceName.setText(source.name.trim());
        } else {
            holder.sourceName.setText(R.string.source_name_loading);
        }

        holder.sourceURL.setText(source.url.toString());

        // load the image with rounded corners using glide
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new RoundedCorners(10));
        Glide.with(holder.icon)
            .load(source.getIcon())
            .placeholder(R.drawable.ic_loading_icon)
            .apply(requestOptions)
            .into(holder.icon);

        DeleteSourceDialogFragment deleteSource =
            new DeleteSourceDialogFragment(source,
                sourcesViewModel,
                (ViewGroup) activity.findViewById(R.id.source_root).getRootView());

        holder.itemView.setOnClickListener(v ->
            deleteSource.show(((AppCompatActivity) context).getSupportFragmentManager(),
            "deleteSource"));

    }

    /**
     * Gets the size of the sources list.
     *
     * @return size of sources as integer.
     */
    @Override
    public int getItemCount() {
        return sources == null ? 0 : sources.size();
    }
}
