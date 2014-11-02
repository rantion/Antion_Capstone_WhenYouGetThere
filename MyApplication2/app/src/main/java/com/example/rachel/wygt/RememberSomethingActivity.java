package com.example.rachel.wygt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 10/15/14.
 */
public class RememberSomethingActivity extends Activity implements View.OnKeyListener {

    private LatLng destinationLocation, currentLocation;
    private String distance, duration, destination, distanceMeters, durationSeconds;
    private TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    private SeekBar mediaVlmSeekBar = null;
    private SeekBar ringerVlmSeekBar = null;
    private SeekBar alarmVlmSeekBar = null;
    private SeekBar notifyVlmSeekBar = null;
    private AudioManager audioManager = null;
    private int mediaMax, ringerMax, notifyMax, alarmMax, ringCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setVolumeControlStream(AudioManager.STREAM_RING);
        this.setVolumeControlStream(AudioManager.STREAM_ALARM);
        this.setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "appIsOpenSetTrue - RememberSomethingActivity");
        editor.putBoolean("appIsOpen", true);
        editor.apply();
        setContentView(R.layout.activity_remember_something);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            destinationLocation = (LatLng) extras.get("Destination_location");
            currentLocation = (LatLng) extras.get("Current_Location");
            try {
                (new GetDrivingDistance()).execute(destinationLocation, currentLocation).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            destination = (String) extras.get("Destination");
            String button = (String) extras.get("Button");
            TextView _destination = (TextView) findViewById(R.id.destination_address);
            TextView _buttonPushed = (TextView) findViewById(R.id.button_type);
            if (_destination != null) {
                _destination.setText(destination);
            }
            if (_buttonPushed != null) {
                _buttonPushed.setText(button);
            }
            initControls();

        }
    }

    private Drawable getVolumeIcon(int max, int current, int type) {
        Drawable icon = null;
        float third = (max/3);
        float twoThirds = (third*2);
        if(current==0){
            if(type == SoundSettings.SOUND_TYPE_RINGER && current == 0) {
                icon = getResources().getDrawable(R.drawable.vibrate);
            }
            else{
                icon = getResources().getDrawable(R.drawable.mute);
            }
        }
        else if (current < third ){
            icon = getResources().getDrawable(R.drawable.volume1);
        }
        else if (current >= third && current <= twoThirds) {
            icon = getResources().getDrawable(R.drawable.volume2);
        }
        else if (current >= twoThirds) {
            icon = getResources().getDrawable(R.drawable.volume3);
        }
        return icon;
    }

    private void initControls() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaVlmSeekBar = (SeekBar) findViewById(R.id.mediaSeek);
        mediaMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int mediaCurrent = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mediaVlmSeekBar.setMax(mediaMax);
        mediaVlmSeekBar.setProgress(mediaCurrent);
        ImageView mediaIcon = (ImageView)findViewById(R.id.mediaIcon);
        mediaIcon.setImageDrawable(getVolumeIcon(mediaMax,mediaCurrent,SoundSettings.SOUND_TYPE_MEDIA));

        ringerVlmSeekBar = (SeekBar) findViewById(R.id.ringerSeek);
        ringerMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        ringCurrent = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        ringerVlmSeekBar.setMax(ringerMax);
        ringerVlmSeekBar.setProgress(ringCurrent);
        ImageView ringerIcon = (ImageView)findViewById(R.id.ringerIcon);
        ringerIcon.setImageDrawable(getVolumeIcon(ringerMax, ringCurrent, SoundSettings.SOUND_TYPE_RINGER));

        alarmVlmSeekBar = (SeekBar) findViewById(R.id.systemSeek);
        alarmMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int alarmCurrent = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        alarmVlmSeekBar.setMax(alarmMax);
        alarmVlmSeekBar.setProgress(alarmCurrent);
        ImageView alarmIcon = (ImageView)findViewById(R.id.systemIcon);
        alarmIcon.setImageDrawable(getVolumeIcon(alarmMax,alarmCurrent,SoundSettings.SOUND_TYPE_ALARM));

        notifyVlmSeekBar = (SeekBar) findViewById(R.id.notificationSeek);
        notifyMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        int notifyCurrent = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        notifyVlmSeekBar.setMax(notifyMax);
        notifyVlmSeekBar.setProgress(notifyCurrent);
        ImageView notifyIcon = (ImageView)findViewById(R.id.notificationIcon);
        notifyIcon.setImageDrawable(getVolumeIcon(notifyMax,notifyCurrent,SoundSettings.SOUND_TYPE_NOTIFICATION));

        if(ringCurrent == 0){
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
            notify.setImageDrawable(getVolumeIcon(notifyMax,progress,SoundSettings.SOUND_TYPE_NOTIFICATION));
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, progress, 0);
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
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void ringIs0(){
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,0,0);
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
            if(progress == 0){
              ringIs0();
            }
            else {
                notifyVlmSeekBar.setEnabled(true);
                alarmVlmSeekBar.setEnabled(true);
            }
            audioManager.setStreamVolume(AudioManager.STREAM_RING, progress, 0);
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
            media.setImageDrawable(getVolumeIcon(mediaMax,progress,SoundSettings.SOUND_TYPE_MEDIA));
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void setProximityAlert(View view) {

        EditText _miles = (EditText) findViewById(R.id.miles_away);
        String miles = _miles.getText().toString();
        RadioButton button = (RadioButton) findViewById(R.id.radio_there);
        EditText _reminder = (EditText) findViewById(R.id.enter_reminder_field);
        EditText _minutes = (EditText) findViewById(R.id.minutes_away);
        String minutes = _minutes.getText().toString();
        String reminder = "";
        long metersAway = 50;
        if (miles.length() > 0) {
            metersAway = convertToMeters(Double.valueOf(miles));
        } else if (minutes.length() > 0) {
            metersAway = getMinutesAwayRadius(Integer.parseInt(minutes));
        } else if (button.isChecked()) {
            metersAway = 50;
        } else {
            Toast.makeText(getApplicationContext(), "Please Choose A Location Trigger", Toast.LENGTH_SHORT).show();
            return;
        }
        if (_reminder != null) {
            reminder = _reminder.getText().toString();
        }
        Toast.makeText(getApplicationContext(),
                "Reminder Created!", Toast.LENGTH_SHORT)
                .show();
        taskDataSource.createTask(destinationLocation, reminder, metersAway, Task.REMINDER_MESSAGE_TASK_TYPE);
        Log.d("CreateTaskActivity", "Saved Destination");

    }

    public long convertToMeters(double miles) {
        long meters = (long) (miles * 1609.34);
        return meters;
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


    private class GetDrivingDistance extends AsyncTask<LatLng, LatLng, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RememberSomethingActivity.this);
            pDialog.setMessage("Performing Calculations ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(LatLng... params) {
            LatLng _origin = params[0];
            LatLng _destination = params[1];
            String origin = _origin.latitude + "," + _origin.longitude;
            String destination = _destination.latitude + "," + _destination.longitude;
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                    origin + "&destinations=" + destination +
                    "&mode=driving&sensor=false&language=en-EN&units=imperial";
            Double finalDistance = 0.0;


            JSONObject _jsonObject = null;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                StatusLine status = httpResponse.getStatusLine();
                if (status.getStatusCode() != 200) {
                    Log.d("**DistanceMatrixHelper**", "HTTP error, invalid server status code: " + httpResponse.getStatusLine());
                }
                HttpEntity httpEntity = httpResponse.getEntity();
                String response = EntityUtils.toString(httpEntity);
                Log.d("RESPONSE", response);
                _jsonObject = new JSONObject(response);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return _jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            pDialog.dismiss();
            JSONArray rows = null;
            List<String> distances = new ArrayList<String>();
            if (jsonObject != null) {
                try {
                    rows = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        JSONArray elements = row.getJSONArray("elements");
                        for (int j = 0; i < elements.length(); i++) {
                            JSONObject element = elements.getJSONObject(0);
                            JSONObject _duration = element.getJSONObject("duration");
                            duration = _duration.getString("text");
                            durationSeconds = _duration.getString("value");
                            JSONObject distance = element.getJSONObject("distance");
                            distances.add(distance.getString("text"));
                            distanceMeters = distance.getString("value");
                        }
                        String _distances = null;
                        for (String distance : distances) {
                            _distances = distance + "\n\n";
                        }
                        TextView _distance = (TextView) findViewById(R.id.distance);
//                        _distance.setText(_distances);
                        distance = _distances;
                        _distance.setText(distance + "\n ETA: " + duration);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
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
    protected void onDestroy() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/ APP IS OPEN", "ONDESTROY__appIsOpenSetToFalse - ReminderTaskActivity");
        editor.putBoolean("appIsOpen", false);
        editor.apply();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "ONSTOP__appIsOpenSetToFalse - ReminderTaskActivity");
        editor.putBoolean("appIsOpen", false);
        editor.apply();
        super.onStop();
    }

    @Override
    protected void onPause() {
//        SharedPreferences sharedPreferences = PreferenceManager
//                .getDefaultSharedPreferences(MyApplication.getAppContext());
//        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "onPause_RememberSomethingActivity");
//        editor.putBoolean("appIsOpen", false);
//        editor.apply();
        super.onPause();
    }


}
