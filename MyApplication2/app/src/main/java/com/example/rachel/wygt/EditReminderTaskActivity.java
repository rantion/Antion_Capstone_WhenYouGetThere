package com.example.rachel.wygt;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 11/8/14.
 */
public class EditReminderTaskActivity extends Activity {

    long taskId;
    TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    GPSTracker gpsTracker = MyApplication.getGpsTracker();
    TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    TextView destination;
    GetDrivingDistances getDistance;
    CheckBox there, distance;
    EditTextClear message, distanceM;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.crud_reminder);
        Location location = gpsTracker.getLocation();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng destinationLocation = null;
        destination = (TextView) findViewById(R.id.edit_reminder_destination);
        there = (CheckBox) findViewById(R.id.edit_reminder_there_checkbox);
        distance = (CheckBox) findViewById(R.id.edit_reminder_distance_checkbox);
        distanceM = (EditTextClear) findViewById(R.id.edit_reminder_distance_away);
        message = (EditTextClear) findViewById(R.id.edit_reminder_message);
        spinner = (Spinner) findViewById(R.id.edit_reminder_spinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.miles_minutes, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(adapter);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            taskId = extras.getLong("taskID");
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
            message.setText(extras.getString("message"));
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
        }
        super.onCreate(savedInstanceState);
    }

    public void thereCheckedEditReminder(View view) {
        distance.setChecked(false);
        distanceM.setEnabled(false);
        distanceM.setClickable(false);
        spinner.setClickable(false);
    }

    public void distanceCheckedEditReminder(View view) {
        there.setChecked(false);
        distanceM.setEnabled(true);
        distanceM.setClickable(true);
        distanceM.setFocusableInTouchMode(true);
        spinner.setClickable(true);
    }

    public void deleteReminder(View view) {
        Task task = taskDataSource.getTaskById(taskId);
        if (task != null) {
            taskDataSource.deleteTask(task);
        }
        Toast.makeText(this, "Reminder Task Deleted", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return true;
    }



    public void updateReminder(View view) {
        Task task = taskDataSource.getTaskById(taskId);
        long metersAway = 150;
        String radiusType = "there";
        int original = 0;
        String reminder = " ";
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
        if (message != null) {
            reminder = message.getText().toString();
        }
        task.setReminder(reminder);
        if (task != null) {
            taskDataSource.updateTask(task);
        }
        Toast.makeText(this, "Reminder Updated", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }

}