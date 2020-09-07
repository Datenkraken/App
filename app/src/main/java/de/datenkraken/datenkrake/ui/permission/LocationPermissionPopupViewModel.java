package de.datenkraken.datenkrake.ui.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

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
            permissionToRequest(),
            activity.getResources().getInteger(R.integer.permission_location));
    }

    String[] permissionToRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            };
        } else {
            return new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
        }
    }
}
