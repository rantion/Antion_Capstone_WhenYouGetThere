package com.example.rachel.wygt;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
 * Created by Rachel on 11/6/14.
 */
public class EditSMSTaskActivity extends Activity {
    long taskId;
    TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    GPSTracker gpsTracker = MyApplication.getGpsTracker();
    TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();
    TextView destination;
    GetDrivingDistances getDistance;
    CheckBox there, distance;
    MultiAutoCompleteTextView contacts;
    EditTextClear message, distanceM;
    LinearLayout thereL, distanceL;
    private SimpleAdapter mAdapter;
    Spinner spinner;
    ArrayList<Map<String, String>> mPeopleList = MyApplication.getmPeopleList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.crud_text);
        Location location = gpsTracker.getLocation();
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng destinationLocation = null;
        (new PopulateContacts()).execute();
        initViews();
        destination = (TextView) findViewById(R.id.edit_text_destination);
        there = (CheckBox) findViewById(R.id.edit_text_there_checkbox);
        distance = (CheckBox) findViewById(R.id.edit_text_distance_checkbox);
        LinearLayout all = (LinearLayout)findViewById(R.id.edit_text_distance_layout);
        thereL = (LinearLayout)findViewById(R.id.there_layout_text);
        distanceL = (LinearLayout)findViewById(R.id.distance_layout_text);
        double width = MyApplication.getWidth();
        thereL.setMinimumWidth(getPxByPercentage(width));
        distanceL.setMinimumWidth(getPxByPercentage(width));
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)thereL.getLayoutParams();
//        params.height= getPxByPercentage(.50);
//        all.updateViewLayout(thereL, params);
//        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)distanceL.getLayoutParams();
//        params1.height= getPxByPercentage(.50);
//        all.updateViewLayout(distanceL, params1);
        distanceM = (EditTextClear) findViewById(R.id.edit_text_distance_away);
        contacts = (MultiAutoCompleteTextView) findViewById(R.id.edit_text_contacts);
        message = (EditTextClear) findViewById(R.id.edit_text_message);
        spinner = (Spinner) findViewById(R.id.edit_text_spinner);
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
            contacts.setText(extras.getString("contacts"));
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

    public void initViews(){
        LinearLayout display = (LinearLayout)findViewById(R.id.edit_text_display);
        LinearLayout destination = (LinearLayout)findViewById(R.id.edit_text_destination_layout);
        LinearLayout distance = (LinearLayout)findViewById(R.id.edit_text_distance_layout);
        LinearLayout contacts = (LinearLayout)findViewById(R.id.edit_text_contact_layout);
        LinearLayout message = (LinearLayout)findViewById(R.id.edit_text_message_layout);
        LinearLayout buttons = (LinearLayout)findViewById(R.id.edit_text_button_layout);

        display.setMinimumHeight(getPxByPercentage(.20));
        destination.setMinimumHeight(getPxByPercentage(.16));
        distance.setMinimumHeight(getPxByPercentage(.20));
        contacts.setMinimumHeight(getPxByPercentage(.25));
        message.setMinimumHeight(getPxByPercentage(.30));
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


    public void thereCheckedEditText(View view) {
        thereL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        distanceL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distance.setChecked(false);
        there.setChecked(true);
        distanceM.setEnabled(false);
        distanceM.setClickable(false);
        spinner.setClickable(false);
    }

    public void distanceCheckedEditText(View view) {
        thereL.setBackgroundColor(getResources().getColor(R.color.translucent_black));
        distanceL.setBackgroundColor(getResources().getColor(R.color.dark_purple));
        there.setChecked(false);
        distance.setChecked(true);
        distanceM.setEnabled(true);
        distanceM.setClickable(true);
        distanceM.setFocusableInTouchMode(true);
        spinner.setClickable(true);
    }

    public void deleteText(View view) {
        Task task = taskDataSource.getTaskById(taskId);
        if (task != null) {
            taskDataSource.deleteTask(task);
        }
        Toast.makeText(this, "Text Task Deleted", Toast.LENGTH_SHORT).show();
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


    public void updateText(View view) {
        Task task = taskDataSource.getTaskById(taskId);
        List<TaskContact> contactTasks = taskContactDataSource.getTaskContactsByTaskId(task.getId());
        for (TaskContact contact : contactTasks) {
            taskContactDataSource.deleteTaskContact(contact);
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
        if (message != null) {
            reminder = message.getText().toString();
        }
        task.setReminder(reminder);
        String _contacts = contacts.getText().toString();
        String[] num1 = _contacts.split("<");
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
        }
        if (task != null) {
            taskDataSource.updateTask(task);
        }
        Toast.makeText(this, "Task Updated", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, TaskListActivity.class);
        startActivity(intent);
    }

    private class PopulateContacts extends AsyncTask<Void, Void, ArrayList<Map<String, String>>> {
        @Override
        protected ArrayList<Map<String, String>> doInBackground(Void... params) {

            return EditSMSTaskActivity.this.mPeopleList;
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, String>> maps) {
            final List<String> numbers = new ArrayList<String>();
            String existing = contacts.getText().toString();
            numbers.add(existing);
            mAdapter = new SimpleAdapter(MyApplication.getAppContext(), mPeopleList, R.layout.custcontview,
                    new String[]{"Name", "Phone", "Type"}, new int[]{
                    R.id.ccontName, R.id.ccontNo, R.id.ccontType}
            );

            contacts.setThreshold(1);
            contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                    contacts.setText(current);
                    contacts.setSelection(current.length());
                }
            });
            contacts.addTextChangedListener(new TextWatcher() {
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
            contacts.setAdapter(mAdapter);
            contacts.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            EditSMSTaskActivity.this.mPeopleList = maps;
        }
    }

    public int getPxByPercentage(double percentage) {

        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float height = MyApplication.getHeight();
        float px = height * (metrics.densityDpi / 160f);

        return (int) (px * percentage);
    }


}

