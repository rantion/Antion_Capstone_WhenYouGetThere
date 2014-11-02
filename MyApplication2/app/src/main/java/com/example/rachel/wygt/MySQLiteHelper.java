package com.example.rachel.wygt;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Rachel on 10/22/14.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    //table names
    public static final String TABLE_TASK = "task";
    public static final String TABLE_TASK_CONTACT = "task_contact";
    public static final String TABLE_TASK_LIST_ITEM = "task_list_item";
   //task table column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_REMINDER = "reminder";
    public static final String COLUMN_TASK_TYPE = "task_type";
    //task_contact table column names
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_CONTACT_NAME = "contact_name";
    //task_list_item table column names
    public static final String COLUMN_LIST_ITEM = "list_item";
    public static final String COLUMN_LIST_INDEX = "list_index";


    private static final String TASK_DB = "task.db";
    private static final int DATABASE_VERSION = 4;

    // Database creation sql statement
    private static final String CREATE_TASK_DB = "create table "
            + TABLE_TASK + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_REMINDER
            + " text not null, "+COLUMN_LATITUDE +" real,"+COLUMN_LONGITUDE+" real, "+COLUMN_RADIUS+" numeric," +COLUMN_TIME+" numeric,"+COLUMN_TASK_TYPE+" integer);";

    private static final String CREATE_TASK_CONTACT_DB ="create table " +TABLE_TASK_CONTACT+"("+ COLUMN_ID
            + " integer primary key autoincrement, " +COLUMN_PHONE_NUMBER+" text not null, "+COLUMN_CONTACT_NAME+" text not null, "+ COLUMN_TASK_ID + " integer,"
            + " FOREIGN KEY ("+COLUMN_TASK_ID+") REFERENCES "+COLUMN_ID+" ("+TABLE_TASK+"));";

    private static final String CREATE_TASK_LIST_ITEM_DB ="create table " +TABLE_TASK_LIST_ITEM+"("+ COLUMN_ID
            + " integer primary key autoincrement, " +COLUMN_LIST_ITEM+" text not null, "+COLUMN_LIST_INDEX+" integer, "+ COLUMN_TASK_ID + " integer,"
            + " FOREIGN KEY ("+COLUMN_TASK_ID+") REFERENCES "+COLUMN_ID+" ("+TABLE_TASK+"));";

    public MySQLiteHelper(Context context) {
        super(context, TASK_DB, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TASK_CONTACT_DB);
        database.execSQL(CREATE_TASK_DB);
        database.execSQL(CREATE_TASK_LIST_ITEM_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_CONTACT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_LIST_ITEM);
        onCreate(db);
    }

}
