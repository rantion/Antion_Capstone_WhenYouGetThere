package com.example.rachel.wygt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rachel on 10/28/14.
 */
public class TaskDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_REMINDER , MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE, MySQLiteHelper.COLUMN_RADIUS,
            MySQLiteHelper.COLUMN_TIME, MySQLiteHelper.COLUMN_TASK_TYPE
    };

    public TaskDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Task createTask(LatLng location, String reminder, long radius, int taskType) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_REMINDER, reminder);
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.longitude);
        values.put(MySQLiteHelper.COLUMN_RADIUS, radius);
        values.put(MySQLiteHelper.COLUMN_TIME, System.currentTimeMillis());
        values.put(MySQLiteHelper.COLUMN_TASK_TYPE, taskType);
        long insertId = database.insert(MySQLiteHelper.TABLE_TASK, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Task newTask = cursorToTask(cursor);
        cursor.close();
        Log.d("TaskDataSource", "Task created with id: " + newTask.getId());
        return newTask;
    }

    public void deleteTask(Task task) {
        long id = task.getId();
        System.out.println("Task deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_TASK, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<Task>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Task task = cursorToTask(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return tasks;
    }

    public List<Task> getTextTasks(){
        List<Task> tasks = new ArrayList<Task>();
        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_TASK+" where "+
                MySQLiteHelper.COLUMN_TASK_TYPE+"="+Task.TEXT_MESSAGE_TASK_TYPE+"", null);
        if(cursor!=null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                tasks.add(task);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        }
        return tasks;
    }

    public List<Task> getCallReminderTasks(){
        List<Task> tasks = new ArrayList<Task>();
        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_TASK+" where "+
                MySQLiteHelper.COLUMN_TASK_TYPE+"="+Task.CALL_REMINDER_TASK_TYPE+"", null);

        if(cursor!= null){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                tasks.add(task);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        }

        return tasks;
    }

    public List<Task> getReminderMessageTasks(){
        List<Task> tasks = new ArrayList<Task>();
        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_TASK+" where "+
                MySQLiteHelper.COLUMN_TASK_TYPE+"="+Task.REMINDER_MESSAGE_TASK_TYPE+"", null);
        if(cursor!=null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Task task = cursorToTask(cursor);
                tasks.add(task);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        }
        return tasks;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getLong(0));
        task.setReminder(cursor.getString(1));
        task.setLatitude(cursor.getDouble(2));
        task.setLongitude(cursor.getDouble(3));
        task.setRadius(cursor.getLong(4));
        task.setTime(cursor.getLong(5));
        task.setTaskType(cursor.getInt(6));
        return task;
    }
}
