package com.example.rachel.wygt;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Rachel on 10/8/14.
 */
public class LogHelper {

    static final String _timeStampFormat = "yyyy-MM-dd'T'HH:mm:ss";
    static final String get_timeStampZoneId = "UTC";


    public static String yOrN(int value){
        return value != 0 ? "Y": "N";
    }

    public static String yOrN(boolean value){
        return value ? "Y": "N";
    }

    public static String translateStatus(int value){
        String message = "UNDEFINED";
        switch(value){
            case LocationProvider.AVAILABLE:
                message = "AVAILABLE";
                break;
            case LocationProvider.OUT_OF_SERVICE:
                message = "OUT_OF_SERVICE";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                message = "TEMPORARILY_UNAVAILABLE";
                break;
        }
        return message;
    }

    public static String translateAccuracyFineCourse(int value){
        String message = "UNDEFINED";

        switch(value){
            case Criteria.ACCURACY_COARSE:
                message = "COARSE";
                break;
            case Criteria.ACCURACY_FINE:
                message = "FINE";
                break;
        }
        return message;
    }

    public static String FormatLocationInfo(String provider,double lat, double lng, float accuracy, long time){
        SimpleDateFormat timeStampFormatter = new SimpleDateFormat(_timeStampFormat);
        timeStampFormatter.setTimeZone(TimeZone.getTimeZone(get_timeStampZoneId));

        String timeStamp = timeStampFormatter.format(time);
        String logMessage = String.format("%s | lat/lng=%f/%f | accuracy = %f | Time = %s",provider,lat,lng,accuracy,timeStamp);
        return logMessage;
    }

    public static String FormatLocationInfo(Location location){
        String provider = location.getProvider();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();

        return LogHelper.FormatLocationInfo(provider, latitude, longitude, accuracy, time);
    }

    public static String formatLocationProvider(Context context, LocationProvider provider){
        String name = provider.getName();
        int horizontalAccuracy = provider.getAccuracy();
        int powerRequirements = provider.getPowerRequirement();
        boolean hasMonetaryCost = provider.hasMonetaryCost();
        boolean requiresCell = provider.requiresCell();
        boolean requiresNetwork = provider.requiresNetwork();
        boolean requiresSatellite = provider.requiresSatellite();
        boolean supportsAltitude = provider.supportsAltitude();
        boolean supportsBearing = provider.supportsBearing();
        boolean supportsSpeed = provider.supportsSpeed();

        String enabledMessage = "UNKNOWN";
        if(context != null){
            LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            enabledMessage = yOrN(lm.isProviderEnabled(name));
        }

        String horizontaAccuracyDisplay = translateAccuracyFineCourse(horizontalAccuracy);
        //String powerRequirementsDisplay =translatePower(powerRequirements);

        String logMessage = String.format(name+ " | "+ " enabled: "+enabledMessage+" | horizontal accuracy: " +horizontaAccuracyDisplay+
                "| power: " +powerRequirements+" | cost: " +yOrN(hasMonetaryCost)+" | uses cell: "+yOrN(requiresCell)+" | uses network: "
                +yOrN(requiresNetwork)+" | uses satellite: "+ yOrN(requiresSatellite)+" | " +    "has altitude: "+yOrN(supportsAltitude)+
                " | has bearing: "+ yOrN(supportsBearing));

        return logMessage;
    }
}

