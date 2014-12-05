package com.example.rachel.wygt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private AlarmInfoDataSource alarmInfoDataSource = MyApplication.getAlarmInfoDataSource();
    private SeekBar mediaVlmSeekBar = null;
    private SeekBar ringerVlmSeekBar = null;
    private SeekBar alarmVlmSeekBar = null;
    private AudioManager audioManager = null;
    private SeekBar notifyVlmSeekBar = null;
    private int mediaMax, ringerMax, notifyMax, alarmMax, ringCurrent;
    private LinearLayout textLayout, callLayout, reminderLayout, soundLayout;
    private GPSTracker gpsTracker = MyApplication.gpsTracker;
    private ImageView soundIcon, reminderIcon, textIcon, phoneIcon;
    private AlarmInfo textAlarm, callAlarm, remindAlarm, soundAlarm;
    private LinearLayout textDone, callDone, remindDone, soundDone;
//    private Bitmap soundI, soundGlow, textI, textGlow, reminderI, reminderGlow, phoneI, phoneGlow;
private Drawable soundI, soundGlow, textI, textGlow, reminderI, reminderGlow, phoneI, phoneGlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_do_something);
        (new loadImages()).execute();
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
        textAlarm = new AlarmInfo();
        callAlarm = new AlarmInfo();
        remindAlarm = new AlarmInfo();
        soundAlarm = new AlarmInfo();

        hideKeyboard();
        initControls();
        initViews();
    }

    private void initViews() {
        textLayout = (LinearLayout) findViewById(R.id.enter_contacts_text);
        callLayout = (LinearLayout) findViewById(R.id.enter_contacts_call_reminder);
        reminderLayout = (LinearLayout) findViewById(R.id.reminder_layout);
        soundLayout = (LinearLayout) findViewById(R.id.sound_layout);

        textDone = (LinearLayout)findViewById(R.id.do_something_button_text);
        callDone = (LinearLayout)findViewById(R.id.do_something_button_call);
        remindDone = (LinearLayout) findViewById(R.id.do_something_button_reminder);
        soundDone = (LinearLayout)findViewById(R.id.do_something_button_sound);

        thereCheckerCall(callLayout);
        thereCheckerReminder(reminderLayout);
        thereCheckerSound(soundLayout);
        thereCheckerText(textLayout);
        Spinner call = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        Spinner remind = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        Spinner sound = (Spinner) findViewById(R.id.spinner_sound_miles_minutes);
        Spinner text = (Spinner) findViewById(R.id.spinner_text_miles_minutes);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.miles_minutes, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        call.setAdapter(adapter);
        remind.setAdapter(adapter);
        sound.setAdapter(adapter);
        text.setAdapter(adapter);

        textIcon = (ImageView) findViewById(R.id.set_text_icon);
        reminderIcon = (ImageView) findViewById(R.id.set_reminder_icon);
        soundIcon = (ImageView) findViewById(R.id.set_sound_icon);
        phoneIcon = (ImageView) findViewById(R.id.set_phone_icon);
        initSizes();
    }

    private void initSizes(){
        LinearLayout destination = (LinearLayout) findViewById(R.id.do_something_destination);
        LinearLayout taskDrawer = (LinearLayout)findViewById(R.id.task_drawer);

        LinearLayout proxText = (LinearLayout) findViewById(R.id.proximity_information_text);
        LinearLayout contactsText = (LinearLayout) findViewById(R.id.do_something_enter_contacts);
        LinearLayout messageText = (LinearLayout) findViewById(R.id.do_something_message_text);
        LinearLayout buttonText = (LinearLayout) findViewById(R.id.do_something_button_text);

        LinearLayout proxCall = (LinearLayout)findViewById(R.id.proximity_information_call);
        LinearLayout contactCall = (LinearLayout)findViewById(R.id.do_something_contact_call);
        LinearLayout buttonCall = (LinearLayout)findViewById(R.id.do_something_button_call);

        LinearLayout proxReminder = (LinearLayout)findViewById(R.id.proximity_information_reminder);
        LinearLayout messageReminder = (LinearLayout)findViewById(R.id.do_something_message_reminder);
        LinearLayout buttonReminder = (LinearLayout)findViewById(R.id.do_something_button_reminder);

        LinearLayout proxSound = (LinearLayout)findViewById(R.id.proximity_information_sound);
        LinearLayout soundSettings = (LinearLayout)findViewById(R.id.sound_settings);
        LinearLayout buttonSound = (LinearLayout)findViewById(R.id.do_something_button_sound);




        destination.setMinimumHeight(getPxByPercentage(.15));
        //text
        proxText.setMinimumHeight(getPxByPercentage(.15));
        contactsText.setMinimumHeight(getPxByPercentage(.20));
        messageText.setMinimumHeight(getPxByPercentage(.20));
        buttonText.setMinimumHeight(getPxByPercentage(.12));
        //call
        proxCall.setMinimumHeight(getPxByPercentage(.15));
        contactCall.setMinimumHeight(getPxByPercentage(.20));
        buttonCall.setMinimumHeight(getPxByPercentage(.12));
        //reminder
        proxReminder.setMinimumHeight(getPxByPercentage(.15));
        messageReminder.setMinimumHeight(getPxByPercentage(.35));
        buttonReminder.setMinimumHeight(getPxByPercentage(.12));
        //sound
        proxSound.setMinimumHeight(getPxByPercentage(.15));
        soundSettings.setMinimumHeight(getPxByPercentage(.60));
        buttonSound.setMinimumHeight(getPxByPercentage(.12));




        taskDrawer.setMinimumHeight(getPxByPercentage(.15));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_settings:
                Intent intent1 = new Intent(this, UserSettingsActivity.class);
                startActivity(intent1);
                return true;
            case R.id.list:
                Intent intent2 = new Intent(this, TaskListActivity.class);
                startActivity(intent2);
                return true;
            case R.id.listLocations:
                Intent intent3 = new Intent(this, MyLocationsActivity.class);
                startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_text);

        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_text);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_text);

        thereL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));

        there.setChecked(true);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_text);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_text_miles_minutes);
        miles.setClickable(false);
    }

    public void thereCheckerCall(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_call);
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_call);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_call);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_call);

        thereL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));

        there.setChecked(true);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_call);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        miles.setClickable(false);
    }

    public void distanceCheckedText(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_text);
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_text);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_text);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_text);

        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.dark_purple));

        distance.setChecked(true);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_text);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_text_miles_minutes);
        miles.setClickable(true);
    }

    public void distanceCheckedCall(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_call);
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_call);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_call);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_call);

        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.dark_purple));

        distance.setChecked(true);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_call);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        miles.setClickable(true);
    }

    public void thereCheckerReminder(View view) {
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_reminder);
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_reminder);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_reminder);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_reminder);

        thereL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));

        there.setChecked(true);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_reminder);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        miles.setClickable(false);
    }

    public void distanceCheckedReminder(View view) {
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_reminder);
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_reminder);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_reminder);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_reminder);

        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.dark_purple));

        distance.setChecked(true);
        there.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_reminder);
        number.setEnabled(true);
        number.setClickable(true);
        number.setFocusableInTouchMode(true);
        Spinner miles = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        miles.setClickable(true);
    }

    public void thereCheckerSound(View view) {
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_sound);
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_sound);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_sound);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_sound);

        thereL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));

        there.setChecked(true);
        distance.setChecked(false);
        EditText number = (EditText) findViewById(R.id.miles_away_sound);
        number.setEnabled(false);
        number.setClickable(false);
        Spinner miles = (Spinner) findViewById(R.id.spinner_sound_miles_minutes);
        miles.setClickable(false);
    }

    public void distanceCheckedSound(View view) {
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_sound);
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_sound);
        LinearLayout distanceL = (LinearLayout) findViewById(R.id.distance_chooser_layout_sound);
        LinearLayout thereL = (LinearLayout) findViewById(R.id.there_layout_sound);

        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.dark_purple));

        distance.setChecked(true);
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
        int original = 0;
        CheckBox distanceCheckbox = (CheckBox) findViewById(R.id.distance_checkbox_sound);
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_sound);
        Spinner milesMinutes = (Spinner) findViewById(R.id.spinner_sound_miles_minutes);
        EditText _miles = (EditText) findViewById(R.id.miles_away_sound);
        if (distanceCheckbox.isChecked()) {
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
            } else if (milesMinutes.getSelectedItem().equals("meters")) {
                radiusType = "meters";
                metersAway = Long.valueOf(miles);
            }
        }

        int media = mediaVlmSeekBar.getProgress();
        int ring = ringerVlmSeekBar.getProgress();
        int system = alarmVlmSeekBar.getProgress();
        int nofity = notifyVlmSeekBar.getProgress();
        int active = 1;
        if(soundAlarm.isHasAlarm()){
            if(isAlarmForToday(soundAlarm)) {
                active = 2;
                Log.d("DOSOMETHING", "isActive");
            }
            else{
                active = 3;
            }
        }
        Task task = taskDataSource.createTask(destinationLocation, "", metersAway, Task.SOUND_SETTING_TASK_TYPE,
                destination, radiusType, original, active);
        soundAlarm.setTaskId(task.getId());
        alarmInfoDataSource.createAlarmInfo(soundAlarm);
        taskSoundDataSource.createSoundSettings(media, ring, nofity, system, task.getId());
        Toast.makeText(getApplicationContext(),
                "Sound Setting Created!", Toast.LENGTH_SHORT)
                .show();
        there.setChecked(true);
        distanceCheckbox.setChecked(false);
        _miles.setText("5");
        milesMinutes.setSelection(0);
        setSoundButtonsToEmpty();
        initControls();
    }

    public void sendTextMessage(View view) {
        long metersAway = 150;
        String radiusType = "there";
        int original = 0;
        MultiAutoCompleteTextView _contacts = (MultiAutoCompleteTextView) findViewById(R.id.multiAuto_contacts);
        EditText _reminder = (EditText) findViewById(R.id.enter_reminder_field);
        String reminder = "filler";
        CheckBox distanceCheckbox = (CheckBox) findViewById(R.id.distance_checkbox_text);
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_text);
        Spinner milesMinutes = (Spinner) findViewById(R.id.spinner_text_miles_minutes);
        EditText _miles = (EditText) findViewById(R.id.miles_away_text);
        if (distanceCheckbox.isChecked()) {
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
            } else if (milesMinutes.getSelectedItem().equals("meters")) {
                metersAway = Long.valueOf(miles);
                radiusType = "meters";
            }
        }
        if (_reminder != null) {
            reminder = _reminder.getText().toString();
        }
        int active = 1;
        if(textAlarm.isHasAlarm()){
            if(isAlarmForToday(textAlarm)) {
                active = 2;
                Log.d("DOSOMETHING", "isActive");
            }
            else{
                active = 3;
            }
        }
        Task task = taskDataSource.createTask(destinationLocation, reminder, metersAway, Task.TEXT_MESSAGE_TASK_TYPE,
                destination, radiusType, original, active);
        textAlarm.setTaskId(task.getId());
        alarmInfoDataSource.createAlarmInfo(textAlarm);
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

        _contacts.setText("");
        _reminder.setText("");
        distanceCheckbox.setChecked(false);
        milesMinutes.setSelection(0);
        there.setChecked(true);
        _miles.setText("5");
        setTextButtonsToEmpty();
    }

    private boolean isAlarmForToday(AlarmInfo info){
        boolean forToday = false;
        int day = getDay();
        if (day == 2 && info.is_mon()) {
            forToday = true;
        } else if (day == 3 && info.is_tue()) {
            forToday = true;
        } else if (day == 4 && info.is_wed()) {
            forToday = true;
        } else if (day == 5 && info.is_thu()) {
            forToday = true;
        } else if (day == 6 && info.is_fri()) {
            forToday = true;
        } else if (day == 7 && info.is_sat()) {
            forToday = true;
        } else if (day == 1 && info.is_sun()) {
            forToday = true;
        }
        return forToday;
    }

    public int getDay() {
        Date d = new Date();
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return day;
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
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_call);
        Spinner milesMin = (Spinner) findViewById(R.id.spinner_call_miles_minutes);
        EditText _miles = (EditText) findViewById(R.id.miles_away_call);
        if (distance.isChecked()) {
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
            } else if (milesMin.getSelectedItem().equals("meters")) {
                metersAway = Long.valueOf(miles);
                radiusType = "meters";
            }
        }

        AutoCompleteTextView _contacts = (AutoCompleteTextView) findViewById(R.id.auto_contacts);
        int active = 1;
        if(callAlarm.isHasAlarm()){
            if(isAlarmForToday(callAlarm)) {
                active = 2;
                Log.d("DOSOMETHING", "isActive");
            }
            else{
                active = 3;
            }
        }
        Task task = taskDataSource.createTask(destinationLocation, "", metersAway, Task.CALL_REMINDER_TASK_TYPE, destination,
                radiusType, original, active);
        callAlarm.setTaskId(task.getId());
        alarmInfoDataSource.createAlarmInfo(callAlarm);
        String contacts = _contacts.getText().toString();
        if(contacts.length()<1){
            Toast.makeText(this, "Please Enter a Contact", Toast.LENGTH_SHORT).show();
            return;
        }
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
        distance.setChecked(false);
        there.setChecked(true);

        _miles.setText("5");
        _contacts.setText("");
        milesMin.setSelection(0);
        setCallButtonsToEmpty();


    }

    private void setCallButtonsToEmpty(){
        callAlarm = new AlarmInfo();
        Button monday = (Button)findViewById(R.id.monday_call);
        Button tuesday = (Button)findViewById(R.id.tuesday_call);
        Button wednesday = (Button)findViewById(R.id.wednesday_call);
        Button thursday = (Button)findViewById(R.id.thursday_call);
        Button friday = (Button)findViewById(R.id.friday_call);
        Button saturday = (Button)findViewById(R.id.saturday_call);
        Button sunday = (Button)findViewById(R.id.sunday_call);
        monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));

    }

    private void setTextButtonsToEmpty(){
        textAlarm = new AlarmInfo();
        Button monday = (Button)findViewById(R.id.monday_text);
        Button tuesday = (Button)findViewById(R.id.tuesday_text);
        Button wednesday = (Button)findViewById(R.id.wednesday_text);
        Button thursday = (Button)findViewById(R.id.thursday_text);
        Button friday = (Button)findViewById(R.id.friday_text);
        Button saturday = (Button)findViewById(R.id.saturday_text);
        Button sunday = (Button)findViewById(R.id.sunday_text);
        monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));

    }

    private void setReminderButtonsToEmpty(){
        remindAlarm = new AlarmInfo();
        Button monday = (Button)findViewById(R.id.monday_reminder);
        Button tuesday = (Button)findViewById(R.id.tuesday_reminder);
        Button wednesday = (Button)findViewById(R.id.wednesday_reminder);
        Button thursday = (Button)findViewById(R.id.thursday_reminder);
        Button friday = (Button)findViewById(R.id.friday_reminder);
        Button saturday = (Button)findViewById(R.id.saturday_reminder);
        Button sunday = (Button)findViewById(R.id.sunday_reminder);
        monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));

    }

    private void setSoundButtonsToEmpty(){
        soundAlarm = new AlarmInfo();
        Button monday = (Button)findViewById(R.id.monday_sound);
        Button tuesday = (Button)findViewById(R.id.tuesday_sound);
        Button wednesday = (Button)findViewById(R.id.wednesday_sound);
        Button thursday = (Button)findViewById(R.id.thursday_sound);
        Button friday = (Button)findViewById(R.id.friday_sound);
        Button saturday = (Button)findViewById(R.id.saturday_sound);
        Button sunday = (Button)findViewById(R.id.sunday_sound);
        monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
        sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));

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
            mTxtPhoneNo.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        numbers.clear();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            mTxtPhoneNo.setAdapter(mAdapter);
            mTxtPhoneNo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            DoSomethingActivity.this.mPeopleList = maps;
        }
    }

    public void phoneSelected(View view) {
        hideKeyboard();
//        Drawable textI = getResources().getDrawable(R.drawable.text);
//        Drawable soundI = getResources().getDrawable(R.drawable.audio);
//        Drawable phoneGlow = getResources().getDrawable(R.drawable.phoneglow);
//        Drawable reminderI = getResources().getDrawable(R.drawable.reminder);

        callLayout.setVisibility(View.VISIBLE);
        textLayout.setVisibility(View.GONE);
        reminderLayout.setVisibility(View.GONE);
        soundLayout.setVisibility(View.GONE);

        textDone.setVisibility(View.GONE);
        callDone.setVisibility(View.VISIBLE);
        remindDone.setVisibility(View.GONE);
        soundDone.setVisibility(View.GONE);

        phoneIcon.setImageDrawable(phoneGlow);
        textIcon.setImageDrawable(textI);
        reminderIcon.setImageDrawable(reminderI);
        soundIcon.setImageDrawable(soundI);
    }

    public void soundSelected(View view) {
        hideKeyboard();
//        Drawable textI = getResources().getDrawable(R.drawable.text);
//        Drawable soundGlow = getResources().getDrawable(R.drawable.audioglow);
//        Drawable phoneI = getResources().getDrawable(R.drawable.phone);
//        Drawable reminderI = getResources().getDrawable(R.drawable.reminder);

        soundLayout.setVisibility(View.VISIBLE);
        textLayout.setVisibility(View.GONE);
        reminderLayout.setVisibility(View.GONE);
        callLayout.setVisibility(View.GONE);

        textDone.setVisibility(View.GONE);
        callDone.setVisibility(View.GONE);
        remindDone.setVisibility(View.GONE);
        soundDone.setVisibility(View.VISIBLE);

        phoneIcon.setImageDrawable(phoneI);
        textIcon.setImageDrawable(textI);
        reminderIcon.setImageDrawable(reminderI);
        soundIcon.setImageDrawable(soundGlow);
    }

    public void textSelected(View view) {
        hideKeyboard();

//        Drawable textGlow = getResources().getDrawable(R.drawable.textglow);
//        Drawable soundI = getResources().getDrawable(R.drawable.audio);
//        Drawable phoneI = getResources().getDrawable(R.drawable.phone);
//        Drawable reminderI = getResources().getDrawable(R.drawable.reminder);

        textLayout.setVisibility(View.VISIBLE);
        reminderLayout.setVisibility(View.GONE);
        callLayout.setVisibility(View.GONE);
        soundLayout.setVisibility(View.GONE);

        textDone.setVisibility(View.VISIBLE);
        callDone.setVisibility(View.GONE);
        remindDone.setVisibility(View.GONE);
        soundDone.setVisibility(View.GONE);

        phoneIcon.setImageDrawable(phoneI);
        textIcon.setImageDrawable(textGlow);
        reminderIcon.setImageDrawable(reminderI);
        soundIcon.setImageDrawable(soundI);

    }

    public void reminderSelected(View view) {
        hideKeyboard();

        reminderLayout.setVisibility(View.VISIBLE);
        textLayout.setVisibility(View.GONE);
        callLayout.setVisibility(View.GONE);
        soundLayout.setVisibility(View.GONE);

        textDone.setVisibility(View.GONE);
        callDone.setVisibility(View.GONE);
        remindDone.setVisibility(View.VISIBLE);
        soundDone.setVisibility(View.GONE);

        phoneIcon.setImageDrawable(phoneI);
        textIcon.setImageDrawable(textI);
        reminderIcon.setImageDrawable(reminderGlow);
        soundIcon.setImageDrawable(soundI);
    }

    public void goToGoogleMaps(View v) {
        double lat = destinationLocation.latitude;
        double longitude = destinationLocation.longitude;
        Location myLocation = gpsTracker.getLocation();
        String url = "http://maps.google.com/maps?" +
                "daddr=" + lat + "," + longitude;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
        Log.d("GOOGLE", "maps activity sent hopefully");
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
        CheckBox there = (CheckBox) findViewById(R.id.there_checkbox_reminder);
        CheckBox distance = (CheckBox) findViewById(R.id.distance_checkbox_reminder);
        Spinner milesMin = (Spinner) findViewById(R.id.spinner_reminder_miles_minutes);
        EditText _reminder = (EditText) findViewById(R.id.reminder_edit_text);
        EditText _miles = (EditText) findViewById(R.id.miles_away_reminder);
        String reminder = "";
        if (distance.isChecked()) {
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
            } else if (milesMin.getSelectedItem().equals("meters")) {
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
        int active = 1;
        if(remindAlarm.isHasAlarm()){
            if(isAlarmForToday(remindAlarm)) {
                active = 2;
                Log.d("DOSOMETHING", "isActive");
            }
            else{
                active = 3;
            }
        }


        Task task = taskDataSource.createTask(destinationLocation, reminder, metersAway, Task.REMINDER_MESSAGE_TASK_TYPE, destination, radiusType, original,active);
        remindAlarm.setTaskId(task.getId());
        alarmInfoDataSource.createAlarmInfo(remindAlarm);
        Log.d("CreateTaskActivity", "Saved Destination");
      //  distance.setText("5");
        there.setChecked(true);
        distance.setChecked(false);
        _reminder.setText("");
        _miles.setText("5");
        milesMin.setSelection(0);
        setReminderButtonsToEmpty();

    }


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (keyCode == EditorInfo.IME_ACTION_SEARCH ||
                keyCode == EditorInfo.IME_ACTION_DONE ||
                event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

            if (!event.isShiftPressed()) {
                Log.v("AndroidEnterKeyActivity", "Enter Key Pressed!");
//                switch (view.getId()) {
//                    case R.id.enter_reminder_field:
//                        EditText enter = (EditText) findViewById(R.id.enter_reminder_field);
//                        Toast.makeText(getApplicationContext(),
//                                enter.getText().toString(), Toast.LENGTH_SHORT)
//                                .show();
//                        break;
//                }
                hideKeyboard();
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


    public int getPxByPercentage(double percentage) {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float height = MyApplication.getHeight();
        float px = height * (metrics.densityDpi / 160f);

        return (int) (px * percentage);
    }

    private class loadImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            textI = getResources().getDrawable(R.drawable.text);
            textGlow = getResources().getDrawable(R.drawable.textglow);

            reminderI =getResources().getDrawable(R.drawable.reminder);
            reminderGlow = getResources().getDrawable(R.drawable.reminderglow);

            soundI = getResources().getDrawable(R.drawable.audio);
            soundGlow = getResources().getDrawable(R.drawable.audioglow);

            phoneI = getResources().getDrawable(R.drawable.phone);
            phoneGlow = getResources().getDrawable(R.drawable.phoneglow);

            return null;

        }
    }

    public void setAlarmText(View view){
       Button monday = (Button)findViewById(R.id.monday_text);
        Button tuesday = (Button)findViewById(R.id.tuesday_text);
        Button wednesday = (Button)findViewById(R.id.wednesday_text);
        Button thursday = (Button)findViewById(R.id.thursday_text);
        Button friday = (Button)findViewById(R.id.friday_text);
        Button saturday = (Button)findViewById(R.id.saturday_text);
        Button sunday = (Button)findViewById(R.id.sunday_text);

        if(view.equals(monday)){
            boolean on = textAlarm.is_mon();
            if(!on){
                monday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_mon(true);
            }
            else{
                monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_mon(false);
            }
        }
        if(view.equals(tuesday)){
            boolean on = textAlarm.is_tue();
            if(!on){
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_tue(true);
            }
            else{
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_tue(false);
            }
        }
        if(view.equals(wednesday)){
            boolean on = textAlarm.is_wed();
            if(!on){
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_wed(true);
            }
            else{
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_wed(false);
            }
        }
        if(view.equals(thursday)){
            boolean on = textAlarm.is_thu();
            if(!on){
                thursday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_thu(true);
            }
            else{
                thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_thu(false);
            }
        }
        if(view.equals(friday)){
            boolean on = textAlarm.is_fri();
            if(!on){
                friday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_fri(true);
            }
            else{
                friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_fri(false);
            }
        }
        if(view.equals(saturday)){
            boolean on = textAlarm.is_sat();
            if(!on){
                saturday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_sat(true);
            }
            else{
                saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_sat(false);
            }
        }
        if(view.equals(sunday)){
            boolean on = textAlarm.is_sun();
            if(!on){
                sunday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                textAlarm.set_sun(true);
            }
            else{
                sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                textAlarm.set_sun(false);
            }
        }
    }

    public void setAlarmCall(View view){
        Button monday = (Button)findViewById(R.id.monday_call);
        Button tuesday = (Button)findViewById(R.id.tuesday_call);
        Button wednesday = (Button)findViewById(R.id.wednesday_call);
        Button thursday = (Button)findViewById(R.id.thursday_call);
        Button friday = (Button)findViewById(R.id.friday_call);
        Button saturday = (Button)findViewById(R.id.saturday_call);
        Button sunday = (Button)findViewById(R.id.sunday_call);

        if(view.equals(monday)){
            boolean on = callAlarm.is_mon();
            if(!on){
                monday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                callAlarm.set_mon(true);
            }
            else{
                monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_mon(false);
            }
        }
        if(view.equals(tuesday)){
            boolean on = callAlarm.is_tue();
            if(!on){
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                callAlarm.set_tue(true);
            }
            else{
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_tue(false);
            }
        }
        if(view.equals(wednesday)){
            boolean on = callAlarm.is_wed();
            if(!on){
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                callAlarm.set_wed(true);
            }
            else{
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_wed(false);
            }
        }
        if(view.equals(thursday)){
            boolean on = callAlarm.is_thu();
            if(!on){
                thursday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                callAlarm.set_thu(true);
            }
            else{
                thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_thu(false);
            }
        }
        if(view.equals(friday)){
            boolean on = callAlarm.is_fri();
            if(!on){
                friday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                callAlarm.set_fri(true);
            }
            else{
                friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_fri(false);
            }
        }
        if(view.equals(saturday)){
            boolean on = callAlarm.is_sat();
            if(!on){
                saturday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                callAlarm.set_sat(true);
            }
            else{
                saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_sat(false);
            }
        }
        if(view.equals(sunday)){
            boolean on = callAlarm.is_sun();
            if(!on){
                sunday.setBackground(getResources().getDrawable(R.drawable.button_filled));
               callAlarm.set_sun(true);
            }
            else{
                sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                callAlarm.set_sun(false);
            }
        }
    }

    public void setAlarmReminder(View view){
        Button monday = (Button)findViewById(R.id.monday_reminder);
        Button tuesday = (Button)findViewById(R.id.tuesday_reminder);
        Button wednesday = (Button)findViewById(R.id.wednesday_reminder);
        Button thursday = (Button)findViewById(R.id.thursday_reminder);
        Button friday = (Button)findViewById(R.id.friday_reminder);
        Button saturday = (Button)findViewById(R.id.saturday_reminder);
        Button sunday = (Button)findViewById(R.id.sunday_reminder);

        if(view.equals(monday)){
            boolean on = remindAlarm.is_mon();
            if(!on){
                monday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_mon(true);
            }
            else{
                monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                remindAlarm.set_mon(false);
            }
        }
        if(view.equals(tuesday)){
            boolean on = textAlarm.is_tue();
            if(!on){
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_tue(true);
            }
            else{
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                remindAlarm.set_tue(false);
            }
        }
        if(view.equals(wednesday)){
            boolean on = remindAlarm.is_wed();
            if(!on){
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_wed(true);
            }
            else{
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                remindAlarm.set_wed(false);
            }
        }
        if(view.equals(thursday)){
            boolean on = remindAlarm.is_thu();
            if(!on){
                thursday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_thu(true);
            }
            else{
                thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                remindAlarm.set_thu(false);
            }
        }
        if(view.equals(friday)){
            boolean on = remindAlarm.is_fri();
            if(!on){
                friday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_fri(true);
            }
            else{
                friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                remindAlarm.set_fri(false);
            }
        }
        if(view.equals(saturday)){
            boolean on = remindAlarm.is_sat();
            if(!on){
                saturday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_sat(true);
            }
            else{
                saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                remindAlarm.set_sat(false);
            }
        }
        if(view.equals(sunday)){
            boolean on = remindAlarm.is_sun();
            if(!on){
                sunday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                remindAlarm.set_sun(true);
            }
            else{
                sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));
               remindAlarm.set_sun(false);
            }
        }
    }

   public void setAlarmSound(View view){
        Button monday = (Button)findViewById(R.id.monday_sound);
        Button tuesday = (Button)findViewById(R.id.tuesday_sound);
        Button wednesday = (Button)findViewById(R.id.wednesday_sound);
        Button thursday = (Button)findViewById(R.id.thursday_sound);
        Button friday = (Button)findViewById(R.id.friday_sound);
        Button saturday = (Button)findViewById(R.id.saturday_sound);
        Button sunday = (Button)findViewById(R.id.sunday_sound);

        if(view.equals(monday)){
            boolean on = soundAlarm.is_mon();
            if(!on){
                monday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_mon(true);
            }
            else{
                monday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_mon(false);
            }
        }
        if(view.equals(tuesday)){
            boolean on = soundAlarm.is_tue();
            if(!on){
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_tue(true);
            }
            else{
                tuesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_tue(false);
            }
        }
        if(view.equals(wednesday)){
            boolean on = soundAlarm.is_wed();
            if(!on){
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_wed(true);
            }
            else{
                wednesday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_wed(false);
            }
        }
        if(view.equals(thursday)){
            boolean on = soundAlarm.is_thu();
            if(!on){
                thursday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_thu(true);
            }
            else{
                thursday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_thu(false);
            }
        }
        if(view.equals(friday)){
            boolean on = soundAlarm.is_fri();
            if(!on){
                friday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_fri(true);
            }
            else{
                friday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_fri(false);
            }
        }
        if(view.equals(saturday)){
            boolean on = soundAlarm.is_sat();
            if(!on){
                saturday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_sat(true);
            }
            else{
                saturday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_sat(false);
            }
        }
        if(view.equals(sunday)){
            boolean on = soundAlarm.is_sun();
            if(!on){
                sunday.setBackground(getResources().getDrawable(R.drawable.button_filled));
                soundAlarm.set_sun(true);
            }
            else{
                sunday.setBackground(getResources().getDrawable(R.drawable.button_outline));
                soundAlarm.set_sun(false);
            }
        }
    }




}

