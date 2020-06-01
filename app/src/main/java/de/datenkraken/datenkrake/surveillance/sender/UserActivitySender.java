package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.UserActivityMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.Activity;
import de.datenkraken.datenkrake.type.CreateUserActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserActivitySender implements ISendProcessedData {
    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {
        List<CreateUserActivity> userActivities = new ArrayList<>();
        for (ProcessedDataPacket packet : packets) {

            Boolean userActivity = packet.getBoolean("activity", null);
            Activity activity;

            if (userActivity == null) {
                activity = Activity.$UNKNOWN;
            } else if (userActivity) {
                activity = Activity.PRESENT;
            } else {
                activity = Activity.GONE;
            }

            userActivities.add(CreateUserActivity.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .activity(activity)
                .build());
        }

        UserActivityMutation mutation = UserActivityMutation.builder()
            .list(userActivities)
            .build();

        return new ApolloMutation<UserActivityMutation.Data>() {
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
        return UserActivityMutation.OPERATION_ID;
    }
}
