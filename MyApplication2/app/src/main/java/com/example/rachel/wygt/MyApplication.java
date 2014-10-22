package com.example.rachel.wygt;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Parcel;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private static Context context;

    public void onCreate() {
        MyApplication.context = getApplicationContext();
        ClassLoader classLoader = getClass().getClassLoader();
        gpsTracker = new GPSTracker();
        Intent intent = new Intent(this, ProximityService.class);
        startService(intent);
        try {
            File file = new File(context.getFilesDir(), "locations_file");
            fileLocation = file.getName();
            locations = new HashMap<LatLong, Long>();
            Location location = gpsTracker.getLocation();
//            LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
//            locations.put(new LatLong(current),(long)23);
            FileOutputStream fileOutputStream = openFileOutput(fileLocation, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(locations);
            objectOutputStream.close();

            FileInputStream fileInputStream  = openFileInput(fileLocation);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            locations = (HashMap) objectInputStream.readObject();
            objectInputStream.close();
            Iterator it = locations.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry entry = (Map.Entry)it.next();
                LatLong _location = (LatLong)entry.getKey();
                Log.d("MyApplication",_location.toString());


            }
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


    public static Context getAppContext() {
        return MyApplication.context;
    }
}
