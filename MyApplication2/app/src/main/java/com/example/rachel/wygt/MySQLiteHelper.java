package com.example.rachel.wygt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Rachel on 10/22/14.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_REMINDER_MESSAGE = "reminder_message";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_REMINDER = "reminder";

    private static final String DATABASE_NAME = "reminder_message.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String CREATE_REMINDER_MESSAGE = "create table "
            + TABLE_REMINDER_MESSAGE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_REMINDER
            + " text not null, "+COLUMN_LATITUDE +" real,"+COLUMN_LONGITUDE+" real, "+COLUMN_RADIUS+" numeric," +COLUMN_TIME+" numeric);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_REMINDER_MESSAGE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDER_MESSAGE);
        onCreate(db);
    }

}
