package de.datenkraken.datenkrake.db;

import androidx.room.Embedded;
import androidx.room.Relation;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

import java.util.List;


/**
 * Class defining the relation from {@link Source} to all its {@link Article}. <br>
 * A source can have multiple articles.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class SourceToArticleRelation extends ModelRelation {

    @Embedded
    Source source;

    @Relation(parentColumn = "uid", entityColumn = "source_id")
    List<Article> articleList;

    /**
     * Sets the source reference in {@link Article} to reference the {@link Source} in this relation.
     */
    @Override
    void reference() {
        for (Article article : articleList) {
            article.source = source;
        }
    }
}
