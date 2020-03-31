package de.datenkraken.datenkrake;

import android.content.Context;
import android.content.res.Resources;

import com.rometools.rome.io.FeedException;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.network.TaskDistributor;
import de.datenkraken.datenkrake.controller.feedupdater.rss.OkHttpFeed;
import de.datenkraken.datenkrake.controller.feedupdater.rss.UnsupportedVersionException;
import de.datenkraken.datenkrake.util.Helper;
import okhttp3.Call;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import timber.log.Timber;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadFeedUnitTest {

    // Rule to pipe Timber output to system.out
    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();

    @Test
    public void testDownloadFeedParse() {
        Source source = new Source();
        MockWebServer server = new MockWebServer();

        Context mockApplicationContext = mock(Context.class);
        Resources mockContextResources = mock(Resources.class);

        when(mockApplicationContext.getResources()).thenReturn(mockContextResources);
        when(mockContextResources.getString(anyInt())).thenReturn(server.url("/").toString());
        TaskDistributor.setup(mockApplicationContext);
        try {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource("sampledata/FeedResponses/RSS0.91.txt")));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource("sampledata/FeedResponses/RSS1.0.txt")));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource("sampledata/FeedResponses/RSS2.0.txt")));
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource("sampledata/FeedResponses/ATOM1.0.txt")));
        } catch (IOException e) {
            assert false;
        }
        Article article = new Article();
        article.title = "Title";
        article.description = "Description";
        article.content = "Content";
        article.source = source;
        try {
            article.link = new URL("https://www.test.de/");
        } catch (MalformedURLException e) {
            article.link = null;
        }
        try {
            article.publishedDate = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss Z", Locale.US).parse("Sun, 01 Dec 2019 15:05:00 +0100");
        } catch (ParseException pe) {
            Timber.e(pe,"parse error");
        }

        source.url = server.url("rssfeeds").url();
        article.uid = Helper.generateArticleUid(article);
        testOkHttpFeed feed = new testOkHttpFeed(source);

        feed.source = source;
        feed.request(); // RSS 0.91
        assert (feed.articles.size() == 1);
        assert feed.articles.get(0).equals(article);

        article.author = "Daniel Thoma";
        article.uid = Helper.generateArticleUid(article);
        feed.request(); // RSS 1.0
        assert (feed.articles.size() == 1);
        assert feed.articles.get(0).equals(article);

        article.author = null;
        article.uid = Helper.generateArticleUid(article);
        feed.request(); // RSS 2.0
        assert (feed.articles.size() == 1);
        assert feed.articles.get(0).equals(article);

        article.title = "Datenkrake frisst Menschen";
        article.author = "Daniel Thoma";
        article.content = null;
        article.uid = Helper.generateArticleUid(article);
        feed.request(); // ATOM1.0
        assert (feed.articles.size() == 1);
        assert feed.articles.get(0).equals(article);
    }

    public class testOkHttpFeed extends OkHttpFeed {
        List<Article> articles;

        public testOkHttpFeed(Source source) {
            super(source);
        }

        @Override
        public void onSuccessfulParsed(@NotNull List<Article> articles) {
            this.articles = articles;
        }

        @Override
        public void onFeedException(@NotNull FeedException e) {
            assert false;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            assert false;
        }

        @Override
        public void request() {
            TaskDistributor.getInstance().synchronousRequest(this);
        }
    }

    @Test
    public void testFeedNotSupported()  {
        MockWebServer server = new MockWebServer();
        String path = "sampledata/FeedResponses/OPML1.0.txt";

        Context mockApplicationContext = mock(Context.class);
        Resources mockContextResources = mock(Resources.class);

        when(mockApplicationContext.getResources()).thenReturn(mockContextResources);
        when(mockContextResources.getString(anyInt())).thenReturn(server.url("/").toString());
        TaskDistributor.setup(mockApplicationContext);

        try {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource(path)));
        } catch (IOException e) {
            assert false;
        }

        Source source = new Source();
        source.url = server.url(path).url();

        new OkHttpFeed(source) {
            @Override
            public void onSuccessfulParsed(@NotNull List<Article> articles) {
                assert false;
            }

            @Override
            public void onFeedException(@NotNull FeedException e) {
                assert e.getClass().equals(UnsupportedVersionException.class);
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                assert false;
            }
        }.request();
    }

    @Test
    public void testFeedCorrupted() {
        MockWebServer server = new MockWebServer();

        Context mockApplicationContext = mock(Context.class);
        Resources mockContextResources = mock(Resources.class);

        when(mockApplicationContext.getResources()).thenReturn(mockContextResources);
        when(mockContextResources.getString(anyInt())).thenReturn(server.url("/").toString());
        TaskDistributor.setup(mockApplicationContext);

        try {
            server.enqueue(new MockResponse().setResponseCode(200).setBody(TestUtils.getResource("sampledata/FeedResponses/corrupted.txt")));
        } catch (IOException e) {
            assert false;
        }

        Source source = new Source();
        source.url = server.url("rssfeeds").url();

        new OkHttpFeed(source) {
            @Override
            public void onSuccessfulParsed(@NotNull List<Article> articles) {
                assert false;
            }

            @Override
            public void onFeedException(@NotNull FeedException e) {
                assert true;
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                assert false;
            }

            @Override
            public void request() {
                TaskDistributor.getInstance().synchronousRequest(this);
            }
        }.request();
    }

    @Test
    public void testSiteExisting()  {

        Context mockApplicationContext = mock(Context.class);
        Resources mockContextResources = mock(Resources.class);

        when(mockApplicationContext.getResources()).thenReturn(mockContextResources);
        when(mockContextResources.getString(anyInt())).thenReturn("");
        TaskDistributor.setup(mockApplicationContext);

        Source source = new Source();
        try {
            source.url = new URL("http://ksdlafkloajsdfolkjad.com");
        } catch (MalformedURLException e) {
            Timber.e("Malformed URL!");
        }
        new OkHttpFeed(source) {
            @Override
            public void onSuccessfulParsed(@NotNull List<Article> articles) {
                assert false;
            }

            @Override
            public void onFeedException(@NotNull FeedException e) {
                assert false;
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                assert true;
            }

            @Override
            public void request() {
                TaskDistributor.getInstance().synchronousRequest(this);
            }
        }.request();
    }
}
