package com.example.rachel.wygt;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rachel on 10/20/14.
 */

public class MyApplication extends Application {

    protected static GPSTracker gpsTracker;
    protected static TaskContactDataSource taskContactDataSource;
    protected static TaskDataSource taskDataSource;
    protected static TaskSoundDataSource taskSoundDataSource;
    protected static MyLocationDataSource myLocationDataSource;
    private static Context context;
    private static ArrayList<Map<String, String>> mPeopleList;

    public void onCreate() {
        MyApplication.context = getApplicationContext();
        try {
            taskDataSource = new TaskDataSource(context);
            taskDataSource.open();
            taskContactDataSource = new TaskContactDataSource(context);
            taskContactDataSource.open();
            taskSoundDataSource = new TaskSoundDataSource(context);
            taskSoundDataSource.open();
            myLocationDataSource = new MyLocationDataSource(context);
            myLocationDataSource.open();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        gpsTracker = new GPSTracker();
        gpsTracker.registerReceivers();
        (new PopulateContacts()).execute();
//        SharedPreferences sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Log.d("MyApplication", "appIsOpenSetToTrue - MyApplication");
//        editor.putBoolean("appIsOpen", true);
        super.onCreate();
    }

    public static GPSTracker getGpsTracker() {
        return gpsTracker;
    }

    public static TaskSoundDataSource getTaskSoundDataSource() {
        return taskSoundDataSource;
    }

    public static void setTaskSoundDataSource(TaskSoundDataSource taskSoundDataSource) {
        MyApplication.taskSoundDataSource = taskSoundDataSource;
    }

    public static TaskDataSource getTaskDataSource() {
        return taskDataSource;
    }

    public static TaskContactDataSource getTaskContactDataSource() {
        return taskContactDataSource;
    }

    public static MyLocationDataSource getMyLocationDataSource() {
        return myLocationDataSource;
    }

    public static void setMyLocationDataSource(MyLocationDataSource myLocationDataSource) {
        MyApplication.myLocationDataSource = myLocationDataSource;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    private class PopulateContacts extends AsyncTask<Void, Void, ArrayList<Map<String, String>>> {

        private ArrayList<Map<String, String>> mPeopleList = new ArrayList<Map<String, String>>();

        @Override
        protected ArrayList<Map<String, String>> doInBackground(Void... params) {
            Thread.currentThread().setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            mPeopleList.clear();
            Cursor people = getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (people.moveToNext()) {
                String contactName = people.getString(people
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId = people.getString(people
                        .getColumnIndex(ContactsContract.Contacts._ID));
                String hasPhone = people
                        .getString(people
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if ((Integer.parseInt(hasPhone) > 0)) {
                    // You know have the number so now query it like this
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                            null, null);
                    while (phones.moveToNext()) {
                        //store numbers and display a dialog letting the user select which.
                        String phoneNumber = phones.getString(
                                phones.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER)
                        );
                        String numberType = phones.getString(phones.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.TYPE));
                        Map<String, String> NamePhoneType = new HashMap<String, String>();
                        NamePhoneType.put("Name", contactName);
                        NamePhoneType.put("Phone", phoneNumber);
                        if (numberType.equals("0"))
                            NamePhoneType.put("Type", "Work");
                        else if (numberType.equals("1"))
                            NamePhoneType.put("Type", "Home");
                        else if (numberType.equals("2"))
                            NamePhoneType.put("Type", "Mobile");
                        else
                            NamePhoneType.put("Type", "Other");
                        //Then add this map to the list.
                        mPeopleList.add(NamePhoneType);
                    }
                    phones.close();
                }
            }
            people.close();
            return mPeopleList;
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, String>> maps) {
            MyApplication.this.mPeopleList = maps;
        }
    }

    public static ArrayList<Map<String, String>> getmPeopleList() {
        return mPeopleList;
    }

    public void setmPeopleList(ArrayList<Map<String, String>> mPeopleList) {
        this.mPeopleList = mPeopleList;
    }
}
