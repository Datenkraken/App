package de.datenkraken.datenkrake.db;

import androidx.room.Embedded;
import androidx.room.Relation;

import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.model.Source;

import java.util.List;


/**
 * Class defining the relation from one {@link Article} to its {@link Source}. <br>
 * An article can have only one source.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class ArticleToSourceRelation extends ModelRelation {

    @Embedded
    Article article;

    @Relation(parentColumn = "source_id", entityColumn = "uid")
    List<Source> source;

    /**
     * Sets the source reference in {@link Article} to reference the {@link Source} in this relation,
     * if {@link ArticleToSourceRelation#source} is not empty. <br>
     * Uses the first item in the list of sources, because an article can have only one source.
     */
    @Override
    void reference() {
        if (!source.isEmpty()) {
            article.source = source.get(0);
        }
    }
}
