package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.SourceActionMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.graphqladapter.SourceAction;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.CreateSourceEvent;
import de.datenkraken.datenkrake.type.SourceEventType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sending the information processed by
 * {@link de.datenkraken.datenkrake.surveillance.processors.event.SourceActionProcessor}.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class SourceActionSender implements ISendProcessedData {

    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {
        List<CreateSourceEvent> list = new ArrayList<>();

        for (ProcessedDataPacket packet : packets) {
            SourceAction action = packet.getObject(SourceAction.class, "action", SourceAction.$UNKNOWN);

            list.add(CreateSourceEvent.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .type(SourceEventType.safeValueOf(action.getValue()))
                .url(packet.getString("url", ""))
                .build());
        }

        SourceActionMutation mutation = SourceActionMutation.builder().list(list).build();

        return new ApolloMutation<SourceActionMutation.Data>() {
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
        return SourceActionMutation.OPERATION_ID;
    }
}
