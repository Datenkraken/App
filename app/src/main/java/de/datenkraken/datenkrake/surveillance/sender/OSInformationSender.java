package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.SubmitOSInformationMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.CreateOSInformation;
import de.datenkraken.datenkrake.util.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class OSInformationSender implements ISendProcessedData {
    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {
        List<CreateOSInformation> list = new ArrayList<>();

        for (ProcessedDataPacket packet : packets) {

            list.add(CreateOSInformation.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .sdk(packet.getInteger("sdk",  -1))
                .device(packet.getString("device", ""))
                .model(packet.getString("model", ""))
                .vendor(packet.getString("vendor", ""))
                .serial(packet.getString("serial", ""))
                .build());
        }

        SubmitOSInformationMutation mutation = SubmitOSInformationMutation.builder()
            .list(list)
            .build();
        Timber.d("OS Information sent!");

        return new ApolloMutation<SubmitOSInformationMutation.Data>() {
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
        return SubmitOSInformationMutation.OPERATION_ID;
    }
}
