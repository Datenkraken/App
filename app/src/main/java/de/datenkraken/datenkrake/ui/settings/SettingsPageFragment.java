package de.datenkraken.datenkrake.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.ui.login.LoginActivity;
import de.datenkraken.datenkrake.ui.settings.datadelete.DataDeletePopupFragment;

import java.util.Objects;

import timber.log.Timber;

/**
 * Fragment displaying the Settings Page with Preferences.
 * With the option to clear the Cache, Logout, set the feed update interval, set the article cache time
 * and delete the user data and account.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Jan Klinkmann - jan.klinkmann@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class SettingsPageFragment extends PreferenceFragmentCompat {

    private SettingsPageViewModel settingsModel;

    /**
     * Sets the preferences of the fragment.
     *
     * @param savedInstanceState bundle of saved instance sent to this function.
     * @param rootKey used for preferences.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Timber.tag("SettingsPageFragment");
        setPreferencesFromResource(R.xml.setting_page_preferences, rootKey);
        settingsModel = new ViewModelProvider(this).get(SettingsPageViewModel.class);
    }

    /**
     * Called upon creation of view. Sets ViewModel, observer for the cacheStatus and deleteState in
     * {@link SettingsPageViewModel} and listener for the preferences. <br>
     * If deleteState changes to Success, logs out and displays message.
     * If deleteState changes to Failure, displays message.
     *
     * @param inflater for layout.
     * @param container of ViewGroup.
     * @param savedInstanceState bundle of saved instance sent to this function.
     * @return view of fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // showing toolbar in case user pressed back button in categories view
        if (getActivity() != null) {
            ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (toolbar != null) {
                toolbar.show();
            }
        }

        // add observer to display toast message
        settingsModel.getCacheStatus().observe(getViewLifecycleOwner(), event -> {
            if (event == null || event.peekContent() == null) {
                return;
            }
            displayCacheStatus(event.getContentIfNotHandled());
        });

        // Observe the data delete. On success, logs out and displays success message, else displays
        // message for failure.
        settingsModel.getDeleteState().observe(getViewLifecycleOwner(), state -> {
            // On success, display success, and logout.
            if (state == SettingsPageViewModel.DeleteState.SUCCESS) {
                Toast.makeText(getContext(),
                    getString(R.string.settings_data_delete_success), Toast.LENGTH_LONG).show();
                redirectToLogin();
            } else if (state == SettingsPageViewModel.DeleteState.FAILURE) {
                // On failure, display failure.
                Toast.makeText(getContext(),
                    getString(R.string.settings_data_delete_failure), Toast.LENGTH_LONG).show();
            }
        });

        setUpOnClickListener();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Displays the cache status, after trying to delete it.
     *
     * @param content true if the cache got deleted successful, false otherwise
     */
    private void displayCacheStatus(boolean content) {
        if (content) {
            Toast.makeText(getContext(), getString(R.string.settings_cache_clear_success),
                Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getContext(), getString(R.string.settings_cache_clear_failure),
            Toast.LENGTH_LONG).show();


    }

    /**
     * Creates and binds the onClickListener and onPreferenceChangeListener for the different Preferences. <br>
     * Sets the onClickListener for deleting the cache, logging out and deleting data. <br>
     * Sets onPreferenceChangeListener for changing the update interval and cache time.
     */
    private void setUpOnClickListener() {
        // Use Preference clear cache as a Button. On Click DeleteCache from SettingsViewModel is executed
        Preference clearCache =
            findPreference(getString(R.string.preference_settings_clear_cache_button));

        Objects.requireNonNull(clearCache).setOnPreferenceClickListener(preference -> {
            settingsModel.deleteCache();
            return true;
        });

        Preference logoutButton =
            findPreference(getString(R.string.preference_settings_logout_button));


        //On click listener for Logout Button.
        Objects.requireNonNull(logoutButton).setOnPreferenceClickListener(preference -> {
            settingsModel.logoutUser();
            redirectToLogin();
            return true;
        });

        Preference choseCategories = findPreference("categories_button");

        Objects.requireNonNull(choseCategories).setOnPreferenceClickListener(preference -> {
            NavController controller = Navigation.findNavController(Objects.requireNonNull(getView()));
            controller.navigate(R.id.nav_cat_recomm);
            return true;
        });


        ListPreference updateInterval =
            Objects.requireNonNull(findPreference(getString(R.string.preference_settings_network_update_interval)));

        ListPreference cacheTime =
            Objects.requireNonNull(findPreference(getString(R.string.preference_settings_cache_article_lifetime)));

        updateInterval.setSummary(updateInterval.getEntry());

        cacheTime.setSummary(cacheTime.getEntry());

        updateInterval.setOnPreferenceChangeListener((preference, newValue) -> {
            String newVal = newValue.toString();
            settingsModel.setUpdateInterval(newVal);
            updateInterval.setSummary(
                updateInterval.getEntries()[updateInterval.findIndexOfValue(newVal)]);
            return true;
        });

        cacheTime.setOnPreferenceChangeListener((preference, newValue) -> {
            String newVal = newValue.toString();
            settingsModel.setCacheTime(newVal);
            cacheTime.setSummary(
                cacheTime.getEntries()[cacheTime.findIndexOfValue(newVal)]);
            return true;
        });

        Preference deleteData =
            findPreference(getString(R.string.preference_settings_delete_data_button_key));
        Objects.requireNonNull(deleteData).setOnPreferenceClickListener(preference -> {
            DataDeletePopupFragment dataDeletePopupFragment =
                new DataDeletePopupFragment(settingsModel);
            dataDeletePopupFragment.show(Objects.requireNonNull(
                getActivity()).getSupportFragmentManager(), "dataDelete"
            );
            return true;
        });

        Preference privacyPolicy =
            findPreference(getString(R.string.preference_settings_privacy_policy_button));
        Objects.requireNonNull(privacyPolicy).setOnPreferenceClickListener(preference -> {
            CustomTabsIntent.Builder tabsBuilder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = tabsBuilder.build();
            customTabsIntent.launchUrl(requireActivity(),
                Uri.parse(getString(R.string.login_data_collection_privacy_url)));
            return true;
        });
    }

    /**
     * Redirects to the {@link LoginActivity} and prevents going back.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}
