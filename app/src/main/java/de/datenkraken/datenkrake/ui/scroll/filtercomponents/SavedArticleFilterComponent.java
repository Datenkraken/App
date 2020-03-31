package de.datenkraken.datenkrake.ui.scroll.filtercomponents;

import androidx.lifecycle.LiveData;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.ui.util.FilterComponent;

/**
 * {@link FilterComponent} filtering {@link Article}s on their {@link Article#saved} status.
 */
public class SavedArticleFilterComponent extends FilterComponent<Article, Boolean> {

    public SavedArticleFilterComponent(LiveData<Boolean> validValue) {
        super(validValue);
    }

    /**
     * Filters the given {@link Article}. Only applies if the given boolean is true.
     *
     * @param article {@link Article} to filter
     * @param shouldFilter {@link Boolean} used to decide if filter applies or not
     * @return true or false depending on {@link Article#saved} and the given boolean
     */
    @Override
    public boolean isValid(Article article, Boolean shouldFilter) {
        if (shouldFilter == null || !shouldFilter) {
            return true;
        }
        return article.saved;
    }
}
