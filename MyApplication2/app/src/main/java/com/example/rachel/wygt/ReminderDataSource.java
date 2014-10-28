package com.example.rachel.wygt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rachel on 10/27/14.
 */
public class ReminderDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_REMINDER, MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE, MySQLiteHelper.COLUMN_RADIUS, MySQLiteHelper.COLUMN_TIME};

    public ReminderDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ReminderMessage createReminder(LatLng location, String reminder, long radius) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_REMINDER, reminder);
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.longitude);
        values.put(MySQLiteHelper.COLUMN_RADIUS, radius);
        values.put(MySQLiteHelper.COLUMN_TIME,System.currentTimeMillis());
        long insertId = database.insert(MySQLiteHelper.TABLE_REMINDER_MESSAGE, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_REMINDER_MESSAGE,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ReminderMessage newTask = cursorToTask(cursor);
        cursor.close();
        Log.d("ReminderDataSource", "Reminder created with id: " + newTask.getId());
        return newTask;
    }

    public void deleteTask(Task task) {
        long id = task.getId();
        System.out.println("Reminder deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_REMINDER_MESSAGE, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<ReminderMessage> getAllTasks() {
        List<ReminderMessage> tasks = new ArrayList<ReminderMessage>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_REMINDER_MESSAGE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ReminderMessage reminder = cursorToTask(cursor);
            tasks.add(reminder);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return tasks;
    }

    private ReminderMessage cursorToTask(Cursor cursor) {
        ReminderMessage task = new ReminderMessage();
        task.setId(cursor.getLong(0));
        task.setReminder(cursor.getString(1));
        task.setLatitude(cursor.getDouble(2));
        task.setLongitude(cursor.getDouble(3));
        task.setRadius(cursor.getLong(4));
        task.setTime(cursor.getLong(5));
        return task;
    }
}


