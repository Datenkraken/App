package de.datenkraken.datenkrake.surveillance.processors.background;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import de.datenkraken.datenkrake.SubmitLocationCoordinatesMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;

import timber.log.Timber;

public class GPSLocationProcessor implements IBackgroundProcessor {

    public GPSLocationProcessor() {
        Timber.tag("GPS Processor");
    }

    @Override
    public void process(Context context, ProcessedDataCollector collector) {

        if (ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("missing permissions?");
            return;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Timber.d("location manager is null!");
            return;
        }

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        String provider;
        if (gpsEnabled) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (networkEnabled) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Timber.d("no provider?");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(provider, null, context.getMainExecutor(), new Consumer<Location>() {
                @Override
                public void accept(Location location) {
                    ProcessedDataPacket packet =
                        new ProcessedDataPacket(SubmitLocationCoordinatesMutation.OPERATION_ID);
                    packet.putLong("timestamp", location.getTime());
                    packet.putDouble("altitude", location.getAltitude());
                    packet.putDouble("longitude", location.getLongitude());
                    packet.putDouble("latitude", location.getLatitude());
                    packet.putFloat("accuracy", location.getAccuracy());
                    packet.putString("provider", provider);
                    collector.addPacket(packet);
                }
            });
        } else {
            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(@NotNull Location location) {
                    ProcessedDataPacket packet =
                        new ProcessedDataPacket(SubmitLocationCoordinatesMutation.OPERATION_ID);
                    packet.putLong("timestamp", location.getTime());
                    packet.putDouble("altitude", location.getAltitude());
                    packet.putDouble("longitude", location.getLongitude());
                    packet.putDouble("latitude", location.getLatitude());
                    packet.putFloat("accuracy", location.getAccuracy());
                    packet.putString("provider", provider);
                    collector.addPacket(packet);
                }

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(@NotNull String provider) {
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras)  {}

            }, context.getMainLooper());
        }
    }

    @Override
    public int keepAlive() {
        return 30000; // keep the Background task 30s alive to give gps time to get accurate and save it's position
    }

}
