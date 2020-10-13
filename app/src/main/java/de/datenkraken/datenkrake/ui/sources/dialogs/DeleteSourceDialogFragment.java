package de.datenkraken.datenkrake.ui.sources.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.ui.sources.SourcesViewModel;

import java.util.Objects;

import jp.wasabeef.blurry.Blurry;

/**
 * Dialog Fragment used for deleting {@link Source}s. <br>
 * Uses a custom dialog fragment.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class DeleteSourceDialogFragment extends DialogFragment {

    @BindView(R.id.delete_accept_button)
    Button acceptButton;
    @BindView(R.id.delete_cancel_button)
    Button cancelButton;
    @BindView(R.id.delete_source_title)
    TextView title;

    private final Source source;
    private final SourcesViewModel sourcesViewModel;
    private final ViewGroup root;

    /**
     * Constructor of the class, initializing it.
     *
     * @param source {@link Source} to possibly be deleted.
     * @param sourcesViewModel {@link SourcesViewModel} to delete the source.
     * @param root root view, which gets blurred
     */
    public DeleteSourceDialogFragment(Source source, SourcesViewModel sourcesViewModel, ViewGroup root) {
        this.source = source;
        this.sourcesViewModel = sourcesViewModel;
        this.root = root;
    }

    /**
     * Called on the creation of the dialog fragment. <br>
     * Inflates the view, sets a text for the deletion of the {@link Source} and sets on click listeners
     * to the buttons.
     *
     * @param savedInstanceState Bundle of saved instance sent to this function.
     * @return the dialog that was build in this function.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_delete_source_dialog, null, false);
        ButterKnife.bind(this, view);

        root.post(() -> Blurry.with(view.getContext()).radius(10).sampling(1).animate(500).async().onto(root));

        String titleText =
            requireContext().getString(R.string.source_delete_before)
            + source.name
            + requireContext().getString(R.string.source_delete_after);
        title.setText(titleText);
        // Set Builder and Buttons
        acceptButton.setOnClickListener(v -> {
            sourcesViewModel.deleteSource(source);
            dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            Blurry.delete(root);
            dismiss();
        });
        return builder.setView(view).show();
    }

    /**
     * Called on the creation of the view. <br>
     * Sets the background drawable transparent to allow round edges.
     *
     * @param inflater to inflate the layout.
     * @param container of the view group.
     * @param savedInstanceState Bundle of saved instance sent to this function.
     * @return view of the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        Objects.requireNonNull(window).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Blurry.delete(root);
        super.onDestroyView();
    }
}
