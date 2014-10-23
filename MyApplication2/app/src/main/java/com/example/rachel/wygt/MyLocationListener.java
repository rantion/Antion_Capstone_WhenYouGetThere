package com.example.rachel.wygt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Rachel on 10/8/14.
 */
public class MyLocationListener implements LocationListener {

    final String _logTag = "MonitorLocation";
    private static final int NOTIFICATION_ID = 1000;
    private TaskDataSource dataSource = MyApplication.getDataSource();

    public MyLocationListener() {
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
                Context context = MyApplication.getAppContext();
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, null, 0);
                Notification notification = createNotification();

                notification.setLatestEventInfo(context,
                        "BD NOTIFICATION IS NOTIFYING YOU", null, pendingIntent);
                notificationManager.notify(NOTIFICATION_ID, notification);
                dataSource.deleteTask(task);

            }
        }


//        Map<LatLong, Long> locations = MyApplication.getLocations();
//        Iterator it = locations.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry entry = (Map.Entry) it.next();
//            LatLong _location = (LatLong) entry.getKey();
//            long radius = (Long) entry.getValue();
//            float[] results = new float[4];
//            Location.distanceBetween(_location.getLatitude(), _location.getLongitude(), location.getLatitude(), location.getLongitude(), results);
//            if (results[0] < (float)radius) {
//                Context context = MyApplication.getAppContext();
//                NotificationManager notificationManager =
//                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, null, 0);
//                Notification notification = createNotification();
//
//                notification.setLatestEventInfo(context,
//                        "YO NOTIFICATION IS NOTIFYING YOU", null, pendingIntent);
////                notificationManager.notify(NOTIFICATION_ID, notification);
////                locations.remove(_location);
//            }
//        }

        String logMessage = LogHelper.FormatLocationInfo(provider, latitude, longitude, accuracy, time);
        Log.d(_logTag, "Monitor Location: " + logMessage);

//         Toast.makeText(MyApplication.getAppContext(),logMessage, Toast.LENGTH_LONG).show();

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
        Log.d(_logTag, "Monitor Location - providerEnabled" + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(_logTag, "Monitor Location - providerDisabled" + provider);
    }
}

