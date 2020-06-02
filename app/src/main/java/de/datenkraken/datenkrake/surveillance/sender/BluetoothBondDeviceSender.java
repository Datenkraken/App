package de.datenkraken.datenkrake.surveillance.sender;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.datenkraken.datenkrake.SubmitBluetoothBondDeviceMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.CreateBluetoothBondDevice;

public class BluetoothBondDeviceSender implements ISendProcessedData {
    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {
        List<CreateBluetoothBondDevice> list = new ArrayList<>();

        for (ProcessedDataPacket packet : packets) {

            list.add(CreateBluetoothBondDevice.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                .name(packet.getString("name", ""))
                .address(packet.getString("address", ""))
                .build());
        }

        SubmitBluetoothBondDeviceMutation mutation = SubmitBluetoothBondDeviceMutation.builder()
            .list(list)
            .build();

        return new ApolloMutation<SubmitBluetoothBondDeviceMutation.Data>() {
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
        return SubmitBluetoothBondDeviceMutation.OPERATION_ID;
    }
}
