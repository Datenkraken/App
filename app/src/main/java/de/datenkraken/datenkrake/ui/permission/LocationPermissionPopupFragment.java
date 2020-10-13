package de.datenkraken.datenkrake.ui.permission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Pair;
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
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.EventCollector;
import de.datenkraken.datenkrake.surveillance.graphqladapter.Permission;

import java.util.Objects;
import jp.wasabeef.blurry.Blurry;

public class LocationPermissionPopupFragment extends DialogFragment {

    @BindView(R.id.location_permission_request_popup_text)
    TextView dataCollectionText;
    @BindView(R.id.location_permission_request_accept_button)
    Button acceptButton;
    @BindView(R.id.location_permission_request_cancel_button)
    Button cancelButton;

    private final LocationPermissionPopupViewModel locationPermissionPopupViewModel =
        new LocationPermissionPopupViewModel();
    private boolean accepted = false;

    final ViewGroup root;

    public LocationPermissionPopupFragment(ViewGroup root) {
        this.root = root;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Window window =  Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow());
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        window.setDimAmount(0);
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
            accepted = true;
            locationPermissionPopupViewModel.openSystemPermissionHandler(getActivity());
            dismiss();
        });
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });
        root.post(() -> Blurry.with(view.getContext()).radius(10).sampling(1).async().animate(500).onto(root));
        // Set Builder and Buttons
        return builder.setView(view)
            .show();
    }

    @Override
    public void onDestroyView() {
        Blurry.delete(root);
        if (!accepted) {
            locationPermissionPopupViewModel.save(requireContext(), false);
            EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.PERMISSIONSTATE)
                .with(new Pair<>(Permission.LOCATION, false)));
        }
        super.onDestroyView();
    }
}
