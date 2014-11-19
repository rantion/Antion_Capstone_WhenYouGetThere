package com.example.rachel.wygt;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Rachel on 10/24/14.
 */
public class GpsTrackerAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = "GpsTrackerAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
      //  Toast.makeText(context,"Alarm Received",Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context, GPSTracker.class));
        Log.d(TAG,"start service intent sent to LocationService");
    }
}