package com.example.rachel.wygt;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 10/27/14.
 */
public class DoSomethingActivity extends Activity implements View.OnKeyListener {

    private LatLng destinationLocation, currentLocation;
    private String distance, duration, destination;
    private ReminderDataSource reminderMessageDataSource = MyApplication.getReminderMessageDataSource();
    private ArrayList<Map<String, String>> mPeopleList;
    private SimpleAdapter mAdapter;
    private MultiAutoCompleteTextView mTxtPhoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle("WhenYouGetThere");
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("APP IS OPEN", "appIsOpenSetTrue - DOTaskActivity");
        editor.putBoolean("appIsOpen", true);
        editor.apply();
        setContentView(R.layout.do_task);
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

//            Cursor peopleCursor = getContentResolver().query(Contacts.People.CONTENT_URI, null, null, null, null);
//            ContactListAdapter contactadapter = new ContactListAdapter(this, peopleCursor);
//            MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.find_contact_editText);
//            textView.setAdapter(contactadapter);
//            textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

            mPeopleList = new ArrayList<Map<String, String>>();
            PopulatePeopleList();
            mTxtPhoneNo = (MultiAutoCompleteTextView) findViewById(R.id.find_contact_editText);
            mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.custcontview,
                    new String[] { "Name", "Phone", "Type" }, new int[] {
                    R.id.ccontName, R.id.ccontNo, R.id.ccontType });
            mTxtPhoneNo.setThreshold(1);
            mTxtPhoneNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> map = (Map<String, String>) parent
                            .getItemAtPosition(position);
                    String name = map.get("Name");
                    String number = map.get("Phone");
                    String current = mTxtPhoneNo.getText().toString();
                    mTxtPhoneNo.setText( " "+ name + "<" + number + ">, ");
                    String newCurrent = mTxtPhoneNo.getText().toString();
                    mTxtPhoneNo.setSelection(newCurrent.length());
                }
            });
            mTxtPhoneNo.setAdapter(mAdapter);
            mTxtPhoneNo.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());


        }

    }

    public void PopulatePeopleList() {
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
    }

//    public void onItemClick(AdapterView<?> av, View v, int index, long arg) {
//        Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
//        Iterator<String> myVeryOwnIterator = map.keySet().iterator();
//        while (myVeryOwnIterator.hasNext()) {
//            String key = (String) myVeryOwnIterator.next();
//            String value = (String) map.get(key);
//            Log.d("DOSOMETHING", "Key: "+key+" Value: "+value);
//            mTxtPhoneNo.setText(value);
//        }
//    }

    public void getContacts(View view) {
        Log.d("DOSOMETHING", "getContacts called");
//      Intent contactPicker = new Intent(this, ContactPickerActivity.class);
//        contactPicker.putExtra(ContactData.CHECK_ALL, cbCheckAll.isChecked());
//        contactPicker.setPackage("com.reptilemobile.MultipleContactsPicker");
//        startActivityForResult(contactPicker, REQUEST_CODE);
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
                            JSONObject distance = element.getJSONObject("distance");
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
    protected void onDestroy() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MyApplication.getAppContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Log.d("APP IS OPEN", "ONDESTROY__appIsOpenSetToFalse - DOSOMETHINGActivity");
        editor.putBoolean("appIsOpen", false);
        editor.apply();
        super.onDestroy();
    }

}
