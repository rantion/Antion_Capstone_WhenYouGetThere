package com.example.rachel.wygt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Parcel;
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
    protected static Map<LatLong, Long> locations;
    protected String fileLocation;
    protected static TaskDataSource dataSource;

    private static Context context;

    public void onCreate() {
        MyApplication.context = getApplicationContext();
        gpsTracker = new GPSTracker();
        try {
            dataSource = new TaskDataSource(context);
            dataSource.open();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        super.onCreate();


    }

    public static Map<LatLong, Long> getLocations() {
        return locations;
    }

    public void setLocations(Map<LatLong, Long> locations) {
        this.locations = locations;
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
        Log.d("MyApplication","My Application Destroyed");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = openFileOutput(fileLocation, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(locations);
            objectOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onTerminate();
    }
}
