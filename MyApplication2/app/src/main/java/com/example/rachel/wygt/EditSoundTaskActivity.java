package com.example.rachel.wygt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 11/7/14.
 */
public class EditSoundTaskActivity extends Activity {
    long taskId;
    TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    TaskSoundDataSource taskSoundDataSource = MyApplication.getTaskSoundDataSource();
    GPSTracker gpsTracker = MyApplication.getGpsTracker();
    TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    TextView destination;
    GetDrivingDistances getDistance;
    LinearLayout thereL, distanceL;
    CheckBox there, distance;
    AutoCompleteTextView contacts;
    EditTextClear distanceM;
    Spinner spinner;
    private SeekBar mediaVlmSeekBar = null;
    private SeekBar ringerVlmSeekBar = null;
    private SeekBar alarmVlmSeekBar = null;
    private AudioManager audioManager = null;
    private SeekBar notifyVlmSeekBar = null;
    private int mediaMax, ringerMax, notifyMax, alarmMax;
    private int _ring, _media, _system, _notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.crud_sound);
        Location location = gpsTracker.getLocation();
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng destinationLocation = null;
        getActionBar().setDisplayHomeAsUpEnabled(true);
        thereL = (LinearLayout)findViewById(R.id.there_layout_sound);
        distanceL = (LinearLayout)findViewById(R.id.distance_layout_sound);
        double width = MyApplication.getWidth();
        thereL.setMinimumWidth(getPxByPercentage(width));
        distanceL.setMinimumWidth(getPxByPercentage(width));
        LinearLayout all = (LinearLayout)findViewById(R.id.edit_sound_distance_layout);
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)thereL.getLayoutParams();
//        params.height= getPxByPercentage(.50);
//        all.updateViewLayout(thereL, params);
//        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)distanceL.getLayoutParams();
//        params1.height= getPxByPercentage(.50);
//        all.updateViewLayout(distanceL, params1);
        destination = (TextView) findViewById(R.id.edit_sound_destination);
        there = (CheckBox) findViewById(R.id.edit_sound_there_checkbox);
        distance = (CheckBox) findViewById(R.id.edit_sound_distance_checkbox);
        distanceM = (EditTextClear) findViewById(R.id.edit_sound_distance_away);
        spinner = (Spinner) findViewById(R.id.edit_sound_spinner);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.miles_minutes, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            taskId = extras.getLong("taskID");
            _ring = extras.getInt("ring");
            _media = extras.getInt("media");
            _system = extras.getInt("system");
            _notify = extras.getInt("notify");
            double lat = extras.getDouble("lat");
            double _long = extras.getDouble("long");
            destinationLocation = new LatLng(lat, _long);
            try {
                (getDistance = new GetDrivingDistances()).execute(destinationLocation, currentLocation).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            destination.setText(extras.getString("destination"));
            String radiusType = extras.getString("radiusType");
            if (radiusType.equals("there")) {
                there.setChecked(true);
                distance.setChecked(false);
                distanceM.setEnabled(false);
                distanceM.setClickable(false);
                spinner.setClickable(false);
            } else {
                distance.setChecked(true);
                there.setChecked(false);
                distanceM.setEnabled(true);
                distanceM.setClickable(true);
                distanceM.setFocusableInTouchMode(true);
                spinner.setClickable(true);
                int distance = extras.getInt("original");
                distanceM.setText(String.valueOf(distance));
                ArrayAdapter myAdap = (ArrayAdapter) spinner.getAdapter();
                int spinnerPosition = myAdap.getPosition(radiusType);
                spinner.setSelection(spinnerPosition);
            }
            initControls();
            initViews();
        }
        super.onCreate(savedInstanceState);
    }

    public void initViews(){
        LinearLayout display = (LinearLayout)findViewById(R.id.edit_sound_display);
        LinearLayout destination = (LinearLayout)findViewById(R.id.edit_sound_destination_layout);
        LinearLayout distance = (LinearLayout) findViewById(R.id.edit_sound_distance_layout);
        LinearLayout sound = (LinearLayout)findViewById(R.id.edit_sound_settings_layout);
        LinearLayout buttons = (LinearLayout)findViewById(R.id.edit_sound_button_layout);


        display.setMinimumHeight(getPxByPercentage(.20));
        destination.setMinimumHeight(getPxByPercentage(.16));
        distance.setMinimumHeight(getPxByPercentage(.20));
        sound.setMinimumHeight(getPxByPercentage(.50));
        buttons.setMinimumHeight(getPxByPercentage(.12));
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


    public void thereCheckedSound(View view) {
        thereL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        there.setChecked(true);
        distance.setChecked(false);
        distanceM.setEnabled(false);
        distanceM.setClickable(false);
        spinner.setClickable(false);
    }

    public void distanceCheckedSound(View view) {
        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        there.setChecked(false);
        distance.setChecked(true);
        distanceM.setEnabled(true);
        distanceM.setClickable(true);
        distanceM.setFocusableInTouchMode(true);
        spinner.setClickable(true);
    }

    public void deleteSound(View view) {
        Task task = taskDataSource.getTaskById(taskId);
        if (task != null) {
            taskDataSource.deleteTask(task);
        }
        Toast.makeText(this, "Sound Setting Task Deleted", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }


    public long getMinutesAwayRadius(int minutes) {
        String distanceAway = getDistance.getDestDistMeters();
        String _duration = getDistance.getDestDistSeconds();
        int meters = Integer.parseInt(distanceAway);
        int seconds = Integer.parseInt(_duration);
        double rate = (meters / seconds);
        double temp = rate * minutes;
        return (long) temp * 60;
    }

    public long convertToMeters(double miles) {
        long meters = (long) (miles * 1609.34);
        return meters;
    }

    public void updateSound(View view) {
        Task task = taskDataSource.getTaskById(taskId);
        List<SoundSettings> sounds = taskSoundDataSource.getTaskSoundsByTaskId(task.getId());
        for (SoundSettings soundSettings : sounds) {
            taskSoundDataSource.deleteTaskSound(soundSettings);
        }
        long metersAway = 150;
        String radiusType = "there";
        int original = 0;
        String reminder = "filler";
        if (distance.isChecked()) {

            String miles = distanceM.getText().toString();
            if (miles.length() == 0) {
                Toast.makeText(this, "Please Enter a Distance", Toast.LENGTH_SHORT).show();
                return;
            }
            original = Integer.parseInt(miles);
            if (spinner.getSelectedItem().equals("miles")) {
                metersAway = convertToMeters(Double.valueOf(miles));
                radiusType = "miles";
            } else if (spinner.getSelectedItem().equals("minutes")) {
                metersAway = getMinutesAwayRadius(Integer.parseInt(miles));
                radiusType = "minutes";
            } else if (spinner.getSelectedItem().equals("meters")) {
                metersAway = Long.valueOf(miles);
                radiusType = "meters";
            }
        }
        task.setOriginalRadius(Integer.parseInt(distanceM.getText().toString()));
        task.setRadius_type(radiusType);
        task.setRadius(metersAway);
        int media = mediaVlmSeekBar.getProgress();
        int ring = ringerVlmSeekBar.getProgress();
        int system = alarmVlmSeekBar.getProgress();
        int notify = notifyVlmSeekBar.getProgress();
        taskSoundDataSource.createSoundSettings(media, ring, notify, system, task.getId());
        if (task != null) {
            taskDataSource.updateTask(task);
        }
        Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
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

    private void initControls() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaVlmSeekBar = (SeekBar) findViewById(R.id.edit_sound_mediaSeek);
        mediaMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mediaVlmSeekBar.setMax(mediaMax);
        mediaVlmSeekBar.setProgress(_media);
        ImageView mediaIcon = (ImageView) findViewById(R.id.edit_sound_mediaIcon);
        mediaIcon.setImageDrawable(getVolumeIcon(mediaMax, _media, SoundSettings.SOUND_TYPE_MEDIA));

        ringerVlmSeekBar = (SeekBar) findViewById(R.id.edit_sound_ringerSeek);
        ringerMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        ringerVlmSeekBar.setMax(ringerMax);
        ringerVlmSeekBar.setProgress(_ring);
        ImageView ringerIcon = (ImageView) findViewById(R.id.edit_sound_ringerIcon);
        ringerIcon.setImageDrawable(getVolumeIcon(ringerMax, _ring, SoundSettings.SOUND_TYPE_RINGER));

        alarmVlmSeekBar = (SeekBar) findViewById(R.id.edit_sound_systemSeek);
        alarmMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        alarmVlmSeekBar.setMax(alarmMax);
        alarmVlmSeekBar.setProgress(_system);
        ImageView alarmIcon = (ImageView) findViewById(R.id.edit_sound_systemIcon);
        alarmIcon.setImageDrawable(getVolumeIcon(alarmMax, _system, SoundSettings.SOUND_TYPE_ALARM));

        notifyVlmSeekBar = (SeekBar) findViewById(R.id.edit_sound_notificationSeek);
        notifyMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        notifyVlmSeekBar.setMax(notifyMax);
        notifyVlmSeekBar.setProgress(_notify);
        ImageView notifyIcon = (ImageView) findViewById(R.id.edit_sound_notificationIcon);
        notifyIcon.setImageDrawable(getVolumeIcon(notifyMax, _notify, SoundSettings.SOUND_TYPE_NOTIFICATION));

        if (_ring == 0) {
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
            ImageView notify = (ImageView) findViewById(R.id.edit_sound_notificationIcon);
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
            ImageView alarm = (ImageView) findViewById(R.id.edit_sound_systemIcon);
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
        alarmVlmSeekBar.setProgress(0);
        notifyVlmSeekBar.setProgress(0);
        notifyVlmSeekBar.setEnabled(false);
        alarmVlmSeekBar.setEnabled(false);
    }

    private SeekBar.OnSeekBarChangeListener ringerChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            ImageView ringer = (ImageView) findViewById(R.id.edit_sound_ringerIcon);
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
            ImageView media = (ImageView) findViewById(R.id.edit_sound_mediaIcon);
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

    public int getPxByPercentage(double percentage) {

        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float height = MyApplication.getHeight();
        float px = height * (metrics.densityDpi / 160f);

        return (int) (px * percentage);
    }


}
