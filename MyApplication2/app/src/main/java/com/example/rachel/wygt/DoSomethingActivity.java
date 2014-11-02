package com.example.rachel.wygt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rachel on 10/27/14.
 */
public class DoSomethingActivity extends Activity implements View.OnKeyListener {

    private LatLng destinationLocation, currentLocation;
    private String distance, duration, destination, distanceMeters, durationSeconds;
    private ArrayList<Map<String, String>> mPeopleList = MyApplication.getmPeopleList();
    private SimpleAdapter mAdapter;
    private MultiAutoCompleteTextView mTxtPhoneNo;
    private TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    private TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    private CheckBox thereCheckbox, distanceCheckbox;
    private Spinner milesMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_something);
        thereCheckbox = (CheckBox) findViewById(R.id.there_checkbox);
        milesMinutes = (Spinner) findViewById(R.id.miles_minutes_spinner);
        distanceCheckbox = (CheckBox) findViewById(R.id.distance_checkbox);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "appIsOpenSetTrue - DoSomethingActivity");
        editor.putBoolean("appIsOpen", true);
        editor.apply();
        Bundle extras = getIntent().getExtras();
        EditText _distanceEdit = (EditText) findViewById(R.id.miles_away);
        _distanceEdit.setEnabled(false);
        if (extras != null) {
            destinationLocation = (LatLng) extras.get("Destination_location");
            currentLocation = (LatLng) extras.get("Current_Location");
            distance = extras.getString("Distance");
            duration = extras.getString("Duration");
            durationSeconds = extras.getString("DurationSeconds");
            distanceMeters = extras.getString("DistanceMeters");
            (new PopulateContacts()).execute();
            destination = (String) extras.get("Destination");
            String button = (String) extras.get("Button");
            TextView _destination = (TextView) findViewById(R.id.destination_address);
            TextView _duration = (TextView) findViewById(R.id.duration);
            TextView _distance = (TextView) findViewById(R.id.distance);
            if (_destination != null) {
                _destination.setText(destination);
            }
            _duration.setText(duration);
            _distance.setText(distance);
            TextView send = (TextView) findViewById(R.id.send_blank_when);
            send.setText("Send text(s) when I am....");

        }
        hideKeyboard();
    }

    public void hideKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void thereChecker(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.miles_minutes_spinner);
        miles.setClickable(false);
    }

    public void distanceChecked(View view) {
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.miles_minutes_spinner);
        miles.setClickable(true);
    }

    public void sendTextMessage(View view) {
        long metersAway = 150;
        MultiAutoCompleteTextView _contacts = (MultiAutoCompleteTextView) findViewById(R.id.multiAuto_contacts);
        EditText _reminder = (EditText) findViewById(R.id.enter_reminder_field);
        String reminder = "filler";
        if (distanceCheckbox.isChecked()) {
            EditText _miles = (EditText) findViewById(R.id.miles_away);
            String miles = _miles.getText().toString();
            if (milesMinutes.getSelectedItem().equals("miles")) {
                metersAway = convertToMeters(Double.valueOf(miles));
            } else if (milesMinutes.getSelectedItem().equals("minutes")) {
                metersAway = getMinutesAwayRadius(Integer.parseInt(miles));
            }
        }
        if (_reminder != null) {
            reminder = _reminder.getText().toString();
        }
        Task task = taskDataSource.createTask(destinationLocation, reminder, metersAway, Task.TEXT_MESSAGE_TASK_TYPE);
        Log.d("DOSOMETHINGActivity", "Saved Destination");
        String contacts = _contacts.getText().toString();
        String[] num1 = contacts.split("<");
        for (int i = 1; i < num1.length; i++) {
            String[] num2 = num1[i].split(">");
            String name = "";
            if (i == 1) {
                name = num1[0];
            } else {
                String[] num3 = num1[i - 1].split(">");
                String[] num4 = num3[1].split(",");
                name = num4[1];
            }
            TaskContact contact = taskContactDataSource.createTaskContact(num2[0], name, task.getId());
            Log.d("DOSOMETHINGACTIVITY", contact.toString());
        }

        Toast.makeText(getApplicationContext(),
                "TextMessageTask Created!", Toast.LENGTH_SHORT)
                .show();
    }

    public long getMinutesAwayRadius(int minutes) {
        String distanceAway = distanceMeters;
        String _duration = durationSeconds;
        int meters = Integer.parseInt(distanceAway);
        int seconds = Integer.parseInt(_duration);
        double rate = (meters / seconds);
        double temp = rate * minutes;
        return (long) temp * 60;
    }

    public void setCallReminder(View view) {
        Log.d("DoSomethingActivity", "setCallReminderCalled");
        EditText _miles = (EditText) findViewById(R.id.miles_away);
        String miles = _miles.getText().toString();
        RadioButton button = (RadioButton) findViewById(R.id.radio_there);
        AutoCompleteTextView _contacts = (AutoCompleteTextView) findViewById(R.id.auto_contacts);
        double milesAway = 0.03;
        if (miles.length() > 0) {
            milesAway = Double.valueOf(miles);
        } else if (button.isChecked()) {
            milesAway = .03;
        }
        Task task = taskDataSource.createTask(destinationLocation, "", convertToMeters(milesAway), Task.CALL_REMINDER_TASK_TYPE);
        String contacts = _contacts.getText().toString();
        String[] num1 = contacts.split("<");
        for (int i = 1; i < num1.length; i++) {
            String[] num2 = num1[i].split(">");
            String name = "";
            if (i == 1) {
                name = num1[0];
            } else {
                String[] num3 = num1[i - 1].split(">");
                String[] num4 = num3[1].split(",");
                name = num4[1];
            }
            TaskContact contact = taskContactDataSource.createTaskContact(num2[0], name, task.getId());
            Log.d("DOSOMETHINGACTIVITY", contact.toString());
        }

    }


    private class PopulateContacts extends AsyncTask<Void, Void, ArrayList<Map<String, String>>> {
        @Override
        protected ArrayList<Map<String, String>> doInBackground(Void... params) {
            return DoSomethingActivity.this.mPeopleList;
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, String>> maps) {
            final List<String> numbers = new ArrayList<String>();
            mAdapter = new SimpleAdapter(MyApplication.getAppContext(), mPeopleList, R.layout.custcontview,
                    new String[]{"Name", "Phone", "Type"}, new int[]{
                    R.id.ccontName, R.id.ccontNo, R.id.ccontType}
            );

            final AutoCompleteTextView mCallPhoneNo = (AutoCompleteTextView) findViewById(R.id.auto_contacts);
            mCallPhoneNo.setThreshold(1);
            mCallPhoneNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> map = (Map<String, String>) parent
                            .getItemAtPosition(position);
                    String name = map.get("Name");
                    String number = map.get("Phone");
                    String current = name + "<" + number + "> ";
                    mCallPhoneNo.setText(current);
                    mCallPhoneNo.setSelection(current.length());
                }
            });
            mCallPhoneNo.setAdapter(mAdapter);


            mTxtPhoneNo = (MultiAutoCompleteTextView) findViewById(R.id.multiAuto_contacts);
            mTxtPhoneNo.setThreshold(1);
            mTxtPhoneNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> map = (Map<String, String>) parent
                            .getItemAtPosition(position);
                    String name = map.get("Name");
                    String number = map.get("Phone");
                    String current = "";
                    numbers.add((" " + name + "<" + number + ">, "));
                    for (int i = 0; i < numbers.size(); i++) {
                        current = current + numbers.get(i);
                    }
                    mTxtPhoneNo.setText(current);
                    mTxtPhoneNo.setSelection(current.length());
                }
            });
            mTxtPhoneNo.setAdapter(mAdapter);
            mTxtPhoneNo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            DoSomethingActivity.this.mPeopleList = maps;
        }
    }

    public void phoneSelected(View view) {
        TextView send = (TextView) findViewById(R.id.send_blank_when);
        send.setText("Send me reminder when I am....");
        LinearLayout text = (LinearLayout) findViewById(R.id.enter_contacts_text);
        text.setVisibility(View.GONE);
        LinearLayout proximity = (LinearLayout) findViewById(R.id.proximity_information_text);
        proximity.setBackgroundColor(getResources().getColor(R.color.baby_blue_teal));
        LinearLayout phone = (LinearLayout) findViewById(R.id.enter_contacts_call_reminder);
        phone.setVisibility(View.VISIBLE);

    }

    public void textSelected(View view) {
        LinearLayout proximity = (LinearLayout) findViewById(R.id.proximity_information_text);
        proximity.setBackgroundColor(getResources().getColor(R.color.baby_blue_lavender));
        TextView send = (TextView) findViewById(R.id.send_blank_when);
        send.setText("Send text(s) when I am....");
        LinearLayout text = (LinearLayout) findViewById(R.id.enter_contacts_text);
        text.setVisibility(View.VISIBLE);
        LinearLayout phone = (LinearLayout) findViewById(R.id.enter_contacts_call_reminder);
        phone.setVisibility(View.GONE);
    }


    public long convertToMeters(double miles) {
        long meters = (long) (miles * 1609.34);
        return meters;
    }


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if (!event.isShiftPressed()) {
                Log.v("AndroidEnterKeyActivity", "Enter Key Pressed!");
                switch (view.getId()) {
                    case R.id.enter_reminder_field:
                        EditText enter = (EditText) findViewById(R.id.enter_reminder_field);
                        Toast.makeText(getApplicationContext(),
                                enter.getText().toString(), Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
                return true;
            }

        }
        return false;
    }

    @Override
    protected void onPause() {
        Log.d("GPS/APP IS OPEN", "ONSPause__DOSOMETHINGTaskActivity");
        super.onPause();
    }

    @Override
    protected void onStop() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "ONSTOP__appIsOpenSetToFalse - DoSomethingTaskActivity");
        editor.putBoolean("appIsOpen", false);
        editor.apply();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "ONDESTROY__appIsOpenSetToFalse - DOSOMETHINGActivity");
        editor.putBoolean("appIsOpen", false);
        editor.apply();
        super.onDestroy();
    }

}
