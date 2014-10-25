package com.example.rachel.wygt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Rachel on 10/20/14.
 */

public class MyApplication extends Application {

    protected static GPSTracker gpsTracker;
    protected static TaskDataSource dataSource;
    private static Context context;

    public void onCreate() {
        MyApplication.context = getApplicationContext();
        try {
            dataSource = new TaskDataSource(context);
            dataSource.open();
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

    public static TaskDataSource getDataSource(){
        return dataSource;
    }


    public static Context getAppContext() {
        return MyApplication.context;
    }



    @Override
    public void onTerminate() {
        Log.d("MyApplication", "My Application Destroyed");
        dataSource.close();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("MyApplication", "appIsOpenSetToFalse");
        editor.putBoolean("appIsOpen", false);
        super.onTerminate();
    }
}
