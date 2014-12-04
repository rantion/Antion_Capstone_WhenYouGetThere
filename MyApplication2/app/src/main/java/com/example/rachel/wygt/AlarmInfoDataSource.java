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
public class AlarmInfoDataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_ALARM_TYPE, MySQLiteHelper.COLUMN_MONDAY,
            MySQLiteHelper.COLUMN_TUESDAY, MySQLiteHelper.COLUMN_WEDNESDAY, MySQLiteHelper.COLUMN_THURSDAY,
            MySQLiteHelper.COLUMN_FRIDAY, MySQLiteHelper.COLUMN_SATURDAY, MySQLiteHelper.COLUMN_SUNDAY,MySQLiteHelper.COLUMN_TASK_ID};

    public AlarmInfoDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public AlarmInfo createAlarmInfo(AlarmInfo info) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ALARM_TYPE, info.getAlarmType());
        values.put(MySQLiteHelper.COLUMN_MONDAY, info.getMon());
        values.put(MySQLiteHelper.COLUMN_TUESDAY, info.getTue());
        values.put(MySQLiteHelper.COLUMN_WEDNESDAY, info.getWed());
        values.put(MySQLiteHelper.COLUMN_THURSDAY, info.getThu());
        values.put(MySQLiteHelper.COLUMN_FRIDAY, info.getFri());
        values.put(MySQLiteHelper.COLUMN_SATURDAY, info.getSat());
        values.put(MySQLiteHelper.COLUMN_SUNDAY, info.getSun());
        values.put(MySQLiteHelper.COLUMN_TASK_ID, info.getTaskId());
        long insertId = database.insert(MySQLiteHelper.TABLE_ALARM_INFO, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ALARM_INFO,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        AlarmInfo newInfo = cursorToAlarmInfo(cursor);
        cursor.close();
        Log.d("AlarmInfoDataSource", "AlarmInfo created");
        return newInfo;
    }


    public void deleteAlarmInfo(AlarmInfo info) {
        long id = info.getId();
        System.out.println("AlarmInfo deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_ALARM_INFO, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<AlarmInfo> getAllAlarms() {
        List<AlarmInfo> infos = new ArrayList<AlarmInfo>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_ALARM_INFO,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            AlarmInfo info = cursorToAlarmInfo(cursor);
            infos.add(info);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return infos;
    }

    public List<AlarmInfo> getAlarmInfoByTaskId(long taskId) {
        List<AlarmInfo> infos = new ArrayList<AlarmInfo>();
        Cursor cursor = database.rawQuery("select * from " + MySQLiteHelper.TABLE_ALARM_INFO +
                " where " + MySQLiteHelper.COLUMN_TASK_ID + "=" + taskId + "", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            AlarmInfo info = cursorToAlarmInfo(cursor);
            infos.add(info);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return infos;
    }

    private AlarmInfo cursorToAlarmInfo(Cursor cursor) {
        AlarmInfo info = new AlarmInfo();
        info.setId(cursor.getLong(0));
        info.setAlarmType(cursor.getInt(1));
        info.setMon(cursor.getInt(2));
        info.setTue(cursor.getInt(3));
        info.setWed(cursor.getInt(4));
        info.setThu(cursor.getInt(5));
        info.setFri(cursor.getInt(6));
        info.setSat(cursor.getInt(7));
        info.setSun(cursor.getInt(8));
        info.setTaskId(cursor.getLong(9));
        return info;
    }
}
