package de.datenkraken.datenkrake;

import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

import de.datenkraken.datenkrake.util.Helper;
import timber.log.Timber;

/**
 * Instrumented test, which will execute on an Android device.
 * Tests the Database.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseUnitTest {

    private AppDatabase db;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void init() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class)
            .allowMainThreadQueries()
            .build();
        Timber.plant(new Timber.DebugTree());
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndLoadArticle() {
        Source source = getRandomSource();
        List<Article> articles = this.getRandomArticles(20, source);
        db.daoSource().insertOneSourceSync(source);

        assertNotEquals(0, source.uid);

        db.daoArticle().insertOrUpdateAllArticleSync(articles);

        db.daoArticle().getAllArticleAsync().observeForever(observedArticles -> {
            if (observedArticles != null) {
                assertEquals(articles.size(), observedArticles.size());
                for (Article article : articles) {
                    assertTrue(observedArticles.contains(article));
                }
            }
        });
    }

    @Test
    public void singleQuerySourceReferences() {
        Source source = getRandomSource();
        List<Article> articles = this.getRandomArticles(20, source);

        db.daoSource().insertOneSourceSync(source);
        db.daoArticle().insertOrUpdateAllArticleSync(articles);

        db.daoArticle().getAllArticleAsync().observeForever(observedArticles -> {
            if (observedArticles != null) {
                Article current = observedArticles.get(0);
                for (int i = 1; i < observedArticles.size(); i++) {
                    assertSame(current.source, observedArticles.get(i).source);
                }
            }
        });
    }

    @Test
    public void updateSource() {
        Source source = getRandomSource();
        db.daoSource().insertOneSourceSync(source);

        source.name = TestUtils.generateRandomString(30);
        db.daoSource().updateOneSourceSync(source);

        db.daoSource().getOneSourceByIdAsync(source.uid).observeForever(observedSource -> {
            if (observedSource != null) {
                assertEquals(source, observedSource);
            }
        });
    }

    @Test
    public void deletes() {
        Source source = getRandomSource();
        Source source1 = getRandomSource();

        List<Article> articles = getRandomArticles(10, source);
        List<Article> articles1 = getRandomArticles(5, source1);

        db.daoSource().insertOneSourceSync(source);
        db.daoSource().insertOneSourceSync(source1);
        db.daoArticle().insertOrUpdateAllArticleSync(articles);
        db.daoArticle().insertOrUpdateAllArticleSync(articles1);


        db.daoArticle().getAllArticleAsync().observeForever(new Observer<List<Article>>() {
            int counter = 0;

             @Override
             public void onChanged(List<Article> articles) {
                 if (articles == null) {
                     return;
                 }

                 switch (counter) {
                     case 0:
                         assertEquals(15, articles.size());
                         break;
                     case 1:
                         assertEquals(5, articles.size());
                        break;
                     case 2:
                         assertEquals(0, articles.size());
                         break;
                 }
                counter++;
             }
        });


        db.daoArticle().deleteAllArticleBySourceUidSync(source.uid);
        db.daoArticle().deleteAllArticleCachedSync();

        db.daoSource().getAllSourceNotMarkedAsync().observeForever(new Observer<List<Source>>() {
            int counter = 0;

            @Override
            public void onChanged(List<Source> sources) {
                if (sources == null) {
                    return;
                }

                switch (counter) {
                    case 0:
                        assertEquals(2, sources.size());
                        break;
                    case 1:
                        assertEquals(1, sources.size());
                        break;
                    case 2:
                        assertEquals(0, sources.size());
                        break;
                }
                counter++;
            }
        });

        db.daoSource().updateOneSourceMarkToDeleteSync(source);
        db.daoSource().updateOneSourceMarkToDeleteSync(source1);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void foreignKeyConstraints() {
        Source source = getRandomSource();
        List<Article> articles = getRandomArticles(10, source);
        db.daoArticle().insertOrUpdateAllArticleSync(articles);
    }

    @Test()
    public void singleArticle() {
        Source source = getRandomSource();
        List<Article> articles = getRandomArticles(20, source);
        db.daoSource().insertOneSourceSync(source);
        db.daoArticle().insertOrUpdateAllArticleSync(articles);

        LiveData<Article> dbArticle = db.daoArticle().getOneArticleByArticleUidAsync(articles.get(15).uid);
        dbArticle.observeForever(article -> {
            if(article != null) {
                assertEquals(articles.get(15), article);
            } else {
                fail();
            }
        });
    }

    private List<Article> getRandomArticles(int number, Source source) {
        Article[] articles = new Article[number];
        for(int i = 0; i < number; i++) {
            articles[i] = getRandomArticle(source);
        }
        return Arrays.asList(articles);
    }

    private Article getRandomArticle(Source source) {
        Random r = new Random();
        Article article = new Article();
        article.title = TestUtils.generateRandomString(30);
        article.imageUrl = Uri.parse(TestUtils.generateRandomLink(30));
        article.author = TestUtils.generateRandomString(10);
        article.publishedDate = new Date(r.nextLong());
        article.source = source;
        article.description = TestUtils.generateRandomString(100);
        article.content = TestUtils.generateRandomString(200);
        try {
            article.link = new URL(TestUtils.generateRandomLink(40));
        } catch (MalformedURLException e) {
            article.link = null;
            Timber.d(e, "generated flawed url for article link");
        }
        article.uid = Helper.generateArticleUid(article);
        return article;
    }

    private Source getRandomSource() {
        Source source = new Source();
        source.name =  TestUtils.generateRandomString(10);
        try {
            source.url = new URL(TestUtils.generateRandomLink(20));
        } catch (MalformedURLException e) {
            source.url = null;
            Timber.d(e, "generated flawed url for source");
        }
        source.deleted = false;
        source.uid = Helper.generateSourceUid(source);
        return source;
    }
}
