package com.example.rachel.wygt;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
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
 * Created by Rachel on 11/6/14.
 */
public class GetDrivingDistances extends AsyncTask<LatLng, LatLng, JSONObject> {

    private String destDistSeconds,destDistMeters;

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
        String duration = "";
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
                        destDistSeconds = _duration.getString("value");
                        JSONObject distance = element.getJSONObject("distance");

                        destDistMeters = distance.getString("value");
                        distances.add(distance.getString("text"));
                    }
                    String _distances = null;
                    for (String distance : distances) {
                        _distances = distance;
                    }
                    String distance = _distances;


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public String getDestDistSeconds() {
        return destDistSeconds;
    }

    public void setDestDistSeconds(String destDistSeconds) {
        this.destDistSeconds = destDistSeconds;
    }

    public String getDestDistMeters() {
        return destDistMeters;
    }

    public void setDestDistMeters(String destDistMeters) {
        this.destDistMeters = destDistMeters;
    }


}
