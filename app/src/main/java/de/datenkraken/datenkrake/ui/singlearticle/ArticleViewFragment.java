package de.datenkraken.datenkrake.ui.singlearticle;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.EventCollector;
import de.datenkraken.datenkrake.surveillance.actions.ArticleAction;
import de.datenkraken.datenkrake.ui.scroll.ScrollViewModel;
import de.datenkraken.datenkrake.ui.util.GlideImageGetter;
import de.datenkraken.datenkrake.ui.util.HtmlDefaultTagHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import kotlin.Triple;

import org.jetbrains.annotations.NotNull;

/**
 * Fragment for the view of a single Article.
 * Displaying title, author, date, source, content and title image. <br>
 * Those Articles can also be shared, saved and opened in a browser. <br>
 * Furthermore, a user can switch to articles before and after this one in the entire list.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
public class ArticleViewFragment extends Fragment {


    @BindView(R.id.article_view_author)
    TextView author;
    @BindView(R.id.article_view_title)
    TextView title;
    @BindView(R.id.article_view_content)
    TextView content;
    @BindView(R.id.article_view_textsource)
    TextView dateAndSource;
    @BindView(R.id.article_view_image)
    ImageView image;
    @BindView(R.id.article_view_bookmark)
    ImageButton bookmark;
    @BindView(R.id.article_scroll)
    ScrollView scrollView;
    @BindView(R.id.article_open_browser)
    FloatingActionButton browserButton;

    private ArticleViewModel articleViewModel;
    private ScrollViewModel scrollModel;
    private Article currentArticle;

    //distance of swipes
    private  int downputX;
    private int upX;

    // when swiping, distance between putting down and lifting up of finger needs
    // to be greater than this in coordinates
    private static int SWIPE_DISTANCE;

    // constant for determining necessary swipe distance based on screen size
    private static final double swipeConstant = 0.15;

    // this is needed so that changeArticle knows whether to get the next or recent article.
    // 1 for the next and -1 for the recent. it will be used to determine the next article position.
    private static final int LEFT = -1;
    private static final int RIGHT = 1;


    private int articlePosition;

    /**
     * Called on the creation of the menu. Inflates the main menu and the share button. <br>
     * Sets a button to share the {@link Article}s url. <br>
     *
     * @param menu to load the items into.
     * @param inflater to inflate the menu layout.
     */
    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        // Inflate menus.
        inflater.inflate(R.menu.share_button, menu);
        inflater.inflate(R.menu.main, menu);
        // Set menu item color.
        MenuItem menuItem = menu.findItem(R.id.action_share_button);
        menuItem.getIcon().setTint(ContextCompat.getColor(Objects.requireNonNull(getContext()),
            R.color.white));
        // Set menu item on click listener.
        menuItem.setOnMenuItemClickListener(v -> {
            // Send share information to backend.
            EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.ARTICLEACTION)
                .with(new Triple<>(ArticleAction.SHARED, currentArticle.title, currentArticle.source.url.toString())));

            // Share article.
            if (currentArticle.link != null) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, currentArticle.link.toString());
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent,
                    requireActivity().getString(R.string.article_share_button_description));
                startActivity(shareIntent);
            } else {
                Toast.makeText(getContext(),
                    getString(R.string.article_no_source_found),
                    Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    /**
     * Called on creation of the fragment. <br>
     * Sets the options menu. <br>
     * Sets the swipe distance for swiping and sets the read state of the {@link Article}s. <br>
     * Calculates the list position of the article in the ScrollView. <br>
     *
     * @param savedInstanceState bundle of saved instance sent to function.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }

        // get current size of display to determine dynamic swipe length
        // also accounts for rotations of the device
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        // can be casted to int as it doesn't need to be very exact
        SWIPE_DISTANCE = (int) (swipeConstant * width);

        // set requireActivity so that ViewModels can be shared with ScrollFragment.
        scrollModel = new ViewModelProvider(requireActivity()).get(ScrollViewModel.class);

        List<Article> scrollArticles = scrollModel.articleStatic;

        if (arguments.containsKey("listposition") && scrollArticles != null
            && scrollArticles.get(articlePosition) != null) {
            articlePosition = arguments.getInt("listposition");

            int listSize = scrollArticles.size();
            if (articlePosition >= 0 && articlePosition < listSize) {

                // we get the position of the currently loaded article in the list and can retrieve its id
                long articleID = scrollArticles.get(articlePosition).uid;
                ArticleViewModel.Factory factory = new ArticleViewModel.Factory(
                    requireActivity().getApplication(), articleID);

                articleViewModel = new ViewModelProvider(this, factory).get(ArticleViewModel.class);
                articleViewModel.setRead(articleID);
            }
        }
    }

    /**
     * Called when the view is created. <br>
     * Inflates the view and sets an on touch listener on the screen to allow changing
     * between the {@link Article}s by swiping left or right.
     *
     * @param inflater to inflate the view.
     * @param container the view is contained in.
     * @param savedInstanceState bundle of saved instance sent to function.
     * @return the inflated view.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_single_article, container, false);

        ButterKnife.bind(this, view);

        // setting touch listener for swiping to next article
        scrollView.setOnTouchListener((v, event) -> {
            boolean result = false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downputX = (int) event.getX();
                return false;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                upX = (int) event.getX();
                if (upX - downputX > SWIPE_DISTANCE) {
                    result = changeArticle(view, LEFT);
                } else if (downputX - upX > SWIPE_DISTANCE) {
                    result = changeArticle(view, RIGHT);
                }
            }
            return result;
        });

        if (articleViewModel != null) {
            subscribeUi(articleViewModel.getArticle());
        }

        return view;
    }

    /**
     * Fetches the position in the list of the next {@link Article} to show, opens single view to show
     * the article and navigates there. <br>
     * Pops the current {@link Article} from the backstack created by the NavController. <br>
     * If the end or beginning of the article list is reached, it will display a message that the user
     * is at the end or beginning of the article list.
     *
     * @param v View that gets passed along from the click handlers.
     * @param direction int which says whether users wants to see article before or after this one
     *                  (-int for previous, +int for next {@link Article}).
     * @return boolean, indicating, if article was changed, or not.
     */
    private boolean changeArticle(View v, int direction) {

        int newPosition = scrollModel.retrieveNextArticle(articlePosition, direction);
        boolean result = false;

        if (newPosition != -1) {
            // if currently viewed article is not the first or last in line
            NavController navController = Navigation.findNavController(v);

            Bundle bundle = new Bundle();
            bundle.putInt("listposition", newPosition);
            // removing the current element from the navigation stack so that
            // one can also go back to scroll view
            if (navController.getCurrentDestination() != null) {
                navController.popBackStack(navController.getCurrentDestination().getId(), true);
                navController.navigate(R.id.nav_single_article, bundle);
                result = true;
            } else {
                Toast.makeText(getContext(),
                    getString(R.string.article_not_found_warning), Toast.LENGTH_LONG).show();
            }

        } else {
            // show toast saying no more articles this way
            Toast.makeText(getContext(), getString(R.string.toast_no_articles_warning), Toast.LENGTH_SHORT).show();
        }
        downputX = 0;
        upX = 0;
        return result;
    }

    /**
     * Fills the views in the fragment with content from the current {@link Article}
     * and sets on click listeners to the browser and bookmark button. <br>
     * Uses Glide to load the first image of the article and uses {@link GlideImageGetter}
     * for the other images. <br>
     * Loads title, author, date, source, content/description into their corresponding views.
     * If some of the contents of the article is not available, hides the corresponding view.
     */
    private void showArticle() {

        // Open article on click in browser using CustomTabs.
        setBrowseButtonOnClickListener();

        // Save the article on click.
        setBookmarkButtonOnClickListener();

        // Get size of the display to properly display the image.
        image.getLayoutParams().height = getImageHeight();

        // Display values from the ArticleViewModel.
        if (currentArticle.author != null) {
            author.setVisibility(View.VISIBLE);
            author.setText(String.format("%s%s", getString(R.string.article_from_author),
                                                    currentArticle.author));
        } else {
            author.setVisibility(View.GONE);
        }

        if (currentArticle.title != null) {
            title.setVisibility(View.VISIBLE);
            title.setText(HtmlCompat.fromHtml(currentArticle.title,
                HtmlCompat.FROM_HTML_MODE_COMPACT));
        } else {
            title.setVisibility(View.GONE);
        }

        String dateString;

        if (currentArticle.publishedDate != null) {
            Date d = currentArticle.publishedDate;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy",
                java.util.Locale.getDefault());
            dateString = simpleDateFormat.format(d) + ", ";
        } else {
            dateString = "";
        }

        String articleSource = getSourceString();

        if (articleSource.isEmpty() && currentArticle.publishedDate == null) {
            dateAndSource.setVisibility(View.GONE);
        } else {
            dateAndSource.setVisibility(View.VISIBLE);
            // Combine these two strings, since they are displayed as a continuous string.
            String combinedDateSource = dateString + articleSource;
            dateAndSource.setText(combinedDateSource);
        }

        String articleContent = getContent();

        // Display content. If no content is available, display description.
        if (articleContent != null) {
            content.setVisibility(View.VISIBLE);
            content.setText(HtmlCompat.fromHtml(articleContent,
                HtmlCompat.FROM_HTML_MODE_COMPACT,
                new GlideImageGetter(this.getContext(), content),
                new HtmlDefaultTagHandler()));
        }  else {
            content.setVisibility(View.GONE);
        }

        if (currentArticle.imageUrl != null) {
            image.setVisibility(View.VISIBLE);
            int radius = 45;
            Glide.with(requireActivity().getApplicationContext())
                .load(currentArticle.imageUrl)
                .into(image);
            image.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, -radius, view.getWidth(), view.getHeight(), radius);
                }
            });
            image.setClipToOutline(true);
        } else {
            image.setVisibility(View.GONE);
        }

        if (currentArticle.saved) {
            bookmark.setImageResource(R.drawable.ic_bookmark_true);
        } else {
            bookmark.setImageResource(R.drawable.ic_bookmark_false);
        }
    }

    /**
     * Sets an observer to listen to changes in the {@link Article} and sets currentArticle.
     *
     * @param liveData to be observed.
     */
    private void subscribeUi(LiveData<Article> liveData) {
        liveData.observe(getViewLifecycleOwner(), article -> {
            if (article != null) {
                currentArticle = article;
                this.showArticle();
            }
        });
    }

    /**
     * Sets the {@link android.view.View.OnClickListener} for {@link #browserButton}.
     * Opens a chrome tab if {@link Article#link} of {@link #currentArticle} is not null,
     * otherwise it shows a {@link Toast} informing the user.
     */
    private void setBrowseButtonOnClickListener() {
        browserButton.setOnClickListener(v -> {
            // Raise Chrome Tab event for Article.
            EventCollector.raiseEvent(new DataCollectionEvent<>(DataCollectionEventType.ARTICLEACTION)
                .with(new Triple<>(ArticleAction.CHROMEOPENED,
                    currentArticle.title, currentArticle.source.url.toString())));

            if (currentArticle.link != null) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(requireActivity(), Uri.parse(currentArticle.link.toString()));
            } else {
                Toast.makeText(getContext(), getString(R.string.article_no_website_found),
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Sets the {@link android.view.View.OnClickListener} for {@link #bookmark}.
     * Asks the user for conformation if {@link #currentArticle}'s {@link Article#source} is marked
     * as deleted and {@link #currentArticle} would be deleted permanently.
     */
    private void setBookmarkButtonOnClickListener() {
        bookmark.setOnClickListener(v -> {
            if (currentArticle.source.deleted) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.article_delete_permantly_title);
                builder.setMessage(R.string.article_delete_permantly_message);
                builder.setPositiveButton(R.string.button_accept, (dialog, which) -> {
                    if (this.getView() != null) {
                        articleViewModel.toggleSaveArticle(currentArticle);
                        Navigation.findNavController(this.getView()).popBackStack();
                    }
                });
                builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                });
                builder.show();
            } else {
                articleViewModel.toggleSaveArticle(currentArticle);
            }
        });
    }

    /**
     * Returns a appropriate image height for the current screen height.
     *
     * @return image height.
     */
    private int getImageHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        return (height / 4);
    }

    /**
     * Returns a non null string to display the {@link Source} of {@link #currentArticle}.
     *
     * @return {@link String} format of {@link Article#source}.
     */
    @NonNull
    private String getSourceString() {
        Source source = currentArticle.source;
        if (source != null && source.name != null) {
            return source.name;
        } else if (currentArticle.link != null) {
            return currentArticle.link.toString();
        } else {
            return  "";
        }
    }


    /**
     * Extracts the content to display from {@link #currentArticle}.
     * {@link Article#content} is prioritized, fallbacks to {@link Article#description} if null.
     *
     * @return {@link String} or null
     */
    private String getContent() {
        if (currentArticle.content != null) {
            return currentArticle.content;
        } else {
            return currentArticle.description;
        }
    }
}
