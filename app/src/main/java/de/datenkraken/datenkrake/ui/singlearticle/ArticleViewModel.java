package de.datenkraken.datenkrake.ui.singlearticle;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import de.datenkraken.datenkrake.DatenkrakeApp;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.repository.ArticleRepository;

import org.jetbrains.annotations.NotNull;


/**
 * The view model for the {@link ArticleViewFragment} holds the article to be displayed.
 * The view model is scoped to its fragment.
 * Here, the {@link Article}s values read and saved can be set.
 *
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Tobias Kröll - tobias.kroell@stud.tu-darmstadt.de
 * @author Simon Schmalfuß - simon.schmalfuss@stud.tu-darmstadt.de
 */
class ArticleViewModel extends AndroidViewModel {

    private final LiveData<Article> observableArticle;
    private final ArticleRepository articleRepository;

    /**
     * Constructor for a new view model instance. <br>
     * Gets the {@link ArticleRepository} and the currently shown {@link Article} as LiveData.
     *
     * @param application the application instance.
     * @param articleId   the currently shown article id.
     */
    ArticleViewModel(@NonNull Application application, final long articleId) {
        super(application);

        articleRepository = ((DatenkrakeApp) application).getArticleRepository();
        this.observableArticle = articleRepository.getArticle(articleId);

    }

    /**
     * Returns the currently shown {@link Article} wrapped in a {@link LiveData} object.
     *
     * @return the current {@link Article} as LiveData.
     */
    LiveData<Article> getArticle() {
        return observableArticle;
    }

    /**
     * Tells the {@link ArticleRepository} to set the read field of this {@link Article} to true.
     *
     * @param articleID id of {@link Article} that should be marked as read.
     */
    void setRead(long articleID) {
        articleRepository.setRead(articleID);
    }

    /**
     * The factory is used, to inject the currently shown article into the viewmodel.
     * It sets the application and articleId.
     */
    static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final Application application;
        private final long articleId;

        /**
         * Constructor for the factory, initializing it.
         *
         * @param application the application instance.
         * @param articleId   the currently shown article id.
         */
        Factory(@NonNull Application application, long articleId) {
            this.application = application;
            this.articleId = articleId;
        }

        /**
         * Creates a new instance of the {@link ArticleViewModel}, using the given variables.
         *
         * @param modelClass class to be created.
         * @param <T> type of the class.
         * @return new ViewModel created by factory.
         */
        @NotNull
        @Override
        public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
            // noinspection unchecked
            return (T) new ArticleViewModel(application, articleId);
        }
    }

    /**
     * Calls {@link ArticleRepository} to invert the save state of the given {@link Article}.
     * Sets true to false and false to true.
     *
     * @param article Article to update.
     */
    void toggleSaveArticle(Article article) {
        articleRepository.setSavedAndTryToDelete(article, !article.saved);
    }
}
