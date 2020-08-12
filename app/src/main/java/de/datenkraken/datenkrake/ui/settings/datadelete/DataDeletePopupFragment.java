package de.datenkraken.datenkrake.ui.settings.datadelete;

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
import de.datenkraken.datenkrake.ui.settings.SettingsPageViewModel;
import jp.wasabeef.blurry.Blurry;

import java.util.Objects;

/**
 * Popup that sends a request to delete the userdata and account when accept is pressed.
 * Before this, displays a message signaling, what is about to happen, when accept is pressed.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class DataDeletePopupFragment extends DialogFragment {

    @BindView(R.id.data_delete_popup_text) TextView dataDelete;
    @BindView(R.id.data_delete_accept_button) Button acceptButton;
    @BindView(R.id.data_delete_cancel_button) Button cancelButton;

    private final SettingsPageViewModel settingsPageViewModel;
    private final ViewGroup root;

    /**
     * Constructor for the DataDeletePopupFragment, initializing it.
     *
     * @param settingsPageViewModel {@link SettingsPageViewModel} to be called to delete the data.
     */
    public DataDeletePopupFragment(SettingsPageViewModel settingsPageViewModel, ViewGroup root) {
        this.settingsPageViewModel = settingsPageViewModel;
        this.root = root;
    }

    /**
     * Called upon creation of view. Sets background transparent for rounded edges.
     *
     * @param inflater for layout.
     * @param container of view group.
     * @param savedInstanceState bundle of saved instance sent to this function.
     * @return view of fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Get displayed window.
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        // Set background to transparent to allow rounded edges.
        Objects.requireNonNull(window).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Called upon dialog creation. Sets view, text and on click listener for the buttons of the fragment.
     * When the acceptButton is pressed, calls deleteData in {@link SettingsPageViewModel}.
     *
     * @param savedInstanceState bundle of saved instance sent to this function.
     * @return dialog to be displayed.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view =
            inflater.inflate(R.layout.data_delete_popup_fragment, null, false);

        ButterKnife.bind(this, view);

        root.post(() -> Blurry.with(view.getContext()).radius(10).sampling(1).animate(500).async().onto(root));

        // Set DialogFragment Text
        dataDelete.setText(R.string.data_delete_warning_text);

        // Set Buttons.
        acceptButton.setOnClickListener(v -> {
            settingsPageViewModel.deleteData();
            dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            Blurry.delete(root);
            dismiss();
        });
        // Set Builder and Buttons
        return builder.setView(view).show();
    }

    @Override
    public void onDestroyView() {
        Blurry.delete(root);
        super.onDestroyView();
    }
}
