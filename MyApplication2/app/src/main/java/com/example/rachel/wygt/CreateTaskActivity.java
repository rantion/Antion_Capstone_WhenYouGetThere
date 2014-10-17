package com.example.rachel.wygt;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

/**
 * Created by Rachel on 10/15/14.
 */
public class CreateTaskActivity extends Activity{

    private LatLng destinationLocation, currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            destinationLocation = (LatLng)extras.get("Destination_location");
            currentLocation = (LatLng)extras.get("Current_Location");
            String destination = (String) extras.get("Destination");
            String button = (String)extras.get("Button");
            TextView _destination = (TextView)findViewById(R.id.destination_address);
            TextView _buttonPushed = (TextView)findViewById(R.id.button_type);
            if(_destination!=null) {
                _destination.setText(destination);
            }
            if(_buttonPushed!=null){
                _buttonPushed.setText(button);
            }
            (new GetDrivingDistance()).execute(destinationLocation,currentLocation);

        }
    }


    private class GetDrivingDistance extends AsyncTask<LatLng, LatLng,JSONObject>{

        @Override
        protected JSONObject doInBackground(LatLng... params) {
            LatLng _origin = params[0];
            LatLng _destination = params[1];
            String origin = _origin.latitude+","+_origin.longitude;
            String destination = _destination.latitude+","+_destination.longitude;
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
            JSONArray rows = null;
            List<String>distances = new ArrayList<String>();
            if(jsonObject!=null) {
                try {
                    rows = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        JSONArray elements = row.getJSONArray("elements");
                        for (int j = 0; i < elements.length(); i++) {
                            JSONObject element = elements.getJSONObject(0);
                            JSONObject distance = element.getJSONObject("distance");
                            distances.add(distance.getString("text"));
                        }
                        String _distances = null;
                        for(String distance: distances){
                            _distances = distance +"\n\n";
                        }
                        TextView _distance = (TextView)findViewById(R.id.distance);
                        _distance.setText(_distances);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
