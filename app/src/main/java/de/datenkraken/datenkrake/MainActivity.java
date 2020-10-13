package de.datenkraken.datenkrake;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragmentArgs;

import de.datenkraken.datenkrake.controller.feedupdater.FeedUpdateManager;
import de.datenkraken.datenkrake.logging.L;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.network.TaskDistributor;
import de.datenkraken.datenkrake.repository.SourceRepository;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.EventCollector;
import de.datenkraken.datenkrake.surveillance.EventManager;
import de.datenkraken.datenkrake.surveillance.background.BackgroundPacketSender;
import de.datenkraken.datenkrake.surveillance.background.BackgroundSupervisor;
import de.datenkraken.datenkrake.surveillance.broadcast.Receiver;
import de.datenkraken.datenkrake.surveillance.broadcast.UserActivityReceiver;
import de.datenkraken.datenkrake.surveillance.graphqladapter.ApplicationAction;
import de.datenkraken.datenkrake.surveillance.graphqladapter.Permission;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import kotlin.Triple;
import timber.log.Timber;

/**
 * Main Activity of the App.
 * It sets up the menus, the feed updater and the functionaries for the surveillance.
 *
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration menuAppBarConfiguration;
    // The uids, names and icons of the sources displayed in the menu, used to check for updates.
    private List<Triple<Long, String, Uri>> displayedSources;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    /**
     * Called, when the app starts. <br>
     * It sets the view of the MainActivity and sets the menus used in the top right and left of the app. <br>
     * It also initialises the NavController, used to navigate between the different screens of the app. <br>
     * Besides this, it also raises an event, that the app has started, that will be send to the server. <br>
     * It also sets up the feed updating and surveillance functionalities and loads sources into the menu on the left.
     *
     * @param savedInstanceState value saved in a bundle passed to the function.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag("mainActivity");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Load the action bar.
        setSupportActionBar(toolbar);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        menuAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_scroll, R.id.nav_sources,
            R.id.nav_saved, R.id.nav_scroll_source, R.id.nav_imprint, R.id.nav_about)
            .setDrawerLayout(drawerLayout)
            .build();

        // Setup navigation
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, menuAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Setup different utilities.
        setupNavDrawer(navController);
        setupFeedUpdater();
        setupNavItems();
        setupServices();
        showCategoryOption(navController);

        // Raise an event, that the app was started.
        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.APPLICATIONACTION)
            .with(ApplicationAction.STARTED));
    }

    /**
     * Called, when the option menu is created. Inflates this menu, thus adding items to the menu.
     *
     * @param menu to be inflated.
     * @return boolean value, displaying the success of the function.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Called whenever the user wants to navigate up to a specified parent activity of the current
     * activity. <br>
     * Parent activities for this activity can be set in the manifest. It is not used here. <br>
     * See https://developer.android.com/training/appbar/up-action for more information.
     *
     * @return true, only if there was a successful navigation and termination of this activity.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, menuAppBarConfiguration)
            || super.onSupportNavigateUp();
    }

    /**
     * Called, when an options item in the menu is selected.
     * Handles this call. <br>
     * Currently redirects to the settings page, when the settings menu item is clicked.
     *
     * @param item that has been selected.
     * @return boolean, displaying the success of handling the selection of the menu item.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // if Settings is Selected, go to MenuSite
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (item.getItemId() == R.id.action_settings) {
            navController.navigate(MobileNavigationDirections.actionGlobalToNavSettings());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Retrieves the saved {@link de.datenkraken.datenkrake.controller.feedupdater.FeedUpdater}
     * configuration and initialize it.
     * Setting the interval time and cache time. <br>
     * Adds an observer to the feed update manager
     */
    private void setupFeedUpdater() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        FeedUpdateManager feedUpdateManager = ((DatenkrakeApp) getApplication()).getFeedUpdateManager();

        String[] timeArray = getResources().getStringArray(R.array.settings_time_values);
        long intervalTime = Long.parseLong(sharedPreferences.getString(
            getString(R.string.preference_settings_network_update_interval),
            timeArray[0]));
        long cacheTime = Long.parseLong(sharedPreferences.getString(
            getString(R.string.preference_settings_cache_article_lifetime),
            timeArray[timeArray.length - 1]));

        feedUpdateManager.updateIntervalTime(intervalTime);
        feedUpdateManager.updateCacheTime(cacheTime);
        this.getLifecycle().addObserver(feedUpdateManager);
    }

    /**
     * Overrides the listener on the navigation view, that was set by
     * {@link NavigationUI#setupWithNavController}, to allow navigation with arguments.
     *
     * @param navController to navigate to the fragments.
     */
    private void setupNavDrawer(NavController navController) {
        navigationView.setNavigationItemSelectedListener((item) -> {
            Bundle arguments = null;

            if (item.getItemId() == R.id.nav_about) {
                LibsBuilder libsBuilder = new LibsBuilder()
                    .withAboutAppName(getString(R.string.app_name))
                    .withLicenseShown(true);
                LibsSupportFragmentArgs argument = new LibsSupportFragmentArgs.Builder(libsBuilder).build();
                arguments = argument.toBundle();
            }

            NavOptions.Builder builder = new NavOptions.Builder()
                .setLaunchSingleTop(true);

            NavOptions options = builder.build();

            // same method as in {@link NavigationUI#onNavDestinationSelected}, but this will allow
            // sending arguments.
            try {
                navController.navigate(item.getItemId(), arguments, options);

                // Close the drawer
                ViewParent parent = navigationView.getParent();
                if (parent instanceof DrawerLayout) {
                    ((DrawerLayout) parent).closeDrawer(navigationView);
                }

                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });
    }

    /**
     * Loads saved {@link Source}s into Menu and sets an observer to listen to changes.
     * On a change of the sources, the observer calls loadIntoNav. <br>
     * Transforms sources into a list of triples, containing the uids, names and icons of the sources.
     * This is done to better compare it to the displayed sources in the menu drawer (on the left of the app).
     */
    private void setupNavItems() {
        Menu menu = navigationView.getMenu();
        // Get LiveData for sources.
        SourceRepository repository = ((DatenkrakeApp) getApplication()).getSourceRepository();
        LiveData<List<Source>> sources = repository.getSources();
        navigationView.setItemIconTintList(null);

        // Initialize List of Triples with uids, name and icons of sources.
        Transformations.map(sources, oldSources -> {
            List<Triple<Long, String, Uri>> list = new ArrayList<>();
            String name;
            for (Source source : oldSources) {
                name = source.name == null ? "" : source.name.trim();
                list.add(new Triple<>(source.uid, name, source.getIcon()));
            }
            return list;
        }).observe(this, s -> loadIntoNav(s, menu));
    }

    /**
     * Loads menuItems into Drawer and sets the Navigation Controller for the Items. <br>
     * Checks, if displayed sources are the same, as given and, if this is the case, does not reload.
     * If sources are different than displayed sources, removes all menu items and loads all sources into the menu.
     * <br>
     * Uses the name, uid and icon of the sources to create menu items. <br>
     * Sets an onClickListener to each menu item. <br>
     * Saves the sources displayed in the menu as displayedSources.
     *
     * @param sources triple containing data that will be loaded into the Drawer.
     * @param menu    the Items should be loaded into.
     */
    private void loadIntoNav(List<Triple<Long, String, Uri>> sources, Menu menu) {
        // Check for relevant changes between sources and displayedSources (checks for uid, name and icon).
        if (sources != null && !sources.equals(displayedSources)) {
            Integer checked = null;
            // Get the currently checked MenuItem.
            MenuItem menuItem = navigationView.getCheckedItem();

            // Save the checked MenuItem, if it is part of nav_scroll_source.
            if (menuItem != null && menuItem.getGroupId() == R.id.nav_scroll_source) {
                checked = navigationView.getCheckedItem().getItemId();
            }

            // Delete all MenuItems in nav_scroll_source.
            menu.removeGroup(R.id.nav_scroll_source);

            // Loads the MenuItems into the group.
            for (Triple<Long, String, Uri> item : sources) {
                if (item.getSecond() != null) {
                    menuItem = menu.add(R.id.nav_scroll_source, item.getFirst().intValue(), 0,
                        item.getSecond());
                } else {
                    menuItem = menu.add(R.id.nav_scroll_source, item.getFirst().intValue(), 0,
                        R.string.source_name_loading);
                }

                loadImageAsIcon(menuItem, item.getThird());
                setMenuOnClick(menuItem, item.getFirst());
            }

            menu.setGroupCheckable(R.id.nav_scroll_source, true, true);

            // Set the MenuItem saved to checked.
            if (checked != null) {
                menuItem = menu.findItem(checked);

                if (menuItem != null) {
                    menuItem.setChecked(true);
                }
            }

            // Save the sources displayed.
            displayedSources = sources;
        }
    }

    /**
     * Sets an onClickListener for MenuItems to show only {@link de.datenkraken.datenkrake.model.Article}s
     * of {@link Source}.
     * On Click, navigates and sends a bundle with the article uid to the scroll view,
     * in which the articles are filtered accordingly.
     *
     * @param menuItem the onClickListener is used on.
     * @param sourceUid  uid of the source the onClickListener redirects to.
     */
    private void setMenuOnClick(MenuItem menuItem, Long sourceUid) {
        menuItem.setOnMenuItemClickListener(v -> {
            NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
            // create bundle with uid for filter.
            Bundle bundle = new Bundle();
            bundle.putLong("source_id", sourceUid);
            navController.navigate(R.id.nav_scroll_source, bundle);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    /**
     * Load a given Image as Icon for a MenuItem. <br>
     * While loading, set the icon to a placeholder. <br>
     * On failure, load a fallback image.
     *
     * @param menuItem the Image is set to.
     * @param image to be loaded as icon.
     */
    private void loadImageAsIcon(MenuItem menuItem, Uri image) {
        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.ic_loading_icon)
            .into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                                            @Nullable Transition<? super Drawable>
                                                transition) {
                    menuItem.setIcon(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    menuItem.setIcon(placeholder);
                }
            });
    }

    /**
     * Setups necessary services for the surveillance system and network.
     */
    private void setupServices() {
        EventManager.setup(new WeakReference<>(this));
        this.getLifecycle().addObserver(EventManager.getInstance());
        TaskDistributor.setup(((DatenkrakeApp) getApplication()).getAuthenticationManager(), getApplicationContext());

        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest
            .Builder(BackgroundPacketSender.class, 30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build();

        WorkManager workManager = WorkManager.getInstance(this);
        workManager.cancelAllWork();

        workManager.enqueueUniquePeriodicWork(
            getResources().getString(R.string.background_service_sender),
            ExistingPeriodicWorkPolicy.REPLACE,
            request);

        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(BackgroundSupervisor.class)
            .setInitialDelay(1200000L - (System.currentTimeMillis() % 1200000L), TimeUnit.MILLISECONDS)
            .addTag(getResources().getString(R.string.background_service_supervisor))
            .build();

        workManager.enqueue(oneTimeWorkRequest);
        L.i("Supervisor queried, set to %s",
            new Date(1200000L - (System.currentTimeMillis() % 120000L)
                + System.currentTimeMillis()).toString());

        Receiver receiver = new UserActivityReceiver();
        registerReceiver(receiver, receiver.getNonManifestIntentsFilter());
    }

    public void killWorker() {
        WorkManager workManager = WorkManager.getInstance(this);
        workManager.cancelAllWork();
    }

    /**
     * Navigates the user to the category recommendation page once after a new registration.
     * This happens only, when the key "NEWLY_REGISTERED" is not contained in the SharedPreferences.
     *
     * @param navController to navigate to the fragment.
     */
    private void showCategoryOption(NavController navController) {
        // if user has just registered then show him the category adding view
        // will be shown when data collection popup is also shown

        String key = getString(R.string.newly_registered);
        SharedPreferences settings = getSharedPreferences(key, MODE_PRIVATE);
        if (!settings.contains(key)) {
            // user is logging in for first time, show him category picking view
            navController.navigate(R.id.nav_recomm);
            // record the fact that the app has been started at least once
            settings.edit().putBoolean(key, false).apply();
        }
    }

    /**
     * Called when the App is paused. <br>
     * Raises an event that will be send to the server, signaling that the app has been paused.
     */
    @Override
    protected void onPause() {
        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.APPLICATIONACTION)
            .with(ApplicationAction.BACKGROUND));
        super.onPause();
    }

    /**
     * Called, when the app is completely closed. <br>
     * Raises an event that will be send to the server, signaling that the app was closed.
     */
    @Override
    protected void onDestroy() {
        EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.APPLICATIONACTION)
            .with(ApplicationAction.CLOSED));
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == getResources().getInteger(R.integer.permission_location)
            && grantResults.length > 0) {

            SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.preference_permission),
                    Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            editor.putBoolean(getString(R.string.preference_permission_location),
                granted);
            EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.PERMISSIONSTATE)
                .with(new Pair<>(Permission.LOCATION, granted)));
            editor.apply();
        }
    }
}
