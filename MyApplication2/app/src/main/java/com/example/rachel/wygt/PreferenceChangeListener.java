package com.example.rachel.wygt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Rachel on 11/4/14.
 */
public class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String locationUpdate = "prefSyncFrequency";


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("PrefChange", "prefChanged");
        Log.d("PrefChange", key);
        if (key.equals(locationUpdate)) {
            cancelAlarmManager();
            startAlarmManager();
        }
    }

    private void cancelAlarmManager() {
        Log.d("PrefChange", "cancelAlarmManager");
        Context context = MyApplication.getAppContext();
        Intent gpsTrackerIntent = new Intent("com.example.wygt.alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MyApplication.REQUESTCODE, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    private void startAlarmManager() {
        Log.d("PrefChange", "startAlarmManager");
        Context context = MyApplication.getAppContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent("com.example.wygt.alarm");
        gpsTrackerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MyApplication.REQUESTCODE, gpsTrackerIntent, 0);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String interval = sharedPreferences.getString("prefSyncFrequency", "5");

        int intervalInMinutes = Integer.parseInt(interval);
        Log.d("PrefChange", "Shared Preferences interval: " + intervalInMinutes);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                intervalInMinutes * 60000, // 60000 = 1 minute
                pendingIntent);
    }
}
