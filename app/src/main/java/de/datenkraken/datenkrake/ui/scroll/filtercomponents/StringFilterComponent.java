package de.datenkraken.datenkrake.ui.scroll.filtercomponents;

import androidx.lifecycle.LiveData;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.ui.util.FilterComponent;

import java.util.Locale;

/**
 * {@link FilterComponent} filtering {@link Article} depending on a search query ({@link String}.
 */
public class StringFilterComponent extends FilterComponent<Article, String> {

    public StringFilterComponent(LiveData<String> validValue) {
        super(validValue);
    }

    /**
     * Searches for the query in the different {@link Article} properties. <br>
     * Currently this includes the title, description and content of the article. <br>
     * Uses containsLower to check, if query is in article.
     *
     * @param article {@link Article} which will be searched through.
     * @param s {@link String} to search for.
     * @return true, if the query was found in at least one of the strings, false otherwise.
     */
    @Override
    public boolean isValid(Article article, String s) {
        if (s == null) {
            return true;
        }

        String query = s.toLowerCase(Locale.getDefault());
        return containsLower(article.title, query)
            || containsLower(article.description, query)
            || containsLower(article.content, query);
    }

    /**
     * Returns true if the target string contains the query in lowercase or
     * the target string is null.
     *
     * @param target String which will be searched through.
     * @param query  String to search for.
     * @return true if the query was found, false otherwise.
     */
    private boolean containsLower(String target, String query) {
        return target != null && target.toLowerCase(Locale.getDefault()).contains(query);
    }
}
