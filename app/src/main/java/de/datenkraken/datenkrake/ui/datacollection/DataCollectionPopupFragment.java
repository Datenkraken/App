package de.datenkraken.datenkrake.ui.datacollection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.datenkraken.datenkrake.R;

import java.util.Objects;

/**
 * DialogFragment asking for data collection permission. <br>
 * User can accept or decline. If he declines, he can not use the app.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class DataCollectionPopupFragment extends DialogFragment {

    @BindView(R.id.data_collection_popup_text)
    TextView dataCollectionText;
    @BindView(R.id.data_collection_accept_button)
    Button acceptButton;
    @BindView(R.id.data_collection_browser_button)
    Button browserButton;

    private final DataCollectionPopupViewModel dataCollectionViewModel = new DataCollectionPopupViewModel();

    /**
     * Called on the creation of the dialog. <br>
     * Inflates the layout and sets the text of the popup. <br>
     * Also sets on click listeners to the buttons.
     * On accept, it will call {@link DataCollectionPopupViewModel#saveAccept(Context)}, on cancel,
     * it will display a toast message.
     *
     * @param savedInstanceState bundle of saved instance sent to function.
     * @return Dialog that was build in this function.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        Objects.requireNonNull(window).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
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
        View view = inflater.inflate(R.layout.data_collection_popup_fragment, null, false);
        ButterKnife.bind(this, view);
        // Set DialogFragment Text
        dataCollectionText.setText(R.string.data_collection_popup_warning_text);
        // Set Buttons
        acceptButton.setOnClickListener(v -> {
            dataCollectionViewModel.saveAccept(requireContext());
            dismiss();
        });
        browserButton.setOnClickListener(v -> {
            CustomTabsIntent.Builder tabsBuilder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = tabsBuilder.build();
            customTabsIntent.launchUrl(requireActivity(),
                Uri.parse(getString(R.string.login_data_collection_privacy_url)));
        });
        // Set Builder and Buttons
        return builder.setView(view)
            .show();
    }
}
