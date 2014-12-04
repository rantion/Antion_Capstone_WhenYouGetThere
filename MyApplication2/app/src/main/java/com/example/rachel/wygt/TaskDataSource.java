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
 * Created by Rachel on 10/28/14.
 */
public class TaskDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_REMINDER , MySQLiteHelper.COLUMN_LATITUDE, MySQLiteHelper.COLUMN_LONGITUDE,
            MySQLiteHelper.COLUMN_ADDRESS, MySQLiteHelper.COLUMN_RADIUS,MySQLiteHelper.COLUMN_RADIUS_TYPE,
            MySQLiteHelper.COLUMN_TIME,MySQLiteHelper.COLUMN_ORIGINAL_RADIUS_VALUE, MySQLiteHelper.COLUMN_TASK_TYPE,

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

    public Task createTask(LatLng location, String reminder, long radius, int taskType, String address, String radius_type, int original, int active) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_REMINDER, reminder);
        values.put(MySQLiteHelper.COLUMN_LATITUDE, location.latitude);
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, location.longitude);
        values.put(MySQLiteHelper.COLUMN_RADIUS, radius);
        values.put(MySQLiteHelper.COLUMN_TIME, System.currentTimeMillis());
        values.put(MySQLiteHelper.COLUMN_TASK_TYPE, taskType);
        values.put(MySQLiteHelper.COLUMN_ADDRESS,address);
        values.put(MySQLiteHelper.COLUMN_RADIUS_TYPE, radius_type);
        values.put(MySQLiteHelper.COLUMN_ORIGINAL_RADIUS_VALUE, original);
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

    public void updateTask(Task task){
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_REMINDER, task.getReminder());
        values.put(MySQLiteHelper.COLUMN_LATITUDE, task.getLatitude());
        values.put(MySQLiteHelper.COLUMN_LONGITUDE, task.getLongitude());
        values.put(MySQLiteHelper.COLUMN_RADIUS, task.getRadius());
        values.put(MySQLiteHelper.COLUMN_TIME, System.currentTimeMillis());
        values.put(MySQLiteHelper.COLUMN_TASK_TYPE, task.getTaskType());
        values.put(MySQLiteHelper.COLUMN_ADDRESS,task.getAddress());
        values.put(MySQLiteHelper.COLUMN_RADIUS_TYPE, task.getRadius_type());
        values.put(MySQLiteHelper.COLUMN_ORIGINAL_RADIUS_VALUE, task.getOriginalRadius());
        database.update(MySQLiteHelper.TABLE_TASK, values, MySQLiteHelper.COLUMN_ID+"="+task.getId(), null);

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

    public Task getTaskById(long id){
        Task task = null;
        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_TASK+" where "+
                MySQLiteHelper.COLUMN_ID+"="+id+"", null);
        if(cursor!=null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                 task = cursorToTask(cursor);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        }
        return task;
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

    public List<Task> getSoundTasks(){
        List<Task> tasks = new ArrayList<Task>();
        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_TASK+" where "+
                MySQLiteHelper.COLUMN_TASK_TYPE+"="+Task.SOUND_SETTING_TASK_TYPE+"", null);
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
        task.setRadius(cursor.getLong(5));
        task.setTime(cursor.getLong(7));
        task.setTaskType(cursor.getInt(9));
        task.setAddress(cursor.getString(4));
        task.setRadius_type(cursor.getString(6));
        task.setOriginalRadius(cursor.getInt(8));
        return task;
    }
}
