package com.example.rachel.wygt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
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
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 10/27/14.
 */
public class DoSomethingActivity extends Activity implements View.OnKeyListener {

    private LatLng destinationLocation, currentLocation;
    private String distance, duration, destination, distanceMeters, durationSeconds;
    private ArrayList<Map<String, String>> mPeopleList;
    private SimpleAdapter mAdapter;
    private MultiAutoCompleteTextView mTxtPhoneNo;
    private TaskDataSource taskDataSource = MyApplication.getTaskDataSource();
    private TaskContactDataSource taskContactDataSource = MyApplication.getTaskContactDataSource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("GPS/APP IS OPEN", "appIsOpenSetTrue - DoSomethingActivity");
        editor.putBoolean("appIsOpen", true);
        editor.apply();
        setContentView(R.layout.do_task);
        RadioGroup group = (RadioGroup) findViewById(R.id.radio_text_call);
        group.check(R.id.radio_text);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            destinationLocation = (LatLng) extras.get("Destination_location");
            currentLocation = (LatLng) extras.get("Current_Location");
            (new GetDrivingDistance()).execute(destinationLocation, currentLocation);
            (new PopulateContacts()).execute();
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
            TextView send = (TextView) findViewById(R.id.send_blank_when);
            send.setText("Send text(s) when I am....");

        }

    }

    public void sendTextMessage(View view) {
        EditText _miles = (EditText) findViewById(R.id.miles_away);
        String miles = _miles.getText().toString();
        RadioButton button = (RadioButton) findViewById(R.id.radio_there);
        EditText _reminder = (EditText) findViewById(R.id.enter_reminder_field);
        MultiAutoCompleteTextView _contacts = (MultiAutoCompleteTextView) findViewById(R.id.multiAuto_contacts);
        String reminder = "";
        double milesAway = 0.03;
        if (miles.length() > 0) {
            milesAway = Double.valueOf(miles);
        } else if (button.isChecked()) {
            milesAway = .03;
        }
        if (_reminder != null) {
            reminder = _reminder.getText().toString();
        }
        Task task = taskDataSource.createTask(destinationLocation, reminder, convertToMeters(milesAway), Task.TEXT_MESSAGE_TASK_TYPE);
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

    public long getMinutesAwayRadius(int minutes){
        String distanceAway = distanceMeters;
        String _duration = durationSeconds;
        int meters = Integer.parseInt(distanceAway);
        int seconds = Integer.parseInt(_duration);
        double rate = (meters/seconds);
        double temp = rate*minutes;
        return (long)temp*60;
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

        private ArrayList<Map<String, String>> mPeopleList = new ArrayList<Map<String, String>>();
        private ProgressDialog prDialog;

        @Override
        protected void onPreExecute() {

            prDialog = new ProgressDialog(DoSomethingActivity.this);
            prDialog.setMessage("Loading Contacts ...");
            prDialog.setIndeterminate(false);
            prDialog.setCancelable(true);
            prDialog.show();
            super.onPreExecute();
        }

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
            startManagingCursor(people);
            return mPeopleList;
        }

        @Override
        protected void onPostExecute(ArrayList<Map<String, String>> maps) {
            prDialog.dismiss();
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
        RadioGroup group = (RadioGroup) findViewById(R.id.radio_text_call);
        group.check(R.id.radio_call);
        TextView send = (TextView) findViewById(R.id.send_blank_when);
        send.setText("Send reminder when I am....");
        LinearLayout text = (LinearLayout) findViewById(R.id.enter_contacts_text);
        text.setVisibility(View.GONE);
        LinearLayout phone = (LinearLayout) findViewById(R.id.enter_contacts_call_reminder);
        phone.setVisibility(View.VISIBLE);

    }

    public void textSelected(View view) {
        RadioGroup group = (RadioGroup) findViewById(R.id.radio_text_call);
        group.check(R.id.radio_text);
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


    private class GetDrivingDistance extends AsyncTask<LatLng, LatLng, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DoSomethingActivity.this);
            pDialog.setMessage("Performing Calculations ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(LatLng... params) {
            Thread.currentThread().setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
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
                            distanceMeters = distance.getString("value");
                            distances.add(distance.getString("text"));
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
