package de.datenkraken.datenkrake.ui.scroll;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import de.datenkraken.datenkrake.R;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.ui.util.HtmlDefaultTagHandler;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This class creates the View Holders and populates them with {@link Article} information.
 * It also sets, sorts and filters the article list for the {@link ScrollFragment}.
 *
 * @author Simon Schmalfuß; - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
class ScrollAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    private final ScrollViewModel scrollViewModel;
    private final Context context;
    List<? extends Article> articles;

    // colors for setting the viewholder depending on whether
    // articles has been read or not
    private final int COLOR_GREY;
    private final int COLOR_WHITE;

    /**
     * Constructor for the class, initializing it.
     *
     * @param scrollViewModel to be used.
     * @param context to be used.
     */
    ScrollAdapter(ScrollViewModel scrollViewModel, Context context) {
        this.scrollViewModel = scrollViewModel;
        this.context = context;
        COLOR_GREY = ContextCompat.getColor(context, R.color.mainBackgroundDark);
        COLOR_WHITE = ContextCompat.getColor(context, R.color.mainBackground);
    }

    /**
     * Called on the creation of the view. Initializes ArticleViewHolder.
     *
     * @param parent of ViewGroup.
     * @param viewType used in ArticleViewHolder.
     * @return a new ArticleViewHolder.
     */
    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
        return new ArticleViewHolder(v);
    }

    /**
     * Loads Content into the ViewHolders of the {@link ScrollFragment}. <br>
     * Fills the ViewHolders with title, author, date, source and content/description from the {@link Article}s.
     * Loads the first image of each article using Glide. <br>
     * Sets on clickListener for the bookmark to save an article and for the item view to navigate to
     * a {@link de.datenkraken.datenkrake.ui.singlearticle.ArticleViewFragment} containing the selected
     * article. <br>
     * It also greys out read articles.
     *
     * @param holder to be filled.
     * @param position of Holder.
     */
    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {

        Article currentArticle = articles.get(position);

        // Load article description or content.
        String articleContent = getDescription(currentArticle);

        // populate article view holder with data
        // Load content.
        if (articleContent != null) {
            holder.description.setText(HtmlCompat.fromHtml(articleContent,
                HtmlCompat.FROM_HTML_MODE_COMPACT,
                source -> new GradientDrawable(),
                new HtmlDefaultTagHandler()));
        }

        // Load title.
        if (currentArticle.title != null) {
            holder.title.setText(HtmlCompat.fromHtml(currentArticle.title,
                HtmlCompat.FROM_HTML_MODE_COMPACT));
        }

        // Load author, date and source into textview.
        // They are loaded together, since they are one continuous text.
        holder.articleInformation.setText(String.format("%s%s%s",
                                            getFormattedAuthor(currentArticle),
                                            getFormattedDate(currentArticle),
                                            getSource(currentArticle)));

        // if the article has been read, make the itemView grey
        // Decide on color, if there is no image.
        int articleFallbackColor;
        if (currentArticle.read) {
            articleFallbackColor = R.color.mainBackgroundDark;
            ((CardView) holder.itemView).setCardBackgroundColor(COLOR_GREY);
        } else {
            articleFallbackColor = R.color.mainBackground;
            ((CardView) holder.itemView).setCardBackgroundColor(COLOR_WHITE);
        }

        // Set bookmark icon
        if (currentArticle.saved) {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_true);
        } else {
            holder.bookmark.setImageResource(R.drawable.ic_bookmark_false);
        }

        // Load image.
        Glide.with(holder.image.getContext())
            .load(currentArticle.imageUrl)
            .error(R.drawable.ic_missing_icon)
            .fallback(articleFallbackColor)
            .into(holder.image);


        // Set listener to buttons.
        holder.bookmark.setOnClickListener(v -> scrollViewModel.updateSavedArticle(currentArticle));

        holder.itemView.setOnClickListener(v -> {
            // If no Content, and Description, open Article in Browser.
            if (currentArticle.content == null && currentArticle.description == null
                && currentArticle.source != null && currentArticle.link != null) {
                // Set Article read and open ChromeTab.
                scrollViewModel.updateReadArticle(currentArticle.uid);
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(context, Uri.parse(currentArticle.link.toString()));
            } else {
                // Open Article View.
                NavController navController = Navigation.findNavController(v);
                Bundle bundle = new Bundle();
                bundle.putInt("listposition", position);
                // save scroll view articles
                scrollViewModel.saveCurrentArticleList();
                navController.navigate(R.id.nav_single_article, bundle);
            }
        });
    }

    /**
     * Gets the Size of the variable articles.
     *
     * @return size of articles as int.
     */
    @Override
    public int getItemCount() {
        return articles == null ? 0 : articles.size();
    }

    /**
     * Set the currently displayed list of {@link Article}s. <br>
     * Removes all articles that have no content, description, source and link. <br>
     * Sorts the list of articles by date. <br>
     * Then calculates the difference between the old list of articles and the new one
     * and notifies on changes.
     *
     * @param articleList the complete list of articles to display.
     */
    void setArticleList(final List<? extends Article> articleList) {

        // Remove all articles which are null or don't have Content, Description, Source and Link.
        removeEmptyArticles(articleList);

        if (articleList != null) {
            // Sort List by Date, the "minus" before saveCompareDates is used to get a descending list
            Collections.sort(articleList, (Comparator<Article>) (o1, o2)  ->
                -safeCompareDates(o1.publishedDate, o2.publishedDate));
        }

        if (articles == null) {
            articles = articleList;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                /**
                 * Gets size of articles.
                 * @return size of articles as int.
                 */
                @Override
                public int getOldListSize() {
                    return articles.size();
                }

                /**
                 * Gets the size of articleList.
                 * @return size of articleList as int or 0, if articleList is null.
                 */
                @Override
                public int getNewListSize() {
                    if (articleList != null) {
                        return articleList.size();
                    } else {
                        return 0;
                    }
                }

                /**
                 * Checks, if two items in {@link Article}s and articleList have the same uid.
                 *
                 * @param oldItemPosition position, where the item is in the old list articles.
                 * @param newItemPosition position, where the item is in the new list articleList.
                 * @return true, if items have equal uids.
                 */
                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    if (articleList != null) {
                        return articles.get(oldItemPosition).uid == articleList.get(newItemPosition).uid;
                    } else {
                        return articles == null;
                    }
                }

                /**
                 * Checks, if two items in two lists are equal.
                 *
                 * @param oldItemPosition position, where the item is in the old list {@link Article}s.
                 * @param newItemPosition position, where the item is in the new list articlesList.
                 * @return true, if items are equal.
                 */
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Article oldArticle = articles.get(oldItemPosition);
                    Article newArticle = null;
                    if (articleList != null) {
                        newArticle = articleList.get(newItemPosition);
                    }
                    return oldArticle.equals(newArticle);
                }
            });
            articles = articleList;
            result.dispatchUpdatesTo(this);
        }
    }

    /**
     * Removes {@link Article} without {@link Article#content}, {@link Article#description},
     * {@link Article#source} and {@link Article#link} from the given list.
     *
     * @param articleList {@link List} to remove {@link Article} from.
     */
    private void removeEmptyArticles(final List<? extends Article> articleList) {
        if (articleList == null) {
            return;
        }

        Iterator<? extends Article> iterator = articleList.iterator();
        while (iterator.hasNext()) {
            Article article = iterator.next();
            if (article == null) {
                iterator.remove();
                continue;
            }

            if (article.content == null
                && article.description == null && article.source == null
                && article.link == null) {
                iterator.remove();
            }
        }
    }

    /**
     * Compares two given {@link Date}s with each other. Null is considered always less then a
     * {@link Date}.
     *
     * @param date1 {@link Date} 1 to compare
     * @param date2 {@link Date} 2 to compare
     * @return -1 if date1 is less, 1 if date1 is bigger, 0 if equal.
     */
    private int safeCompareDates(Date date1, Date date2) {
        if (date1 == null) {
            if (date2 == null) {
                return 0; // both dates are null so equal
            }
            return -1; // date1 is null, date2 is not so date1 < date2
        }

        if (date2 == null) {
            return 1; // date1 is not null, date2 is null, so date1 > date2
        }
        return date1.compareTo(date2); // both are not null, so compare them
    }

    /**
     * Extracts the description to display from the given {@link Article}.
     * {@link Article#description} is prioritized, fallbacks to {@link Article#content} if null.
     *
     * @param article {@link Article} containing the description to extract.
     * @return {@link String} or null
     */
    private String getDescription(final Article article) {
        if (article.description != null) {
            return article.description;
        } else {
            return article.content;
        }
    }

    /**
     * Returns {@link Article#publishedDate} in a formatted {@link String} if it is not null.
     * Otherwise it returns an empty {@link String}.
     *
     * @param article {@link Article} with the {@link Article#publishedDate} to extract and format
     * @return {@link String}
     */
    @NonNull
    private String getFormattedDate(final Article article) {
        if (article.publishedDate != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy",
                java.util.Locale.getDefault());

            return simpleDateFormat.format(article.publishedDate) + ", ";
        }

        return  "";
    }

    /**
     * Returns {@link de.datenkraken.datenkrake.model.Source#name} in the given {@link Article}. <br>
     * Returns an empty {@link String} if {@link de.datenkraken.datenkrake.model.Source#name} is null.
     *
     * @param article {@link Article}
     * @return {@link String}
     */
    @NonNull
    private String getSource(final Article article) {
        return article.source.name != null ? article.source.name : "";
    }

    /**
     * Returns {@link Article#author} in the given {@link Article}. <br>
     * Returns an empty {@link String} if {@link Article#author} is null.
     *
     * @param article {@link Article}
     * @return {@link String}
     */
    @NonNull
    private String getFormattedAuthor(final Article article) {
        return article.author != null ? article.author + " - " : "";
    }
}
