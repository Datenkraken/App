package de.datenkraken.datenkrake.model;

import android.net.Uri;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import de.datenkraken.datenkrake.db.DaoArticle;

import java.net.URL;
import java.util.Date;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;


/**
 * Container Class for an standardized article. <br>
 * Required and used by Room, creating the table in the database. <br>
 * Used by {@link DaoArticle} to save and load these from and to the database. <br>
 * Provides the functionalities to create a hash and string and to check for equality to a given object.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
@Entity(tableName = "articles",
    indices = @Index(name = "index_source", value = "source_id", unique = false),
    foreignKeys = @ForeignKey(parentColumns = "uid", childColumns = "source_id",
        entity = Source.class))
public class Article {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "uid")
    public long uid = -1;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "image_url")
    public Uri imageUrl;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "published_date")
    public Date publishedDate;

    @ColumnInfo(name = "source_id")
    public Source source;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "read")
    public boolean read = false;

    @ColumnInfo(name = "updated")
    public Date updated;

    @ColumnInfo(name = "link")
    public URL link;

    @ColumnInfo(name = "saved")
    public boolean saved = false;

    /**
     * Default Constructor for an Article, initializing it.
     */
    public Article() {

    }

    /**
     * Creates a hash code out of the title, author, publishedDate, description, content, read state,
     * link and saved state of the article.
     *
     * @return the created hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, imageUrl, author, publishedDate, source, description, content, read, link, saved);
    }

    /**
     * Compares the article to a given object and returns true, if the values are equal.
     *
     * @param o object to be compared to the article.
     * @return returns true, if the above listed values are equal, else false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Article article = (Article) o;
        return Objects.equals(uid, article.uid)
            && Objects.equals(title, article.title)
            && Objects.equals(imageUrl, article.imageUrl)
            && Objects.equals(author, article.author)
            && Objects.equals(publishedDate, article.publishedDate)
            && Objects.equals(source, article.source)
            && Objects.equals(description, article.description)
            && Objects.equals(content, article.content)
            && Objects.equals(read, article.read)
            && Objects.equals(link, article.link)
            && Objects.equals(saved, article.saved);
    }

    /**
     * Creates a string out of the values of the article. <br>
     * The string is of the form Article{uid: ..., title: ..., ...}.
     *
     * @return string created from the values of the article.
     */
    @Override
    @NotNull
    public String toString() {
        return "Article{"
            + "uid: " + uid
            + ", title='" + title + "'"
            + ", imageUrl=" + imageUrl
            + ", author='" + author + "'"
            + ", publishedDate=" + publishedDate
            + ", source=" + source
            + ", description='" + description + "'"
            + ", content='" + content + "'"
            + ", read=" + read
            + ", link='" + link + "'"
            + ", saved=" + saved + "}";
    }
}
