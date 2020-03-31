package de.datenkraken.datenkrake.controller.feedupdater.rss;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;
import de.datenkraken.datenkrake.network.clients.okhttp.OkHttpTask;
import de.datenkraken.datenkrake.network.util.HTMLUtil;
import de.datenkraken.datenkrake.util.Helper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import timber.log.Timber;

/**
 * Class containing necessary functionality to request and parse a RSSFeed. <br>
 * This class should be used by instantiating an anonymous subclass and defining
 * the required functions. To start or cancel this task call request() or cancel().
 * This class can be extended to perform more specialized tasks.
 * When request() is called, this instance will be executed by the
 * {@link de.datenkraken.datenkrake.network.TaskDistributor}.
 * Provides functionalities to parse {@link SyndFeed}s and build and handle {@link Request}s.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @version 1.0
 * @since 06.12.2019
 */
public abstract class OkHttpFeed extends OkHttpTask {

    public Source source;

    /**
     * Gets called when the downloaded feed could be parsed into an {@link Article} list.
     *
     * @param articles list of {@link Article}s.
     */
    public abstract void onSuccessfulParsed(@NotNull List<Article> articles);

    /**
     * Gets called when this {@link de.datenkraken.datenkrake.network.ITask} was not able to parse
     * the Response.
     *
     * @param e {@link FeedException} to be displayed.
     */
    public abstract void onFeedException(@NotNull FeedException e);

    /**
     * Constructor of this class.
     * Sets a Timber tag.
     */
    private OkHttpFeed() {
        Timber.tag("OkHttpFeed");
    }

    /**
     * Constructor of this class.
     * Calls the default constructor and sets a {@link Source}.
     *
     * @param source {@link Source} to be set.
     */
    public OkHttpFeed(Source source) {
        this();
        this.source = source;
    }
    /**
     * Creates a request used by the {@link de.datenkraken.datenkrake.network.TaskDistributor} to
     * download the feed. <br>
     * Uses the {@link Source#url} as the url of the request.
     *
     * @return Request build in the function.
     */

    @Override
    public final Request getRequest() {
        return new Request.Builder()
                    .url(source.url)
                    .get()
                    .build();
    }

    /**
     * Called on the response of a request. <br>
     * Tries to parse the response in an ArrayList of type {@link Article}.
     * Will call {@link #onFeedException(FeedException)} if it failed to parse the response and displays
     * a message using timber.
     * After the response is successfully parsed, calls {@link #parseSource(SyndFeed, Source)} and
     * {@link #onSuccessfulParsed(List)} with the created {@link SyndFeed} and the source.
     *
     * @param call {@link Call} of the request. Not used here.
     * @param response {@link Response} of the request to be parsed.
     * @throws IOException if no {@link Response} is given, will lead to a call of
     * {@link #onFailure(Call, IOException)}.
     */
    @Override
    public final void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        SyndFeed feed;

        try (ResponseBody responseBody = response.body()) {
            if (responseBody == null) {
                Timber.e("No Body to convert from %s", source.url);
                onFeedException(new FeedException("Response didn't contained a body to parse."));
                return;
            }

            feed = new SyndFeedInput().build(new XmlReader(responseBody.byteStream()));
        } catch (FeedException e) {
            Timber.e(e, "Could not convert rss-feed from %s", source.url);
            onFeedException(e);
            return;
        }  catch (IllegalArgumentException e) {
            Timber.e(e, "Unable to convert body from %s, Wrong RSS Version", source.url);
            onFeedException(new UnsupportedVersionException("Unable to convert this RSS Version"));
            return;
        }

