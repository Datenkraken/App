package de.datenkraken.datenkrake.ui.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModel;
import de.datenkraken.datenkrake.R;

class LocationPermissionPopupViewModel extends ViewModel {

    void save(Context context, boolean status) {
        SharedPreferences sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.preference_permission),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_permission_location), status);
        editor.apply();

    }

    void openSystemPermissionHandler(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
            activity.getResources().getInteger(R.integer.permission_fine_location));

    }
}
