package de.datenkraken.datenkrake.ui.scroll.filtercomponents;

import androidx.lifecycle.LiveData;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.ui.util.FilterComponent;

/**
 * {@link FilterComponent} filtering {@link Article}s on their {@link Article#source}s's
 * {@link de.datenkraken.datenkrake.model.Source#uid}s.
 */
public class SourceUidFilterComponent extends FilterComponent<Article, Long> {

    public SourceUidFilterComponent(LiveData<Long> validValue) {
        super(validValue);
    }

    /**
     * Checks if {@link Article#source} uid is the same as the given one.
     *
     * @param article {@link Article} to filter
     * @param uid {@link de.datenkraken.datenkrake.model.Source#uid} to filter for
     * @return true if the given uid is null or the same as in
     * {@link de.datenkraken.datenkrake.model.Source#uid}, false otherwise
     */
    @Override
    public boolean isValid(Article article, Long uid) {
        if (uid == null) {
            return true;
        }

        return article.source.uid == uid;
    }
}