        parseSource(feed, source);
        onSuccessfulParsed(parseArticles(feed));
    }

    /**
     * Extracts information from the given {@link SyndFeed} about its {@link Source} and updates
     * the given {@link Source#name} with the title of the feed.
     *
     * @param feed {@link SyndFeed} to get the title.
     * @param source {@link Source} to update the {@link Source#name}.
     */
    private void parseSource(SyndFeed feed, Source source) {
        source.name = feed.getTitle();
    }

    /**
     * Parses given {@link SyndFeed} to  an {@link Article} list. <br>
     * Extracts informations, such as title, author, published date, description, link, content and
     * image from each entry of the feed and creates an article object. <br>
     * The image gets extracted form the content or the description, using
     * {@link #extractImageAndRemoveTracker(String, Article)} and the resulting string is set as
     * content or description. <br>
     * These objects gets collected and returned as a list. <br>
     * Standardizes the articles using {@link #standardizeArticle(Article)}.
     *
     * @param feed {@link SyndFeed} to be used to get the {@link Article}s.
     * @return List of {@link Article}s from the feed.
     */
    private List<Article> parseArticles(SyndFeed feed) {
        List<SyndEntry> syndEntries = feed.getEntries();
        List<Article> articles = new ArrayList<>();
        Article article;

        for (SyndEntry entry : syndEntries) {
            article = new Article();
            article.title = entry.getTitle();

            article.author = entry.getAuthor();
            // image urls are embedded in the content part, not trivial to extract them
            article.source = this.source;
            article.publishedDate = entry.getPublishedDate();
            if (entry.getDescription() != null) {
                String articleDescription = entry.getDescription().getValue();
                article.description = extractImageAndRemoveTracker(articleDescription, article);
            }

            try {
                article.link = new URL(entry.getLink());
            } catch (MalformedURLException e) {
                article.link = null;
            }

            if (entry.getContents() != null && !entry.getContents().isEmpty()) {
                StringBuilder content = new StringBuilder();
                for (SyndContent syndContent : entry.getContents()) {
                    content.append(syndContent.getValue()).append('\n');
                }
                // removes the last '\n'
                String articleContent = content.substring(0, content.length() - 1);
                article.content = extractImageAndRemoveTracker(articleContent, article);
            }

            standardizeArticle(article);
            articles.add(article);
        }
        return articles;
    }

    /**
     * Standardizes a {@link Article} by replacing empty Strings with null. <br>
     * Uses {@link Helper#generateArticleUid(Article)} to generate the uid for this article. <br>
     * Sets {@link Article#updated} to the current date.
     *
     * @param article Article to be standardized
     */
    private void standardizeArticle(Article article) {
        if (article.title != null && article.title.isEmpty()) {
            article.title = null;
        }

        if (article.author != null && article.author.isEmpty()) {
            article.author = null;
        }

        if (article.description != null && article.description.isEmpty()) {
            article.description = null;
        }

        if (article.content != null && article.content.isEmpty()) {
            article.content = null;
        }

        article.uid = Helper.generateArticleUid(article);
        article.updated = new Date();
    }



    /**
     * Parses the given content to HTML. Removes all image tracker and tries to extract a
     * front page image from the rest of images. <br>
     * The first image found is used as the front page image. <br>
     * The found front page image is removed from the given content of the {@link Article}. <br>
     * If it doesn't find any image tags in the parsed content, it will return the original content
     * so non HTML content will stay the same. <br>
     * Uses {@link HTMLUtil#removeImageTracker(Elements, int)} to remove trackers from the content
     * and {@link HTMLUtil#getTitleImage(Elements)} to get the image.
     *
     * @param content to be parsed for the image.
     * @param article for the image to be loaded as {@link Article#imageUrl}.
     * @return String with the image removed. Returns content, if no image was found.
     */
    private String extractImageAndRemoveTracker(String content, @NonNull Article article) {

        if (content == null) {
            return null;
        }

        // parsing to HTML objects
        Document document = Jsoup.parse(content);
        Elements imageElements = document.getElementsByTag("img");

        // If there are no images, just return the original content
        if (imageElements == null || imageElements.isEmpty()) {
            return content;
        }

        HTMLUtil.removeImageTracker(imageElements, 2);

        Uri imageSource = HTMLUtil.getTitleImage(imageElements);
        if (imageSource != null) {
            article.imageUrl = imageSource;
        }

        return document.toString();
    }
}
