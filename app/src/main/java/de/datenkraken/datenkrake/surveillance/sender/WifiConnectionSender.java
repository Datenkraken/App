package de.datenkraken.datenkrake.surveillance.sender;

import androidx.annotation.NonNull;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.SubmitWifiDataMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.Callback;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.CreateWifiData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Implements {@link ISendProcessedData} to parse {@link ProcessedDataPacket}s into an {@link ITask}.
 *
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
public class WifiConnectionSender implements ISendProcessedData {

    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {

        List<CreateWifiData> createWifiDataList = new ArrayList<>();
        for (ProcessedDataPacket packet : packets) {
            createWifiDataList.add(CreateWifiData.builder()
                .timestamp(FormatUtil.formatDate(new Date(packet.getLong("time", 0L))))
                .ssid(packet.getString("SSID", ""))
                .bssid(packet.getString("BSSID", ""))
                .rssi(packet.getInteger("RSSI", 0))
                .build());
        }

        SubmitWifiDataMutation mutation = SubmitWifiDataMutation.builder()
            .list(createWifiDataList)
            .build();

        return new ApolloMutation<SubmitWifiDataMutation.Data>() {
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
        return SubmitWifiDataMutation.OPERATION_ID;
    }
}
