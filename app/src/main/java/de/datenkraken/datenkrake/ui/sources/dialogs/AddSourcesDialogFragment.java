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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.ui.sources.SourcesViewModel;

import java.net.MalformedURLException;
import java.util.Objects;

/**
 * Dialog Popup Fragment for adding {@link de.datenkraken.datenkrake.model.Source}s.
 * Using a custom dialog.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class AddSourcesDialogFragment extends DialogFragment {

    @BindView(R.id.add_source_accept_button)
    Button acceptButton;
    @BindView(R.id.add_source_cancel_button)
    Button cancelButton;
    @BindView(R.id.add_source_edittext)
    EditText input;
    private final SourcesViewModel sourceModel;

    /**
     * Constructor for AddSourcesFragment, initializing it.
     *
     * @param sourceModel {@link SourcesViewModel} to be used to save the
     * {@link de.datenkraken.datenkrake.model.Source}s entered.
     */
    public AddSourcesDialogFragment(SourcesViewModel sourceModel) {
        this.sourceModel = sourceModel;
    }

    /**
     * Called on the creation of the dialog. <br>
     * Inflates the view and sets on click listeners to the buttons.
     *
     * @param savedInstanceState Bundle of saved instance sent to this function.
     * @return the dialog that was build in this function.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_source_dialog, null, false);
        ButterKnife.bind(this, view);

        // Set Builder and Buttons
        acceptButton.setOnClickListener(v -> {
            try {
                sourceModel.addSource(input.getText().toString());
                input.setText("");
                dismiss();
            } catch (MalformedURLException e) {
                Toast.makeText(getContext(), getText(R.string.source_url_wrong_format),
                    Toast.LENGTH_LONG).show();
            }
        });
        cancelButton.setOnClickListener(v -> dismiss());
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
}
