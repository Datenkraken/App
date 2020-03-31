package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.ApplicatonActionMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.actions.ApplicationAction;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.AppEventType;
import de.datenkraken.datenkrake.type.CreateAppEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sending the information processed by
 * {@link de.datenkraken.datenkrake.surveillance.processors.event.ApplicationActionProcessor}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class ApplicationActionSender implements ISendProcessedData {

    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {

        List<CreateAppEvent> appEvents = new ArrayList<>();
        for (ProcessedDataPacket packet : packets) {

            ApplicationAction action = packet.getObject(ApplicationAction.class,
                "action",
                ApplicationAction.$UNKNOWN);

            appEvents.add(CreateAppEvent.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .type(AppEventType.safeValueOf(action.getValue()))
                .build());
        }

        ApplicatonActionMutation mutation = ApplicatonActionMutation.builder()
            .list(appEvents)
            .build();

        return new ApolloMutation<ApplicatonActionMutation.Data>() {
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
        return ApplicatonActionMutation.OPERATION_ID;
    }
}
