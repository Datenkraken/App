package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.SubmitBluetoothDeviceScanMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.CreateBluetoothDeviceScan;
import de.datenkraken.datenkrake.util.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BluetoothDeviceScanSender implements ISendProcessedData {
    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {
        List<CreateBluetoothDeviceScan> list = new ArrayList<>();

        for (ProcessedDataPacket packet : packets) {

            list.add(CreateBluetoothDeviceScan.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .name(packet.getString("name", ""))
                .address(packet.getString("address", ""))
                .known(packet.getBoolean("known", false))
                .build());
        }

        SubmitBluetoothDeviceScanMutation mutation = SubmitBluetoothDeviceScanMutation.builder()
            .list(list)
            .build();

        return new ApolloMutation<SubmitBluetoothDeviceScanMutation.Data>() {
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
        return SubmitBluetoothDeviceScanMutation.OPERATION_ID;
    }
}
