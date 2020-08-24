package de.datenkraken.datenkrake.surveillance.sender;

import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apollographql.apollo.api.Mutation;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import de.datenkraken.datenkrake.SubmitLocationCoordinatesMutation;
import de.datenkraken.datenkrake.network.ITask;
import de.datenkraken.datenkrake.network.clients.apollo.ApolloMutation;
import de.datenkraken.datenkrake.surveillance.ISendProcessedData;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.util.FormatUtil;
import de.datenkraken.datenkrake.type.CreateLocationCoordinates;
import de.datenkraken.datenkrake.type.LocationProviderType;
import de.datenkraken.datenkrake.util.Callback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class GPSLocationSender implements ISendProcessedData {

    public GPSLocationSender() {
        Timber.tag("GPS Sender");
    }

    @Nullable
    @Override
    public ITask getTask(List<ProcessedDataPacket> packets, Callback callback) {

        List<CreateLocationCoordinates> list = new ArrayList<>();
        for (ProcessedDataPacket packet : packets) {
            LocationProviderType providerType;
            switch (packet.getString("provider", "")) {
                case LocationManager.GPS_PROVIDER:
                    providerType = LocationProviderType.GPS;
                    break;
                case LocationManager.NETWORK_PROVIDER:
                    providerType = LocationProviderType.NETWORK;
                    break;
                default: providerType =
                    LocationProviderType.$UNKNOWN;
                break;
            }

            list.add(CreateLocationCoordinates.builder()
                    .timestamp(FormatUtil.formatDate(new Date(packet.getLong("timestamp", 0L))))
                    .altitude(packet.getDouble("altitude", 0.0))
                    .longitude(packet.getDouble("longitude", 0.0))
                    .latitude(packet.getDouble("latitude", 0.0))
                    .accuracy(packet.getFloat("accuracy", 0f))
                    .provider(providerType)
                    .build());
        }

        SubmitLocationCoordinatesMutation mutation = SubmitLocationCoordinatesMutation.builder()
                                                        .list(list)
                                                        .build();

        return new ApolloMutation<SubmitLocationCoordinatesMutation.Data>() {

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
        return SubmitLocationCoordinatesMutation.OPERATION_ID;
    }
}
