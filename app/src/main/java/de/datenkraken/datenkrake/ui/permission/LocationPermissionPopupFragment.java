package de.datenkraken.datenkrake.ui.permission;

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

import java.util.Objects;

public class LocationPermissionPopupFragment extends DialogFragment {

    @BindView(R.id.location_permission_request_popup_text)
    TextView dataCollectionText;
    @BindView(R.id.location_permission_request_accept_button)
    Button acceptButton;
    @BindView(R.id.location_permission_request_cancel_button)
    Button cancelButton;

    private final LocationPermissionPopupViewModel locationPermissionPopupViewModel =
        new LocationPermissionPopupViewModel();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        Objects.requireNonNull(window).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.location_permission_request_popup_fragment,
            null,
            false);
        ButterKnife.bind(this, view);

        // Set Buttons
        acceptButton.setOnClickListener(v -> {
            locationPermissionPopupViewModel.openSystemPermissionHandler(getActivity());
            dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            locationPermissionPopupViewModel.save(Objects.requireNonNull(getContext()), false);
            dismiss();
        });

        // Set Builder and Buttons
        return builder.setView(view)
            .show();
    }
}
