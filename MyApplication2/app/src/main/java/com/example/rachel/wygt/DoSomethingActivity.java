package com.example.rachel.wygt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
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
    private TaskSoundDataSource taskSoundDataSource = MyApplication.getTaskSoundDataSource();
    private TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    private TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    private SeekBar mediaVlmSeekBar = null;
    private SeekBar ringerVlmSeekBar = null;
    private SeekBar alarmVlmSeekBar = null;
    private AudioManager audioManager = null;
    private SeekBar notifyVlmSeekBar = null;
    private int mediaMax, ringerMax, notifyMax, alarmMax, ringCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_do_something);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setVolumeControlStream(AudioManager.STREAM_RING);
        this.setVolumeControlStream(AudioManager.STREAM_ALARM);
        this.setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "appIsOpenSetTrue - DoSomethingActivity");
        editor.putBoolean("appIsOpen", true);
        editor.apply();
        Bundle extras = getIntent().getExtras();
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
        }
        hideKeyboard();
        initControls();
    }

    private Drawable getVolumeIcon(int max, int current, int type) {
        Drawable icon = null;
        float third = (max / 3);
        float twoThirds = (third * 2);
        if (current == 0) {
            if (type == SoundSettings.SOUND_TYPE_RINGER && current == 0) {
                icon = getResources().getDrawable(R.drawable.vibrate);
            } else {
                icon = getResources().getDrawable(R.drawable.mute);
            }
        } else if (current < third) {
            icon = getResources().getDrawable(R.drawable.volume1);
        } else if (current >= third && current <= twoThirds) {
            icon = getResources().getDrawable(R.drawable.volume2);
        } else if (current >= twoThirds) {
            icon = getResources().getDrawable(R.drawable.volume3);
        }
        return icon;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return true;
    }

    private void initControls() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaVlmSeekBar = (SeekBar) findViewById(R.id.mediaSeek);
        mediaMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int mediaCurrent = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mediaVlmSeekBar.setMax(mediaMax);
        mediaVlmSeekBar.setProgress(mediaCurrent);
        ImageView mediaIcon = (ImageView) findViewById(R.id.mediaIcon);
        mediaIcon.setImageDrawable(getVolumeIcon(mediaMax, mediaCurrent, SoundSettings.SOUND_TYPE_MEDIA));

        ringerVlmSeekBar = (SeekBar) findViewById(R.id.ringerSeek);
        ringerMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        ringCurrent = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        ringerVlmSeekBar.setMax(ringerMax);
        ringerVlmSeekBar.setProgress(ringCurrent);
        ImageView ringerIcon = (ImageView) findViewById(R.id.ringerIcon);
        ringerIcon.setImageDrawable(getVolumeIcon(ringerMax, ringCurrent, SoundSettings.SOUND_TYPE_RINGER));

        alarmVlmSeekBar = (SeekBar) findViewById(R.id.systemSeek);
        alarmMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int alarmCurrent = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        alarmVlmSeekBar.setMax(alarmMax);
        alarmVlmSeekBar.setProgress(alarmCurrent);
        ImageView alarmIcon = (ImageView) findViewById(R.id.systemIcon);
        alarmIcon.setImageDrawable(getVolumeIcon(alarmMax, alarmCurrent, SoundSettings.SOUND_TYPE_ALARM));

        notifyVlmSeekBar = (SeekBar) findViewById(R.id.notificationSeek);
        notifyMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int notifyCurrent = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        notifyVlmSeekBar.setMax(notifyMax);
        notifyVlmSeekBar.setProgress(notifyCurrent);
        ImageView notifyIcon = (ImageView) findViewById(R.id.notificationIcon);
        notifyIcon.setImageDrawable(getVolumeIcon(notifyMax, notifyCurrent, SoundSettings.SOUND_TYPE_NOTIFICATION));

        if (ringCurrent == 0) {
            ringIs0();
        }

        try {
            mediaVlmSeekBar
                    .setOnSeekBarChangeListener(mediaChangeListener);
            ringerVlmSeekBar
                    .setOnSeekBarChangeListener(ringerChangeListener);
            alarmVlmSeekBar
                    .setOnSeekBarChangeListener(alarmChangeListener);
            notifyVlmSeekBar
                    .setOnSeekBarChangeListener(notificationChangeListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SeekBar.OnSeekBarChangeListener notificationChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView notify = (ImageView) findViewById(R.id.notificationIcon);
            notify.setImageDrawable(getVolumeIcon(notifyMax, progress, SoundSettings.SOUND_TYPE_NOTIFICATION));
            //         audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, progress, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener alarmChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView alarm = (ImageView) findViewById(R.id.systemIcon);
            alarm.setImageDrawable(getVolumeIcon(alarmMax, progress, SoundSettings.SOUND_TYPE_ALARM));
            //         audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void ringIs0() {
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
        alarmVlmSeekBar.setProgress(0);
        notifyVlmSeekBar.setProgress(0);
        notifyVlmSeekBar.setEnabled(false);
        alarmVlmSeekBar.setEnabled(false);
    }

    private SeekBar.OnSeekBarChangeListener ringerChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView ringer = (ImageView) findViewById(R.id.ringerIcon);
            ringer.setImageDrawable(getVolumeIcon(ringerMax, progress, SoundSettings.SOUND_TYPE_RINGER));
            if (progress == 0) {
                ringIs0();
            } else {
                notifyVlmSeekBar.setEnabled(true);
                alarmVlmSeekBar.setEnabled(true);
            }
            //    audioManager.setStreamVolume(AudioManager.STREAM_RING, progress, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener mediaChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView media = (ImageView) findViewById(R.id.mediaIcon);
            media.setImageDrawable(getVolumeIcon(mediaMax, progress, SoundSettings.SOUND_TYPE_MEDIA));
            //       audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void thereCheckerText(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_text);
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout);
        thereL.setBackgroundColor(getResources().getColor(R.color.baby_blue_lavender));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_text);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_text_miles_minute);
        miles.setClickable(false);
    }

    public void thereCheckerCall(View view) {
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout_call);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout_call);
        thereL.setBackgroundColor(getResources().getColor(R.color.baby_blue_teal));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_call);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_call);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        miles.setClickable(false);
    }

    public void distanceCheckedText(View view) {
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_text);
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout);
        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.baby_blue_lavender));
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_text);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_text_miles_minute);
        miles.setClickable(true);
    }

    public void distanceCheckedCall(View view) {
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout_call);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout_call);
        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.baby_blue_teal));
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_call);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_call);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        miles.setClickable(true);
    }

    public void thereCheckerReminder(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_reminder);
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout_reminder);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout_reminder);
        thereL.setBackgroundColor(getResources().getColor(R.color.baby_blue_lavender));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_reminder);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        miles.setClickable(false);
    }

    public void distanceCheckedReminder(View view) {
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout_reminder);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout_reminder);
        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.baby_blue_lavender));
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_reminder);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_reminder);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        miles.setClickable(true);
    }

    public void thereCheckerSound(View view) {
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout_sound);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout_sound);
        thereL.setBackgroundColor(getResources().getColor(R.color.baby_blue_teal));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_sound);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_sound);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_sound_miles_minutes);
        miles.setClickable(false);
    }

    public void distanceCheckedSound(View view) {
        LinearLayout distanceL = (LinearLayout)findViewById(R.id.distance_chooser_layout_sound);
        LinearLayout thereL = (LinearLayout)findViewById(R.id.there_layout_sound);
        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.baby_blue_teal));
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_sound);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_sound);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_sound_miles_minutes);
        miles.setClickable(true);
    }

    public void setSound(View view) {
        long metersAway = 150;
        String radiusType = "there";
        int original =0;
        CheckBox distanceCheckbox = (CheckBox) findViewById(R.id.distance_checkbox_sound);
        Spinner milesMinutes = (Spinner) findViewById(R.id.spinner_sound_miles_minutes);
        if (distanceCheckbox.isChecked()) {
            EditText _miles = (EditText) findViewById(R.id.miles_away_sound);
            String miles = _miles.getText().toString();
            if (miles.length() == 0) {
                Toast.makeText(this, "Please Enter a Distance", Toast.LENGTH_SHORT).show();
                return;
            }
            original = Integer.parseInt(miles);
            if (milesMinutes.getSelectedItem().equals("miles")) {
                radiusType = "miles";
                metersAway = convertToMeters(Double.valueOf(miles));
            } else if (milesMinutes.getSelectedItem().equals("minutes")) {
                radiusType = "minutes";
                metersAway = getMinutesAwayRadius(Integer.parseInt(miles));
            }
            else if(milesMinutes.getSelectedItem().equals("meters")){
                radiusType = "meters";
                metersAway = Long.valueOf(miles);
            }
        }

        int media = mediaVlmSeekBar.getProgress();
        int ring = ringerVlmSeekBar.getProgress();
        int system = alarmVlmSeekBar.getProgress();
        int nofity = notifyVlmSeekBar.getProgress();
        Task task = taskDataSource.createTask(destinationLocation, "", metersAway, Task.SOUND_SETTING_TASK_TYPE, destination, radiusType, original);
        taskSoundDataSource.createSoundSettings(media, ring, nofity, system, task.getId());
        Toast.makeText(getApplicationContext(),
                "Sound Setting Created!", Toast.LENGTH_SHORT)
                .show();


    }

    public void sendTextMessage(View view) {
        long metersAway = 150;
        String radiusType = "there";
        int original = 0;
        MultiAutoCompleteTextView _contacts = (MultiAutoCompleteTextView) findViewById(R.id.multiAuto_contacts);
        EditText _reminder = (EditText) findViewById(R.id.enter_reminder_field);
        String reminder = "filler";
        CheckBox distanceCheckbox = (CheckBox) findViewById(R.id.distance_checkbox_text);
        Spinner milesMinutes = (Spinner) findViewById(R.id.spinner_text_miles_minute);
        if (distanceCheckbox.isChecked()) {
            EditText _miles = (EditText) findViewById(R.id.miles_away_text);
            String miles = _miles.getText().toString();
            if (miles.length() == 0) {
                Toast.makeText(this, "Please Enter a Distance", Toast.LENGTH_SHORT).show();
                return;
            }
            original = Integer.parseInt(miles);
            if (milesMinutes.getSelectedItem().equals("miles")) {
                metersAway = convertToMeters(Double.valueOf(miles));
                radiusType = "miles";
            } else if (milesMinutes.getSelectedItem().equals("minutes")) {
                metersAway = getMinutesAwayRadius(Integer.parseInt(miles));
                radiusType = "minutes";
            }
            else if(milesMinutes.getSelectedItem().equals("meters")){
                metersAway = Long.valueOf(miles);
                radiusType = "meters";
            }
        }
        if (_reminder != null) {
            reminder = _reminder.getText().toString();
        }
        Task task = taskDataSource.createTask(destinationLocation, reminder, metersAway, Task.TEXT_MESSAGE_TASK_TYPE, destination, radiusType, original);
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
        long metersAway = 150;
        int original = 0;
        String radiusType = "there";
        Log.d("DoSomethingActivity", "setCallReminderCalled");
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_call);
        Spinner milesMin = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        if (distance.isChecked()) {
            EditText _miles = (EditText) findViewById(R.id.miles_away_call);
            String miles = _miles.getText().toString();
            if (miles.length() == 0) {
                Toast.makeText(this, "Please Enter a Distance", Toast.LENGTH_SHORT).show();
                return;
            }
            original = Integer.parseInt(miles);
            if (milesMin.getSelectedItem().equals("miles")) {
                metersAway = convertToMeters(Double.valueOf(miles));
                radiusType = "miles";
            } else if (milesMin.getSelectedItem().equals("minutes")) {
                metersAway = getMinutesAwayRadius(Integer.parseInt(miles));
                radiusType = "minutes";
            }
            else if(milesMin.getSelectedItem().equals("meters")){
                metersAway = Long.valueOf(miles);
                radiusType = "meters";
            }
        }

        AutoCompleteTextView _contacts = (AutoCompleteTextView) findViewById(R.id.auto_contacts);
        Task task = taskDataSource.createTask(destinationLocation, "", metersAway, Task.CALL_REMINDER_TASK_TYPE, destination, radiusType, original);
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
            Toast.makeText(getApplicationContext(),
                    "Call Reminder Created!", Toast.LENGTH_SHORT)
                    .show();
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
        LinearLayout phone = (LinearLayout) findViewById(R.id.enter_contacts_call_reminder);
        if (phone.getVisibility() != View.VISIBLE) {
            phone.setVisibility(View.VISIBLE);
        } else if (phone.getVisibility() == View.VISIBLE) {
            phone.setVisibility(View.GONE);
        }
    }

    public void soundSelected(View view) {
        LinearLayout sound = (LinearLayout) findViewById(R.id.sound_layout);
        if (sound.getVisibility() != View.VISIBLE) {
            sound.setVisibility(View.VISIBLE);
        } else if (sound.getVisibility() == View.VISIBLE) {
            sound.setVisibility(View.GONE);
        }
    }

    public void textSelected(View view) {
        LinearLayout text = (LinearLayout) findViewById(R.id.enter_contacts_text);
        if (text.getVisibility() != View.VISIBLE) {
            text.setVisibility(View.VISIBLE);
        } else if (text.getVisibility() == View.VISIBLE) {
            text.setVisibility(View.GONE);
        }
    }

    public void reminderSelected(View view) {
        LinearLayout reminder = (LinearLayout) findViewById(R.id.reminder_layout);
        if (reminder.getVisibility() != View.VISIBLE) {
            reminder.setVisibility(View.VISIBLE);
        } else if (reminder.getVisibility() == View.VISIBLE) {
            reminder.setVisibility(View.GONE);
        }
    }


    public long convertToMeters(double miles) {
        long meters = (long) (miles * 1609.34);
        return meters;
    }

    public void setReminder(View view) {
        long metersAway = 150;
        int original = 0;
        String radiusType = "there";
        Log.d("DoSomethingActivity", "setReminderCalled");
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_reminder);
        Spinner milesMin = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        EditText _reminder = (EditText) findViewById(R.id.reminder_edit_text);
        String reminder = "";
        if (distance.isChecked()) {
            EditText _miles = (EditText) findViewById(R.id.miles_away_reminder);
            String miles = _miles.getText().toString();
            if (miles.length() == 0) {
                Toast.makeText(this, "Please Enter a Distance", Toast.LENGTH_SHORT).show();
                return;
            }
            original = Integer.parseInt(miles);
            if (milesMin.getSelectedItem().equals("miles")) {
                metersAway = convertToMeters(Double.valueOf(miles));
                radiusType = "miles";
            } else if (milesMin.getSelectedItem().equals("minutes")) {
                metersAway = getMinutesAwayRadius(Integer.parseInt(miles));
                radiusType = "minutes";
            }
            else if(milesMin.getSelectedItem().equals("meters")){
                metersAway = Long.valueOf(miles);
                radiusType = "meters";
            }
        }
        if (_reminder != null) {
            reminder = _reminder.getText().toString();
        }
        if (metersAway < 100) {
            Toast.makeText(this, "Please enter a greater radius", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(getApplicationContext(),
                "Reminder Created!", Toast.LENGTH_SHORT)
                .show();
        taskDataSource.createTask(destinationLocation, reminder, metersAway, Task.REMINDER_MESSAGE_TASK_TYPE, destination, radiusType, original);
        Log.d("CreateTaskActivity", "Saved Destination");

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
