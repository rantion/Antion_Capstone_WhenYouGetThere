package com.example.rachel.wygt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rachel on 11/10/14.
 */
public class MyLocationDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_MY_LOCATION_NAME, MySQLiteHelper.COLUMN_ADDRESS,
            MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE};

    public MyLocationDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public MyLocation createMyLocation(String name, String address, double lat, double _long) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MY_LOCATION_NAME, name);
        values.put(MySQLiteHelper.COLUMN_ADDRESS, address);
        values.put(MySQLiteHelper.COLUMN_LATITUDE, lat);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, _long);
        long insertId = database.insert(MySQLiteHelper.TABLE_MY_LOCATIONS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MY_LOCATIONS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        MyLocation newLocation = cursorToMyLocation(cursor);
        cursor.close();
        Log.d("MyLocationDataSource", "MyLocation created with id: ");
        return newLocation;
    }


    public void deleteMyLocation(MyLocation location) {
        long id = location.getId();
        System.out.println("MyLocation deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_MY_LOCATIONS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<MyLocation> getAllMyLocations() {
        List<MyLocation> locations = new ArrayList<MyLocation>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_MY_LOCATIONS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            MyLocation location = cursorToMyLocation(cursor);
            locations.add(location);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        if(locations.equals(null)){
            Log.d("MyLocationDataSource", "is null");
        }
        return locations;
    }

    public MyLocation getLocationById(long id){
        MyLocation location = null;
        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_MY_LOCATIONS+" where "+
                MySQLiteHelper.COLUMN_ID+"="+id+"", null);
        if(cursor!=null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                location = cursorToMyLocation(cursor);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        }
        return location;
    }

    public void updateMyLocation(MyLocation location){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MY_LOCATION_NAME, location.getName());
        values.put(MySQLiteHelper.COLUMN_ADDRESS, location.getAddress());
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.getLatitude());
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.getLongitude());
        database.update(MySQLiteHelper.TABLE_MY_LOCATIONS, values, MySQLiteHelper.COLUMN_ID+"="+location.getId(), null);

    }


    private MyLocation cursorToMyLocation(Cursor cursor) {
        MyLocation myLocation = new MyLocation();
        myLocation.setId(cursor.getLong(0));
        myLocation.setName(cursor.getString(1));
        myLocation.setAddress(cursor.getString(2));
        myLocation.setLatitude(cursor.getDouble(3));
        myLocation.setLongitude(cursor.getDouble(4));
        return myLocation;
    }
}
