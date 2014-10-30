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
 * Created by Rachel on 10/22/14.
 */
public class TaskContactDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,MySQLiteHelper.COLUMN_PHONE_NUMBER, MySQLiteHelper.COLUMN_CONTACT_NAME, MySQLiteHelper.COLUMN_TASK_ID};

    public TaskContactDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public TaskContact createTaskContact(String phoneNumber,String name, long taskId) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(MySQLiteHelper.COLUMN_CONTACT_NAME, name);
        values.put(MySQLiteHelper.COLUMN_TASK_ID, taskId);
        long insertId = database.insert(MySQLiteHelper.TABLE_TASK_CONTACT, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK_CONTACT,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        TaskContact newTask = cursorToTaskContact(cursor);
        cursor.close();
        Log.d("TaskContactDataSource", "TaskContact created with Taskid: "+taskId+"and phoneNumber: "+phoneNumber);
        return newTask;
    }


    public void deleteTaskContact(TaskContact taskContact) {
        long id = taskContact.getId();
        System.out.println("TaskContact deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_TASK_CONTACT, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<TaskContact> getAllTasks() {
        List<TaskContact> tasks = new ArrayList<TaskContact>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK_CONTACT,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TaskContact task = cursorToTaskContact(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return tasks;
    }

    public List<TaskContact> getTaskContactsByTaskId(long taskId){
        List<TaskContact> tasks = new ArrayList<TaskContact>();

        Cursor cursor = database.rawQuery("select * from "+MySQLiteHelper.TABLE_TASK_CONTACT+" where "+MySQLiteHelper.COLUMN_TASK_ID+"="+taskId+"", null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TaskContact task = cursorToTaskContact(cursor);
            tasks.add(task);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return tasks;
    }

    private TaskContact cursorToTaskContact(Cursor cursor) {
        TaskContact taskContact = new TaskContact();
        taskContact.setId(cursor.getLong(0));
        taskContact.setPhoneNumber(cursor.getString(1));
        taskContact.setName(cursor.getString(2));
        taskContact.setTaskID(cursor.getLong(3));
        return taskContact;
    }
}
