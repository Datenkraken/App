package de.datenkraken.datenkrake.ui.datacollection;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;

import de.datenkraken.datenkrake.R;

/**
 * ViewModel for {@link DataCollectionPopupFragment}.
 * Saves the accept in the SharedPreferences.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 */
class DataCollectionPopupViewModel extends ViewModel {

    /**
     * Saves the value true in the SharedPreference LOGIN_STORAGE under the key ACCEPTED_DATA_COLLECTION,
     * displaying that the user has accepted the data collection.
     *
     * @param context to access the Shared Preferences.
     */
    void saveAccept(Context context) {
        SharedPreferences sharedPreferences =
            context.getSharedPreferences(context
                .getString(R.string.preference_login_storage), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context
            .getString(R.string.preference_accepted_data_collection), true);
        editor.apply();
    }
}
