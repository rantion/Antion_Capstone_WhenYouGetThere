package com.example.rachel.wygt;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Rachel on 10/16/14.
 */
public class DistanceMatrixHelper {
    private LatLng origin, destination;
    public List<String> distances;


    public DistanceMatrixHelper(LatLng origin, LatLng destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public void getDistanceInMiles(){
        (new GetDrivingDistance()).execute(origin, destination);

    }

    public String getTimeEstimate(){
        return null;
    }

    private class GetDrivingDistance extends AsyncTask<LatLng, LatLng,JSONObject>{

        @Override
        protected JSONObject doInBackground(LatLng... params) {
            LatLng origin = params[0];
            LatLng destination = params[1];
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
                    origin.toString() + "&destinations=" + destination.toString() +
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
            distances = new ArrayList<String>();
            if(jsonObject!=null) {
                try {
                    rows = jsonObject.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject row = rows.getJSONObject(i);
                        JSONArray elements = row.getJSONArray("elements");
                        for (int j = 0; i < elements.length(); i++) {
                            JSONObject element = elements.getJSONObject(0);
                            JSONObject distance = element.getJSONObject("distance");
                            distances.add(distance.getString("value"));
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }



}
