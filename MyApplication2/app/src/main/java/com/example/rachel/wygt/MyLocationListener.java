package com.example.rachel.wygt;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Rachel on 10/8/14.
 */
public class MyLocationListener implements LocationListener {

    final String _logTag = "MonitorLocation";

    @Override
    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();

        String logMessage = LogHelper.FormatLocationInfo(provider, latitude, longitude, accuracy,time);
        Log.d(_logTag, "Monitor Location: " + logMessage);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(_logTag,"Monitor Location - providerEnabled" +provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(_logTag,"Monitor Location - providerDisabled" +provider);
    }
}

