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
 * Created by Rachel on 11/2/14.
 */
public class TaskSoundDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_MEDIA_LVL,MySQLiteHelper.COLUMN_RINGER_LVL,
            MySQLiteHelper.COLUMN_SYSTEM_LVL, MySQLiteHelper.COLUMN_NOTIFICATION_LVL, MySQLiteHelper.COLUMN_TASK_ID};

    public TaskSoundDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public SoundSettings createSoundSettings(int media, int ring, int notify, int system, long taskId) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_RINGER_LVL, ring);
        values.put(MySQLiteHelper.COLUMN_MEDIA_LVL, media);
        values.put(MySQLiteHelper.COLUMN_SYSTEM_LVL, system);
        values.put(MySQLiteHelper.COLUMN_NOTIFICATION_LVL, notify);
        values.put(MySQLiteHelper.COLUMN_TASK_ID, taskId);
        long insertId = database.insert(MySQLiteHelper.TABLE_TASK_SOUND, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK_SOUND,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        SoundSettings newSound = cursorToSoundSettings(cursor);
        cursor.close();
        Log.d("TaskSoundDataSource", "TaskSound created with Taskid: " + taskId);
        return newSound;
    }


    public void deleteTaskSound(SoundSettings soundSettings) {
        long id = soundSettings.getId();
        System.out.println("TaskSound deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_TASK_SOUND, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<SoundSettings> getAllTasks() {
        List<SoundSettings> sounds = new ArrayList<SoundSettings>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_TASK_SOUND,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoundSettings sound = cursorToSoundSettings(cursor);
            sounds.add(sound);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return sounds;
    }

    public List<SoundSettings> getTaskSoundsByTaskId(long taskId) {
        List<SoundSettings> sounds = new ArrayList<SoundSettings>();
        Cursor cursor = database.rawQuery("select * from " + MySQLiteHelper.TABLE_TASK_SOUND +
                " where " + MySQLiteHelper.COLUMN_TASK_ID + "=" + taskId + "", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            SoundSettings sound = cursorToSoundSettings(cursor);
            sounds.add(sound);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return sounds;
    }

    private SoundSettings cursorToSoundSettings(Cursor cursor) {
        SoundSettings soundSettings = new SoundSettings();
        soundSettings.setId(cursor.getLong(0));
        soundSettings.setRinger(cursor.getInt(2));
        soundSettings.setMedia(cursor.getInt(1));
        soundSettings.setNotification(cursor.getInt(4));
        soundSettings.setAlarm(cursor.getInt(3));
        soundSettings.setTaskId(cursor.getLong(5));
        return soundSettings;
    }
}
