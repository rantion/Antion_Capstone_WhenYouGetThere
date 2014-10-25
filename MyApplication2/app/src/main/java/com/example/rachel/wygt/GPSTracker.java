package com.example.rachel.wygt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
* Created by Rachel on 10/17/14.
*/
public class GPSTracker extends Service implements LocationListener {

//    private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    protected LocationManager locationManager;
    private static final int NOTIFICATION_ID = 1000;
    private TaskDataSource dataSource = MyApplication.getDataSource();

    public GPSTracker(){
     Log.d("GPSTracker","GPS Tracker created");
     getLocation();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        getLocation();
    }

    public LocationManager getLocationManager(){
        return locationManager;
    }

    public Location getLocation() {
          //  Log.d("GPS TRACKER","getLocation called");
        try {
            locationManager = (LocationManager) MyApplication.getAppContext()
                    .getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                AlertUserDialog dialog = new AlertUserDialog("Please Enable Location Services", Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                Activity activity = (Activity)MyApplication.getAppContext();
                dialog.show(activity.getFragmentManager(),null);
            }
            else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
           //         Log.d("GPS Tracker", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Tracker", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }



    @Override
    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();

        List<Task> values  = dataSource.getAllTasks();
        for(Task task : values){
            float[] results = new float[4];
            long radius = task.getRadius();
            Location.distanceBetween(task.getLatitude(), task.getLongitude(), location.getLatitude(), location.getLongitude(), results);
            if (results[0] < (float)radius) {
                int requestID = (int) System.currentTimeMillis();
                Intent intent = new Intent(MyApplication.getAppContext(),ReminderActivity.class);
                intent.putExtra("Reminder", task.getReminder());
                intent.putExtra("id", task.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


                Context context = MyApplication.getAppContext();
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = createNotification();
                notification.setLatestEventInfo(context,
                        "BD NOTIFICATION IS NOTIFYING YOU", null, pendingIntent);
                notificationManager.notify(NOTIFICATION_ID, notification);
                dataSource.deleteTask(task);
                Log.d("reminder", "you should see that shit");

            }
        }

        String logMessage = LogHelper.FormatLocationInfo(provider, latitude, longitude, accuracy, time);
        Log.d("GPS TRACKER", "Monitor Location: " + logMessage);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        boolean appIsOpen = sharedPreferences.getBoolean("appIsOpen", true);

        if(!appIsOpen && location.getAccuracy()<500f){
            locationManager.removeUpdates(this);
            Log.d("GPSTracker", "locationManager removing updates");
        }
    }


    private Notification createNotification() {
        Notification notification = new Notification();
        notification.icon = R.drawable.ic_launcher;
        notification.when = System.currentTimeMillis();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.ledARGB = Color.RED;
        notification.ledOnMS = 1500;
        notification.ledOffMS = 1500;
        return notification;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
