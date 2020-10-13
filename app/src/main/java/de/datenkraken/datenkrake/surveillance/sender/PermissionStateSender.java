package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.PermissionStateMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.graphqladapter.Permission;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.AppPermission;
import de.datenkraken.datenkrake.type.CreatePermissionState;
import de.datenkraken.datenkrake.util.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sending the information processed by
 * {@link de.datenkraken.datenkrake.surveillance.processors.event.PermissionStateProcessor}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class PermissionStateSender implements ISendProcessedData {

    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {

        List<CreatePermissionState> permissionStates = new ArrayList<>();
        for (ProcessedDataPacket packet : packets) {

            Permission permission = packet.getObject(Permission.class,
                "permission",
                Permission.$UNKNOWN);

            permissionStates.add(CreatePermissionState.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .permission(AppPermission.safeValueOf(permission.getValue()))
                .state(packet.getBoolean("state", false))
                .build());
        }

        PermissionStateMutation mutation = PermissionStateMutation.builder()
            .list(permissionStates)
            .build();

        return new ApolloMutation<PermissionStateMutation.Data>() {
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
        return PermissionStateMutation.OPERATION_ID;
    }
}
