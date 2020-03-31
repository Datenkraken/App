package de.datenkraken.datenkrake.ui.scroll;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.controller.feedupdater.FeedUpdateManager;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.repository.ArticleRepository;
import de.datenkraken.datenkrake.repository.SourceRepository;
import de.datenkraken.datenkrake.ui.scroll.filtercomponents.SavedArticleFilterComponent;
import de.datenkraken.datenkrake.ui.scroll.filtercomponents.SourceUidFilterComponent;
import de.datenkraken.datenkrake.ui.scroll.filtercomponents.StringFilterComponent;
import de.datenkraken.datenkrake.ui.util.LiveDataFilter;

import java.util.List;

/**
 * Class provides the {@link Article}s to be shown in the {@link ScrollFragment}.
 * It is shared with the {@link de.datenkraken.datenkrake.ui.singlearticle.ArticleViewFragment}. <br>
 * Provides the functionalities to retrieve a next article, get filtered articles and save articles.
 * Can also set articles to read.
 *
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 */
public class ScrollViewModel extends AndroidViewModel {

    final MutableLiveData<String> searchQuery;
    final MutableLiveData<Long> filterSourceUid;
    final MutableLiveData<Boolean> filterSavedArticles;
    final LiveDataFilter<Article> liveDataFilter;

    private final ArticleRepository articleRepository;
    private final SourceRepository sourceRepository;

    public List<Article> articleStatic;

    private final FeedUpdateManager feedUpdateManager;
    private LiveData<String> sourceName;


    /**
     * Constructor to create a ScrollViewModel instance. <br>
     * Retrieves the {@link FeedUpdateManager}, {@link ArticleRepository} and {@link SourceRepository}.
     * Also creates new MutableLiveData and a new {@link LiveDataFilter}.
     *
     * @param application the current {@link Application} instance.
     */
    public ScrollViewModel(Application application) {
        super(application);
        feedUpdateManager = ((DatenkrakeApp) application).getFeedUpdateManager();
        articleRepository = ((DatenkrakeApp) application).getArticleRepository();
        sourceRepository = ((DatenkrakeApp) application).getSourceRepository();
        searchQuery = new MutableLiveData<>();
        filterSourceUid = new MutableLiveData<>();
        filterSavedArticles = new MutableLiveData<>();
        liveDataFilter = new LiveDataFilter<>(articleRepository.getArticles());

        liveDataFilter.addFilterComponent(new StringFilterComponent(searchQuery));
        liveDataFilter.addFilterComponent(new SavedArticleFilterComponent(filterSavedArticles));
        liveDataFilter.addFilterComponent(new SourceUidFilterComponent(filterSourceUid));
    }

    /**
     * Checks boundary cases of {@link Article} being first or last and returns the next position of
     * the article to be shown. If an article should be opened in the browser, it will skip the article.
     *
     * @param position  of the article currently loaded in
     * {@link de.datenkraken.datenkrake.ui.singlearticle.ArticleViewFragment}.
     * @param direction indicating whether next or previous {@link Article} needs to be fetched
     * @return position of the article that should be shown next in the
     * {@link de.datenkraken.datenkrake.ui.singlearticle.ArticleViewFragment}.
     */
    public int retrieveNextArticle(int position, int direction) {
        int nextIdx;

        if (articleStatic != null) {
            int size = articleStatic.size();


            // if swipe was detected get uid of next article in list
            // check if next position was valid
            nextIdx = position + direction;
            if (nextIdx >= 0 && nextIdx < size) {
                // Get the next Article to be loaded.
                Article nextArticle = articleStatic.get(nextIdx);
                if (nextArticle != null) {
                    if (nextArticle.description == null && nextArticle.content == null) {
                        // If Article should be opened in Browser, skip the Article.
                        return retrieveNextArticle(nextIdx, direction);
                    } else {
                        // Else return the next Article.
                        return nextIdx;
                    }
                } else {
                    return -1;
                }
            }
        }
        return -1;

    }


    /**
     * Returns a LiveData instance which will provide a list of {@link Article}s. <br>
     * Calls the {@link LiveDataFilter} to get the articles.
     *
     * @return LiveData instance with a list of {@link Article}s.
     */
    LiveData<List<Article>> getArticles() {
        return liveDataFilter.getLiveDataOutput();
    }


    /**
     * Saves the current live data state of {@link Article}s currently shown in scrollview. <br>
     * This way, it can be prevented that live data changes the scroll view during the viewing of an
     * single article which can cause navigation inconsistencies for the user. <br>
     * It get called in the {@link ScrollAdapter} before changing to single view.
     */
    void saveCurrentArticleList() {
        if (getArticles() != null) {
            articleStatic = getArticles().getValue();
        }
    }


    /**
     * Notify the {@link FeedUpdateManager} to fetch new articles.
     */
    void fetchArticles() {
        feedUpdateManager.dispatchUpdate();
    }

    /**
     * Calls {@link ArticleRepository} to invert the saved state of the given {@link Article}. <br>
     * Sets true to false and false to true.
     *
     * @param article {@link Article} to update the saved state.
     */
    void updateSavedArticle(Article article) {
        articleRepository.setSavedAndTryToDelete(article, !article.saved);
    }

    /**
     * Returns the LiveData object that contains the name of the {@link Source} identified by it's uid.
     *
     * @return LiveData containing the name of one {@link Source}.
     */
    LiveData<String> getSourceName(long uid) {
        sourceName =  Transformations.map(sourceRepository.getSource(uid), source -> {
            if (source == null) {
                return "";
            }
            return source.name;
        });

        return sourceName;
    }

    /**
     * Removes all Observers from the {@link Source} name sets it to null, so that the observers in
     *{@link ScrollFragment} doesn't get triggered anymore.
     *
     * @param owner of the observer, which should be removed.
     */
    void stopGettingSourceName(LifecycleOwner owner) {
        if (sourceName != null) {
            sourceName.removeObservers(owner);
            sourceName = null;
        }
    }

    /**
     * Calls {@link ArticleRepository} to set the read state of the given {@link Article} to true.
     *
     * @param uid of {@link Article} to update.
     */
    void updateReadArticle(Long uid) {
        articleRepository.setRead(uid);
    }
}
