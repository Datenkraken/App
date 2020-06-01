package de.datenkraken.datenkrake.surveillance.processors.background;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import de.datenkraken.datenkrake.SubmitLocationCoordinatesMutation;
import de.datenkraken.datenkrake.surveillance.ProcessedDataCollector;
import de.datenkraken.datenkrake.surveillance.ProcessedDataPacket;
import de.datenkraken.datenkrake.surveillance.background.IBackgroundProcessor;

import timber.log.Timber;

public class GPSLocationProcessor implements IBackgroundProcessor {

    public GPSLocationProcessor() {
        Timber.d("GPS Processor");
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
            return;
        }

        locationManager.requestLocationUpdates(provider, 5000, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    ProcessedDataPacket packet =
                        new ProcessedDataPacket(SubmitLocationCoordinatesMutation.OPERATION_ID);
                    packet.putLong("timestamp", location.getTime());
                    packet.putDouble("altitude", location.getAltitude());
                    packet.putDouble("longitude", location.getLongitude());
                    packet.putDouble("latitude", location.getLatitude());
                    packet.putFloat("accuracy", location.getAccuracy());
                    packet.putString("provider", provider);
                    collector.addPacket(packet);
                    collector.flush(); // TODO: workaround atm. because the background supervisor is unable to wait for this request to finish
                }
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                locationManager.removeUpdates(this);
            }

            @Override
            public void onProviderEnabled(String provider) {
                locationManager.removeUpdates(this);
            }

            @Override
            public void onProviderDisabled(String provider) {
                locationManager.removeUpdates(this);
            }
        }, Looper.getMainLooper());
    }

}