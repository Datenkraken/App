package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.ArticleActionMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.actions.ArticleAction;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.ArticleEventType;
import de.datenkraken.datenkrake.type.CreateArticleEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jetbrains.annotations.Nullable;

/**
 * Sending the information processed by
 * {@link de.datenkraken.datenkrake.surveillance.processors.event.ArticleActionProcessor}.
 * @author Julian Wagner - julian.wagner@stud.tu-darmstadt.de
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ArticleActionSender implements ISendProcessedData {

    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {

        List<CreateArticleEvent> articleEvents = new ArrayList<>();
        for (ProcessedDataPacket packet : packets) {

            ArticleAction action = packet.getObject(ArticleAction.class,
                "action",
                ArticleAction.$UNKNOWN);

            articleEvents.add(CreateArticleEvent.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .type(ArticleEventType.safeValueOf(action.getValue()))
                .title(packet.getString("title", ""))
                .url(packet.getString("url", ""))
                .build());
        }

        ArticleActionMutation mutation = ArticleActionMutation.builder()
            .list(articleEvents)
            .build();

        return new ApolloMutation<ArticleActionMutation.Data>() {
            @Override
            public Mutation getMutation() {
                return mutation;
            }

            @Override
            public void onFailure(@NonNull ApolloException e) {
                super.onFailure(e);
                callback.onFailure();
            }

            @Override
            public void onResponse(@NonNull Response response) {
                super.onResponse(response);
                callback.onSuccess();
            }
        };
    }

    @Override
    public String getTaskId() {
        return ArticleActionMutation.OPERATION_ID;
    }
}

