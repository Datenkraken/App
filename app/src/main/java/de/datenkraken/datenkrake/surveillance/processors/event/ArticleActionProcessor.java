package de.datenkraken.datenkrake.surveillance.processors.event;

import de.datenkraken.datenkrake.ArticleActionMutation;
import de.datenkraken.datenkrake.db.AppDatabase;
import de.datenkraken.datenkrake.model.Article;
import de.datenkraken.datenkrake.surveillance.DataCollectionEvent;
import de.datenkraken.datenkrake.surveillance.DataCollectionEventType;
import de.datenkraken.datenkrake.surveillance.IEventProcessor;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.actions.ArticleAction;

import java.lang.ref.WeakReference;

import kotlin.Triple;

/**
 * Collects and process the events raised by the interactions with the articles.
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ArticleActionProcessor implements IEventProcessor {

    @Override
    public DataCollectionEventType[] canProcess() {
        return new DataCollectionEventType[] {
            DataCollectionEventType.ARTICLEACTION,
            DataCollectionEventType.ARTICLEIDACTION
        };
    }

    @Override
    public void process(DataCollectionEvent event, ProcessedDataCollector collector) {
        ArticleAction action;
        String title;
        String url;
        if (event.type == DataCollectionEventType.ARTICLEIDACTION) {
            // we only have the id of article, so we need to request the article from our db.
            Triple triple = (Triple) event.content.get();
            action = (ArticleAction) triple.getFirst();
            AppDatabase db = (AppDatabase) ((WeakReference) triple.getThird()).get();

            if (db == null) {
                return;
            }

            Article article = db.daoArticle().getOneArticleByArticleUidSync((Long) triple.getSecond());

            if (article == null) {
                return;
            }

            title = article.title;
            url = article.source.url.toString();
        } else {
            Triple actionTriple = (Triple) event.content.get();

            // Load content from triple.
            action = (ArticleAction) actionTriple.getFirst();
            title = (String) actionTriple.getSecond();
            url = (String) actionTriple.getThird();
        }

        // Create new package.
        ProcessedDataPacket packet = new ProcessedDataPacket(ArticleActionMutation.OPERATION_ID);
        packet.putObject("action", action);
        packet.putLong("timestamp", event.timestamp.getTime());
        packet.putString("title", title);
        packet.putString("url", url);
        collector.addPacket(packet);
    }
}
