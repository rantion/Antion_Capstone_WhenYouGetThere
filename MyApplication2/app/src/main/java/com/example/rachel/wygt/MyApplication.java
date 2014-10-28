package com.example.rachel.wygt;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Rachel on 10/20/14.
 */

public class MyApplication extends Application {

    protected static GPSTracker gpsTracker;
    protected static ReminderDataSource reminderMessageDataSource;
    private static Context context;

    public void onCreate() {
        MyApplication.context = getApplicationContext();
        try {
            reminderMessageDataSource = new ReminderDataSource(context);
            reminderMessageDataSource.open();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        gpsTracker = new GPSTracker();
//        SharedPreferences sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Log.d("MyApplication", "appIsOpenSetToTrue - MyApplication");
//        editor.putBoolean("appIsOpen", true);
        super.onCreate();
    }

    public static GPSTracker getGpsTracker() {
        return gpsTracker;
    }

    public static ReminderDataSource getReminderMessageDataSource(){
        return reminderMessageDataSource;
    }


    public static Context getAppContext() {
        return MyApplication.context;
    }



    @Override
    public void onTerminate() {
        Log.d("MyApplication", "My Application Destroyed");
        reminderMessageDataSource.close();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("MyApplication", "appIsOpenSetToFalse");
        editor.putBoolean("appIsOpen", false);
        super.onTerminate();
    }
}
