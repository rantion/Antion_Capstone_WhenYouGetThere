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
    public static final String TABLE_TASK_SOUND = "task_sound";
    public static final String TABLE_MY_LOCATIONS = "my_locations";
    public static final String TABLE_ALARM_INFO = "alarm_info";
   //task table column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_RADIUS_TYPE = "radius_type";
    public static final String COLUMN_REMINDER = "reminder";
    public static final String COLUMN_TASK_TYPE = "task_type";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ORIGINAL_RADIUS_VALUE="radius_value";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    //task_contact table column names
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_CONTACT_NAME = "contact_name";
    //task_list_item table column names
    public static final String COLUMN_LIST_ITEM = "list_item";
    public static final String COLUMN_LIST_INDEX = "list_index";
    //task_sound table column names
    public static final String COLUMN_MEDIA_LVL = "media_lvl";
    public static final String COLUMN_RINGER_LVL = "ringer_lvl";
    public static final String COLUMN_SYSTEM_LVL = "system_lvl";
    public static final String COLUMN_NOTIFICATION_LVL = "notification_lvl";
    //my_location table column names
    public static final String COLUMN_MY_LOCATION_NAME = "location_name";
    //alarmInfo table column names
    public static final String COLUMN_ALARM_TYPE = "alarm_type";
    public static final String COLUMN_MONDAY = "monday";
    public static final String COLUMN_TUESDAY = "tuesday";
    public static final String COLUMN_WEDNESDAY = "wednesday";
    public static final String COLUMN_THURSDAY = "thursday";
    public static final String COLUMN_FRIDAY = "friday";
    public static final String COLUMN_SATURDAY = "saturday";
    public static final String COLUMN_SUNDAY = "sunday";



    private static final String TASK_DB = "task.db";
    private static final int DATABASE_VERSION = 14;

    // Database creation sql statement
    private static final String CREATE_TASK_DB = "create table "
            + TABLE_TASK + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_REMINDER
            + " text, "+COLUMN_LATITUDE +" real,"+COLUMN_LONGITUDE+" real, "+COLUMN_ADDRESS+" text, "+COLUMN_RADIUS+
            " numeric,"+COLUMN_RADIUS_TYPE+" text, " +COLUMN_TIME+" numeric,"+COLUMN_ORIGINAL_RADIUS_VALUE+" integer, "
            +COLUMN_TASK_TYPE+" integer, "+COLUMN_IS_ACTIVE+" integer );";

    private static final String CREATE_TASK_CONTACT_DB ="create table " +TABLE_TASK_CONTACT+"("+ COLUMN_ID
            + " integer primary key autoincrement, " +COLUMN_PHONE_NUMBER+" text not null, "+COLUMN_CONTACT_NAME
            +" text not null, "+ COLUMN_TASK_ID + " integer,"
            + " FOREIGN KEY ("+COLUMN_TASK_ID+") REFERENCES "+COLUMN_ID+" ("+TABLE_TASK+"));";

    private static final String CREATE_TASK_LIST_ITEM_DB ="create table " +TABLE_TASK_LIST_ITEM+"("+ COLUMN_ID
            + " integer primary key autoincrement, " +COLUMN_LIST_ITEM+" text not null, "+COLUMN_LIST_INDEX+
            " integer, "+ COLUMN_TASK_ID + " integer,"
            + " FOREIGN KEY ("+COLUMN_TASK_ID+") REFERENCES "+COLUMN_ID+" ("+TABLE_TASK+"));";

    private static final String CREATE_TASK_SOUND_DB = "create table " +TABLE_TASK_SOUND+"("+ COLUMN_ID
            + " integer primary key autoincrement, " +COLUMN_MEDIA_LVL+" integer, "+COLUMN_RINGER_LVL+" integer, "+
            COLUMN_SYSTEM_LVL+" integer, "+COLUMN_NOTIFICATION_LVL+" integer, "+ COLUMN_TASK_ID + " integer,"
            + " FOREIGN KEY ("+COLUMN_TASK_ID+") REFERENCES "+COLUMN_ID+" ("+TABLE_TASK+"));";

    private static final String CREATE_MY_LOCATION_DB = "create table "+ TABLE_MY_LOCATIONS+"("+COLUMN_ID+"" +
            " integer primary key autoincrement, "+COLUMN_MY_LOCATION_NAME+" text, "+COLUMN_ADDRESS+" text, "+
            COLUMN_LATITUDE+" real, "+COLUMN_LONGITUDE+" real);";

    private static final String CREATE_ALARM_INFO ="create table " +TABLE_ALARM_INFO+"("+ COLUMN_ID
            + " integer primary key autoincrement, " +COLUMN_ALARM_TYPE+" integer, " + COLUMN_MONDAY+" integer, "+
            COLUMN_TUESDAY+ " integer, "+COLUMN_WEDNESDAY+" integer, "+COLUMN_THURSDAY + " integer, "+ COLUMN_FRIDAY+
            "integer, "+ COLUMN_SATURDAY+ " integer, "+COLUMN_SUNDAY+" integer,  "
            + " FOREIGN KEY ("+COLUMN_TASK_ID+") REFERENCES "+COLUMN_ID+" ("+TABLE_TASK+"));";

    public MySQLiteHelper(Context context) {
        super(context, TASK_DB, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TASK_CONTACT_DB);
        database.execSQL(CREATE_TASK_DB);
        database.execSQL(CREATE_TASK_LIST_ITEM_DB);
        database.execSQL(CREATE_TASK_SOUND_DB);
        database.execSQL(CREATE_MY_LOCATION_DB);
        database.execSQL(CREATE_ALARM_INFO);
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_SOUND);
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_MY_LOCATIONS);
        onCreate(db);
    }

}
