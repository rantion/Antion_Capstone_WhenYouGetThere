package com.example.rachel.wygt;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
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
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Rachel on 10/15/14.
 */
public class CreateTaskActivity extends Activity {

    private LatLng destinationLocation, currentLocation;
    private String distance, duration, destination;
    private TaskDataSource dataSource = MyApplication.getDataSource();
    private static final String PROX_ALERT_INTENT = "Proximity_Alert_Intent";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);
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

        }
    }

    @Override
    protected void onStop() {
//        if (proximityReciever != null) {
//            unregisterReceiver(proximityReciever);
//        }
        super.onStop();
    }

    public void setProximityAlert(View view) {


        EditText _miles = (EditText) findViewById(R.id.miles_away);
        String miles = _miles.getText().toString();
        RadioButton button = (RadioButton) findViewById(R.id.radio_there);
        double milesAway = 0.0;
        if (!miles.equals("...")) {
            milesAway = Double.valueOf(miles);
        } else if (button.isChecked()) {
            milesAway = .1;
        }
////        proximityReciever = new ProximityIntentReceiver();
//          LocationManager lm = gpsTracker.getLocationManager();
////        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
////        registerReceiver(proximityReciever, filter);
//        Intent intent = new Intent(PROX_ALERT_INTENT);
//
//        intent.putExtra("Time", System.currentTimeMillis());
//        intent.putExtra("Destination", destination);
//        PendingIntent proximityIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        lm.addProximityAlert(destinationLocation.latitude, destinationLocation.longitude, convertToMeters(milesAway), -1, proximityIntent);
//        Log.d("CreateTaskActivity", "Proximity Alert Created");
//        Toast.makeText(getApplicationContext(),
//                "Proximity Alert Created!!", Toast.LENGTH_SHORT)
//                .show();
//
//        String string = "Proximity alert created for " + destinationLocation.latitude + "," + destinationLocation.longitude;
//        TextView prox = (TextView) findViewById(R.id.proximity_data);
//        prox.setText(string);

        Map<LatLong, Long> locations =  MyApplication.getLocations();
      //  locations.put(new LatLong(destinationLocation),convertToMeters(milesAway));
        dataSource.createTask(destinationLocation,"This is my reminder "+ SystemClock.currentThreadTimeMillis(),convertToMeters(milesAway));
        Log.d("CreateTaskActivity", "Saved Destination");

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
            pDialog = new ProgressDialog(CreateTaskActivity.this);
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
}
